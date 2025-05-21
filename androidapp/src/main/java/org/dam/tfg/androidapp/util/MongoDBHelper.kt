package org.dam.tfg.androidapp.util

import android.util.Log
import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.ReadPreference
import com.mongodb.ServerApi
import com.mongodb.ServerApiVersion
import com.mongodb.kotlin.client.coroutine.MongoClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bson.BsonDocument
import org.bson.BsonInt32
import java.io.IOException
import java.net.HttpURLConnection
import java.net.InetSocketAddress
import java.net.Socket
import java.net.URL
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

/**
 * Clase auxiliar para crear conexiones a MongoDB en Android
 * Maneja las especificidades de Android para conectarse a MongoDB Atlas
 */
object MongoDBHelper {
    private const val TAG = "MongoDBHelper"
    private val dnsResolver = AndroidDnsResolver()

    /**
     * Crea un cliente MongoDB utilizando la URI proporcionada
     * Convierte automáticamente las URI de tipo mongodb+srv
     * a URIs estándar que funcionen en Android
     */
    suspend fun createClient(mongodbUri: String): MongoClient = withContext(Dispatchers.IO) {
        try {
            val finalUri = if (mongodbUri.startsWith("mongodb+srv://")) {
                Log.d(TAG, "Detectada URI SRV, convirtiendo a formato estándar")
                dnsResolver.convertSrvToStandardUri(mongodbUri)
            } else {
                mongodbUri
            }

            Log.d(TAG, "Creando cliente MongoDB con URI: ${maskSensitiveInfo(finalUri)}")

            // Implementar verificación de conectividad previa para detectar problemas de red temprano
            val connString = ConnectionString(finalUri)
            val hosts = connString.hosts

            // Verificar qué hosts están realmente disponibles
            val availableHosts = verifyServerAvailability(hosts)
            if (availableHosts.isEmpty()) {
                Log.e(TAG, "No se pudo conectar a ningún servidor MongoDB. Verificando puertos...")
                // Si no se puede conectar, verificar si los puertos están abiertos
                val openPorts = checkMongoDBPorts(hosts)
                if (openPorts.isEmpty()) {
                    Log.e(TAG, "Ningún puerto MongoDB está abierto. Posible problema de firewall o red.")
                } else {
                    Log.d(TAG, "Puertos MongoDB abiertos: $openPorts")
                }
            } else {
                Log.d(TAG, "Servidores MongoDB disponibles: $availableHosts")
            }

            // Configurar SSLContext personalizado para evitar problemas de validación de certificados
            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
            })

            val sslContext = SSLContext.getInstance("TLSv1.2")
            sslContext.init(null, trustAllCerts, SecureRandom())

            // Instalar el SSLContext en el sistema para todas las conexiones
            try {
                HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.socketFactory)
                HttpsURLConnection.setDefaultHostnameVerifier { _, _ -> true }
            } catch (e: Exception) {
                Log.w(TAG, "No se pudo configurar el SSLContext por defecto: ${e.message}")
            }

            // Crear una API de servidor v1 para mejor compatibilidad
            val serverApi = ServerApi.builder()
                .version(ServerApiVersion.V1)
                .strict(false)
                .build()

            // Configuraciones optimizadas para la conexión
            val settings = MongoClientSettings.builder()
                .applyConnectionString(connString)
                .applyToSslSettings { builder ->
                    builder.enabled(true)
                    builder.invalidHostNameAllowed(true)
                    builder.context(sslContext)
                }
                .applyToSocketSettings { builder ->
                    builder.connectTimeout(30, TimeUnit.SECONDS)
                    builder.readTimeout(30, TimeUnit.SECONDS)
                }
                .applyToClusterSettings { builder ->
                    builder.serverSelectionTimeout(30, TimeUnit.SECONDS)
                    builder.localThreshold(20, TimeUnit.MILLISECONDS)
                }
                .applyToConnectionPoolSettings { builder ->
                    builder.maxConnectionIdleTime(30, TimeUnit.SECONDS)
                    builder.maxSize(3) // Reducir el número de conexiones para evitar problemas
                    builder.minSize(1) // Mantener al menos una conexión
                    builder.maxWaitTime(20, TimeUnit.SECONDS)
                }
                .readPreference(ReadPreference.primaryPreferred()) // Más flexible que nearest o primary
                .retryWrites(true)
                .retryReads(true)
                .serverApi(serverApi)
                .build()

            // Crear el cliente MongoDB
            return@withContext MongoClient.create(settings)
        } catch (e: Exception) {
            Log.e(TAG, "Error al crear cliente MongoDB: ${e.message}", e)
            throw e
        }
    }

    /**
     * Verifica qué servidores MongoDB están disponibles
     */
    private fun verifyServerAvailability(hosts: List<String>): List<String> {
        val availableServers = mutableListOf<String>()

        for (host in hosts) {
            try {
                // Extraer host y puerto
                val parts = host.split(":")
                val hostname = parts[0]
                val port = if (parts.size > 1) parts[1].toInt() else 27017

                // Verificar si el servidor responde a una solicitud HTTP básica
                val isAvailable = try {
                    val url = URL("https://$hostname:$port")
                    val connection = url.openConnection() as HttpURLConnection
                    connection.connectTimeout = 5000
                    connection.readTimeout = 5000
                    connection.requestMethod = "HEAD"

                    // No necesitamos verificar el código de respuesta, sólo que conecta
                    connection.connect()
                    connection.disconnect()
                    true
                } catch (e: Exception) {
                    // Intentar con conexión de socket simple
                    try {
                        val socket = Socket()
                        socket.connect(InetSocketAddress(hostname, port), 5000)
                        socket.close()
                        true
                    } catch (e2: Exception) {
                        Log.w(TAG, "No se puede conectar a $host: ${e2.message}")
                        false
                    }
                }

                if (isAvailable) {
                    Log.d(TAG, "Servidor disponible: $host")
                    availableServers.add(host)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error verificando servidor $host: ${e.message}")
            }
        }

        return availableServers
    }

    /**
     * Verifica si los puertos MongoDB están abiertos en los hosts
     */
    private fun checkMongoDBPorts(hosts: List<String>): List<String> {
        val openPorts = mutableListOf<String>()

        for (host in hosts) {
            try {
                // Extraer host y puerto
                val parts = host.split(":")
                val hostname = parts[0]
                val port = if (parts.size > 1) parts[1].toInt() else 27017

                // Verificar si el puerto está abierto
                try {
                    val socket = Socket()
                    socket.connect(InetSocketAddress(hostname, port), 5000)
                    socket.close()
                    openPorts.add(host)
                    Log.d(TAG, "Puerto abierto: $host")
                } catch (e: IOException) {
                    Log.w(TAG, "Puerto cerrado para $host: ${e.message}")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error verificando puerto para $host: ${e.message}")
            }
        }

        return openPorts
    }

    /**
     * Oculta información sensible de las URIs de MongoDB para los logs
     */
    private fun maskSensitiveInfo(uri: String): String {
        return try {
            val regex = Regex("(mongodb(?:\\+srv)?://)([^:]+):([^@]+)@")
            regex.replace(uri, "$1$2:****@")
        } catch (e: Exception) {
            "URI protegida"
        }
    }
}



