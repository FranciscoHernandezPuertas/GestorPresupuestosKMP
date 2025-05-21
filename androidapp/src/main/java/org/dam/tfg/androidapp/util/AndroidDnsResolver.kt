package org.dam.tfg.androidapp.util

import android.util.Log
import org.xbill.DNS.*
import org.xbill.DNS.lookup.LookupResult
import org.xbill.DNS.lookup.LookupSession
import java.net.InetAddress
import java.net.UnknownHostException
import java.time.Duration
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

/**
 * Resolvedor DNS personalizado para Android que maneja conexiones MongoDB+SRV
 * Reemplaza la funcionalidad de JNDI que no está disponible en Android
 */
class AndroidDnsResolver {
    private val TAG = "AndroidDnsResolver"

    // Nombres de servidores DNS conocidos (Google y Cloudflare)
    private val DNS_SERVERS = listOf(
        "8.8.8.8",       // Google DNS primario
        "8.8.4.4",       // Google DNS secundario
        "1.1.1.1",       // Cloudflare primario
        "1.0.0.1",       // Cloudflare secundario
        "9.9.9.9"        // Quad9 DNS
    )

    /**
     * Resuelve los hosts a partir de registros SRV para un host de MongoDB
     * @param host Nombre del host sin el prefijo _mongodb._tcp
     * @return Lista de hosts con formato hostname:port
     */
    fun resolveHostFromSrvRecords(host: String): List<String> {
        try {
            Log.d(TAG, "Resolviendo SRV records para: _mongodb._tcp.$host")

            // Para MongoDB Atlas, usar método específico si el patrón coincide
            if (host.contains(".mongodb.net")) {
                val atlasHosts = resolveMongoDBAtlasHosts(host)
                if (atlasHosts.isNotEmpty()) {
                    return atlasHosts
                }
            }

            // Probamos primero con el resolvedor DNS por defecto
            var hosts = tryResolveWithResolver(host, null)

            // Si falla, intentamos con servidores DNS específicos
            if (hosts.isEmpty()) {
                Log.d(TAG, "Intentando con servidores DNS específicos")
                for (dnsServer in DNS_SERVERS) {
                    hosts = tryResolveWithResolver(host, dnsServer)
                    if (hosts.isNotEmpty()) {
                        Log.d(TAG, "Resolución exitosa con DNS server: $dnsServer")
                        break
                    }
                }
            }

            // Si aún no tenemos hosts, intentar directamente los nombres estándar de MongoDB Atlas
            if (hosts.isEmpty() && host.contains(".mongodb.net")) {
                Log.d(TAG, "Intentando con nombres estándar de MongoDB Atlas")
                hosts = generateFallbackHosts(host)
            }

            Log.d(TAG, "Hosts resueltos: $hosts")
            return hosts
        } catch (e: Exception) {
            Log.e(TAG, "Error al resolver SRV records: ${e.message}", e)
            return generateFallbackHosts(host)
        }
    }

    /**
     * Método específico para resolver hosts de MongoDB Atlas
     * basado en sus patrones de nomenclatura conocidos
     */
    private fun resolveMongoDBAtlasHosts(host: String): List<String> {
        try {
            Log.d(TAG, "Aplicando resolución específica para MongoDB Atlas: $host")

            // Extraer el nombre del cluster y la región de MongoDB Atlas
            val match = Regex("(cluster[0-9]+)(?:-shard-[0-9]+)?(?:\\.|-)([a-z0-9]+)\\.mongodb\\.net").find(host)
            if (match != null) {
                val (clusterName, region) = match.destructured

                // Generar los 3 nodos estándar del replicaset de MongoDB Atlas
                val hosts = listOf(
                    "$clusterName-shard-00-00.$region.mongodb.net:27017",
                    "$clusterName-shard-00-01.$region.mongodb.net:27017",
                    "$clusterName-shard-00-02.$region.mongodb.net:27017"
                )

                // Verificar que al menos uno de los hosts es resoluble
                val resolvedHosts = hosts.filter { hostPort ->
                    val parts = hostPort.split(":")
                    val hostname = parts[0]
                    try {
                        val address = InetAddress.getByName(hostname)
                        true
                    } catch (e: Exception) {
                        false
                    }
                }

                if (resolvedHosts.isNotEmpty()) {
                    Log.d(TAG, "Hosts de MongoDB Atlas resueltos: $resolvedHosts")
                    return resolvedHosts
                }
            }

            return emptyList()
        } catch (e: Exception) {
            Log.w(TAG, "Error en resolución específica de MongoDB Atlas: ${e.message}")
            return emptyList()
        }
    }

    /**
     * Intenta resolver registros SRV usando un servidor DNS específico
     */
    private fun tryResolveWithResolver(host: String, dnsServerIp: String?): List<String> {
        try {
            // Configurar el resolver
            val resolver = if (dnsServerIp != null) {
                SimpleResolver(dnsServerIp).apply {
                    timeout = Duration.ofSeconds(5)
                }
            } else {
                SimpleResolver().apply {
                    timeout = Duration.ofSeconds(5)
                }
            }

            // Usar una implementación robusta de lookup asíncrono
            val hosts = mutableListOf<String>()
            val resultRef = AtomicReference<LookupResult?>()
            val latch = CountDownLatch(1)

            try {
                val lookupSession = LookupSession.defaultBuilder()
                    .resolver(resolver)
                    .build()

                lookupSession.lookupAsync(Name.fromString("_mongodb._tcp.$host"), Type.SRV)
                    .whenComplete { records, ex ->
                        if (ex != null) {
                            Log.w(TAG, "Error en lookupAsync: ${ex.message}")
                        } else {
                            resultRef.set(records)
                        }
                        latch.countDown()
                    }

                // Esperar hasta 5 segundos por respuesta
                latch.await(5, TimeUnit.SECONDS)

                // Procesar resultados si existen
                val records = resultRef.get()
                if (records != null) {
                    // Usamos getRecords() para obtener la lista de registros en lugar de .answers
                    for (record in records.getRecords()) {
                        if (record.type == Type.SRV) {
                            val srv = record as SRVRecord
                            val port = srv.port
                            val target = srv.target.toString().trimEnd('.')
                            hosts.add("$target:$port")
                            Log.d(TAG, "SRV record encontrado (async): $target:$port")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error en consulta asíncrona: ${e.message}")
            }

            // Si la consulta asíncrona falló, intentar con lookup sincrónico
            if (hosts.isEmpty()) {
                val srvLookup = Lookup("_mongodb._tcp.$host", Type.SRV)
                srvLookup.setResolver(resolver)
                val srvRecords = srvLookup.run()

                if (srvRecords != null) {
                    for (record in srvRecords) {
                        if (record is SRVRecord) {
                            val port = record.port
                            val target = record.target.toString().trimEnd('.')
                            Log.d(TAG, "SRV record encontrado: $target:$port")
                            hosts.add("$target:$port")
                        }
                    }
                }
            }

            return hosts
        } catch (e: Exception) {
            Log.w(TAG, "Error con resolver ${dnsServerIp ?: "default"}: ${e.message}")
            return emptyList()
        }
    }

    /**
     * Resuelve los registros TXT de un host
     */
    fun resolveTxtRecords(host: String): Map<String, String> {
        try {
            Log.d(TAG, "Resolviendo TXT records para: $host")

            // Si es MongoDB Atlas, usar valores predeterminados conocidos primero
            if (host.contains(".mongodb.net")) {
                Log.d(TAG, "Usando valores TXT predeterminados de MongoDB Atlas")
                val params = getMongoDBAtlasDefaultParams(host)
                if (params.isNotEmpty()) {
                    return params
                }
            }

            // Probar primero con resolvedor por defecto
            var params = mutableMapOf<String, String>()

            // Intentar con el resolvedor predeterminado
            params = tryResolveTxtWithResolver(host, null)

            // Si falla, intentar con servidores DNS específicos
            if (params.isEmpty()) {
                for (dnsServer in DNS_SERVERS) {
                    params = tryResolveTxtWithResolver(host, dnsServer)
                    if (params.isNotEmpty()) {
                        Log.d(TAG, "TXT records resueltos con DNS server: $dnsServer")
                        break
                    }
                }
            }

            // Si no se encontraron parámetros, usar valores predeterminados para MongoDB Atlas
            if (params.isEmpty()) {
                Log.w(TAG, "No se pudieron resolver TXT records, usando parámetros predeterminados")
                params.putAll(getDefaultMongoDBParams())
            }

            return params
        } catch (e: Exception) {
            Log.e(TAG, "Error al resolver TXT records: ${e.message}", e)
            return getDefaultMongoDBParams()
        }
    }

    /**
     * Obtiene parámetros predeterminados para MongoDB Atlas basados en el nombre del host
     */
    private fun getMongoDBAtlasDefaultParams(host: String): Map<String, String> {
        try {
            // Extraer el nombre del cluster y la región de MongoDB Atlas
            val match = Regex("(cluster[0-9]+)(?:-shard-[0-9]+)?(?:\\.|-)([a-z0-9]+)\\.mongodb\\.net").find(host)
            if (match != null) {
                val (clusterName, region) = match.destructured

                // Reemplazar caracteres no alfanuméricos por guiones en el nombreCluster
                val sanitizedClusterName = clusterName.replace(Regex("[^a-zA-Z0-9]"), "-")

                // El formato típico del replicaSet de Atlas es atlas-XXXXX-shard-0
                // donde XXXXX son caracteres aleatorios. Usaremos el nombre de región como estimación
                return mapOf(
                    "ssl" to "true",
                    "replicaSet" to "atlas-$region-shard-0",
                    "authSource" to "admin",
                    "retryWrites" to "true",
                    "w" to "majority",
                    "maxIdleTimeMS" to "60000",
                    "connectTimeoutMS" to "60000",
                    "socketTimeoutMS" to "60000",
                    "serverSelectionTimeoutMS" to "60000"
                )
            }

            return emptyMap()
        } catch (e: Exception) {
            Log.w(TAG, "Error al generar parámetros predeterminados para Atlas: ${e.message}")
            return emptyMap()
        }
    }

    /**
     * Intenta resolver registros TXT usando un servidor DNS específico
     */
    private fun tryResolveTxtWithResolver(host: String, dnsServerIp: String?): MutableMap<String, String> {
        val params = mutableMapOf<String, String>()

        try {
            // Configurar el resolver
            val resolver = if (dnsServerIp != null) {
                SimpleResolver(dnsServerIp).apply {
                    timeout = Duration.ofSeconds(5)
                }
            } else {
                SimpleResolver().apply {
                    timeout = Duration.ofSeconds(5)
                }
            }

            val txtLookup = Lookup(host, Type.TXT)
            txtLookup.setResolver(resolver)
            val txtRecords = txtLookup.run()

            if (txtRecords != null && txtRecords.isNotEmpty()) {
                for (record in txtRecords) {
                    if (record is TXTRecord) {
                        val strings = record.strings
                        val txtValue = strings.joinToString("")
                        Log.d(TAG, "TXT record encontrado: $txtValue")

                        // Extraer parámetros de conexión
                        txtValue.split("&").forEach { param ->
                            val keyValue = param.split("=")
                            if (keyValue.size == 2) {
                                params[keyValue[0]] = keyValue[1]
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error resolviendo TXT con DNS ${dnsServerIp ?: "default"}: ${e.message}")
        }

        return params
    }

    /**
     * Genera hosts alternativos basados en el patrón de nomenclatura de MongoDB Atlas
     */
    private fun generateFallbackHosts(host: String): List<String> {
        // Para MongoDB Atlas, intentar generar los hosts según el patrón estándar
        val clusterName = when {
            host.contains(".mongodb.net") && host.contains(".") -> {
                // Extraer el nombre del cluster para MongoDB Atlas
                val atlasPattern = Regex("(cluster[0-9]+)(?:-shard-[0-9]+)?(?:\\.|-)([a-z0-9]+)\\.mongodb\\.net")
                val match = atlasPattern.find(host)
                if (match != null) {
                    match.groupValues[1]
                } else {
                    host.split(".").first()
                }
            }
            host.contains(".") -> host.split(".").first()
            else -> host
        }

        // Intentar extraer la región de la URL de MongoDB Atlas
        val regionPattern = Regex("cluster[0-9]+(?:-shard-[0-9]+)?(?:\\.|-)([a-z0-9]+)\\.mongodb\\.net")
        val regionMatch = regionPattern.find(host)
        val region = regionMatch?.groupValues?.get(1) ?: "gsfz9"

        Log.d(TAG, "Generando hosts alternativos para cluster: $clusterName, región: $region")

        // Para Atlas, generamos los tres nodos del replicaset y verificamos su resolubilidad
        val potentialHosts = listOf(
            "$clusterName-shard-00-00.$region.mongodb.net:27017",
            "$clusterName-shard-00-01.$region.mongodb.net:27017",
            "$clusterName-shard-00-02.$region.mongodb.net:27017"
        )

        // Verificar qué hosts son resolubles
        val validHosts = potentialHosts.filter { hostPort ->
            try {
                val parts = hostPort.split(":")
                val hostname = parts[0]
                InetAddress.getByName(hostname)
                true
            } catch (e: UnknownHostException) {
                Log.w(TAG, "Host no resoluble: $hostPort")
                false
            } catch (e: Exception) {
                Log.w(TAG, "Error verificando host $hostPort: ${e.message}")
                true // En caso de error diferente a UnknownHostException, asumimos que es válido
            }
        }

        return if (validHosts.isNotEmpty()) {
            Log.d(TAG, "Hosts alternativos verificados: $validHosts")
            validHosts
        } else {
            Log.w(TAG, "Ningún host alternativo resoluble, devolviendo lista original")
            potentialHosts
        }
    }

    /**
     * Devuelve los parámetros de conexión MongoDB por defecto
     */
    private fun getDefaultMongoDBParams(): Map<String, String> {
        return mapOf(
            "ssl" to "true",
            "replicaSet" to "atlas-awexkt-shard-0",
            "authSource" to "admin",
            "retryWrites" to "true",
            "w" to "majority",
            "maxIdleTimeMS" to "60000",
            "connectTimeoutMS" to "60000",
            "socketTimeoutMS" to "60000",
            "serverSelectionTimeoutMS" to "60000"
        )
    }

    /**
     * Convierte una URI de MongoDB SRV a una URI estándar
     * @param srvUri URI con formato mongodb+srv://
     * @return URI estándar con hosts resueltos
     */
    fun convertSrvToStandardUri(srvUri: String): String {
        try {
            Log.d(TAG, "Convirtiendo URI SRV a estándar: $srvUri")

            // Extraer componentes de la URI SRV
            val regex = Regex("mongodb\\+srv://([^:]+):([^@]+)@([^/?]+)(/[^?]*)?(?:\\?(.*))?")
            val matchResult = regex.find(srvUri) ?: throw IllegalArgumentException("Formato de URI SRV inválido: $srvUri")

            val (username, password, host, database, queryParams) = matchResult.destructured

            // 1. Resolver hosts SRV
            val hosts = resolveHostFromSrvRecords(host)
            if (hosts.isEmpty()) {
                throw IllegalStateException("No se pudieron resolver hosts para: $host")
            }

            // 2. Resolver parámetros TXT
            val txtParams = resolveTxtRecords(host)

            // 3. Combinar parámetros de la URI original con los de TXT
            val allParams = mutableMapOf<String, String>()

            // Primero añadir los parámetros de TXT
            allParams.putAll(txtParams)

            // Luego añadir los parámetros de la URI original (que tienen prioridad)
            if (queryParams.isNotEmpty()) {
                queryParams.split("&").forEach { param ->
                    val parts = param.split("=")
                    if (parts.size == 2) {
                        allParams[parts[0]] = parts[1]
                    }
                }
            }

            // Asegurar que siempre tengamos parámetros críticos
            if (!allParams.containsKey("ssl")) allParams["ssl"] = "true"
            if (!allParams.containsKey("authSource")) allParams["authSource"] = "admin"

            // Asegurar tiempos de timeout adecuados
            if (!allParams.containsKey("connectTimeoutMS")) allParams["connectTimeoutMS"] = "60000"
            if (!allParams.containsKey("socketTimeoutMS")) allParams["socketTimeoutMS"] = "60000"
            if (!allParams.containsKey("serverSelectionTimeoutMS")) allParams["serverSelectionTimeoutMS"] = "60000"

            // Construir la cadena de parámetros
            val paramsString = if (allParams.isNotEmpty()) {
                "?" + allParams.entries.joinToString("&") { "${it.key}=${it.value}" }
            } else {
                ""
            }

            // 4. Construir la URI estándar
            val dbPath = database.ifEmpty { "/admin" }
            val standardUri = "mongodb://$username:$password@${hosts.joinToString(",")}$dbPath$paramsString"
            Log.d(TAG, "URI estándar generada: ${maskSensitiveInfo(standardUri)}")

            return standardUri
        } catch (e: Exception) {
            Log.e(TAG, "Error al convertir URI SRV: ${e.message}", e)
            throw e
        }
    }

    /**
     * Oculta información sensible en las URIs para los logs
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
