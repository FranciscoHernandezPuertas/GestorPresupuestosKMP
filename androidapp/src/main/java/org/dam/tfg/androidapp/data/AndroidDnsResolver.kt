package org.dam.tfg.androidapp.util

import android.util.Log
import com.mongodb.internal.dns.DnsResolver
import org.xbill.DNS.*

class AndroidDnsResolver : DnsResolver {
    private val TAG = "AndroidDnsResolver"

    override fun resolveHostFromSrvRecords(srvHost: String, srvServiceName: String): List<String?> {
        try {
            Log.d(TAG, "Resolviendo SRV records para: $srvServiceName.$srvHost")

            // Configuración para mejorar la resolución DNS
            Lookup.setDefaultSearchPath(".")
            val resolver = SimpleResolver()
            resolver.timeout = java.time.Duration.ofSeconds(5) // 5 segundos timeout

            val lookup = Lookup("$srvServiceName.$srvHost", Type.SRV)
            lookup.setResolver(resolver)
            val records = lookup.run()

            if (records == null || records.isEmpty()) {
                Log.w(TAG, "No se encontraron registros SRV, generando hosts alternativos")
                return generateFallbackHosts(srvHost)
            }

            return records.mapNotNull { record ->
                if (record is SRVRecord) {
                    val port = record.port
                    val target = record.target.toString().trimEnd('.')
                    Log.d(TAG, "SRV record encontrado: $target:$port")
                    "$target:$port"
                } else null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al resolver SRV records: ${e.message}", e)
            return generateFallbackHosts(srvHost)
        }
    }

    private fun generateFallbackHosts(host: String): List<String> {
        // Para MongoDB Atlas, genera hosts basados en el patrón estándar
        val cluster = if (host.contains(".")) {
            host.split(".").firstOrNull() ?: "cluster0"
        } else {
            host
        }

        return listOf(
            "$cluster-shard-00-00.gsfz9.mongodb.net:27017",
            "$cluster-shard-00-01.gsfz9.mongodb.net:27017",
            "$cluster-shard-00-02.gsfz9.mongodb.net:27017"
        )
    }

    override fun resolveAdditionalQueryParametersFromTxtRecords(host: String): String {
        try {
            Log.d(TAG, "Resolviendo TXT records para: $host")
            val lookup = Lookup(host, Type.TXT)
            val records = lookup.run() ?: return getDefaultMongoAtlasParams()

            val result = mutableMapOf<String, String>()
            records.filterIsInstance<TXTRecord>().forEach { txt ->
                val strings = txt.strings.joinToString("")
                val parameters = strings.split("&")
                parameters.forEach { parameter ->
                    val keyValue = parameter.split("=")
                    if (keyValue.size == 2) {
                        result[keyValue[0]] = keyValue[1]
                    }
                }
            }

            val params = result.entries.joinToString("&") { (key, value) -> "$key=$value" }
            return if (params.isNotBlank()) params else getDefaultMongoAtlasParams()
        } catch (e: Exception) {
            Log.e(TAG, "Error al resolver TXT records: ${e.message}", e)
            return getDefaultMongoAtlasParams()
        }
    }

    private fun getDefaultMongoAtlasParams(): String {
        return "ssl=true&replicaSet=atlas-awexkt-shard-0&authSource=admin&retryWrites=true&w=majority"
    }

    fun convertSrvToStandardUri(srvUri: String): String {
        try {
            Log.d(TAG, "Convirtiendo URI SRV a estándar: $srvUri")
            val regex = Regex("""mongodb\+srv://([^:]+):([^@]+)@([^/]+)(/[^?]*)?(\?.*)?""")
            val match = regex.matchEntire(srvUri) ?: throw IllegalArgumentException("URI SRV inválida: $srvUri")

            val (user, pass, host, path, query) = match.destructured

            // 1. Resolver hosts SRV
            val hosts = resolveHostFromSrvRecords(host, "_mongodb._tcp")
            if (hosts.isEmpty()) {
                throw IllegalStateException("No se encontraron registros SRV")
            }

            // 2. Resolver parámetros TXT
            val txtParams = resolveAdditionalQueryParametersFromTxtRecords(host)

            // 3. Unir parámetros de la URI original y TXT
            val allParams = listOfNotNull(
                query.removePrefix("?").takeIf { it.isNotBlank() },
                txtParams
            ).filter { it.isNotBlank() }.joinToString("&")

            // 4. Construir la URI estándar
            val standardUri = "mongodb://$user:$pass@${hosts.joinToString(",")}${path.ifBlank { "" }}${if (allParams.isNotBlank()) "?$allParams" else ""}"
            Log.d(TAG, "URI estándar generada: $standardUri")
            return standardUri
        } catch (e: Exception) {
            Log.e(TAG, "Error al convertir URI SRV: ${e.message}", e)
            throw IllegalStateException("No se pudieron resolver los hosts SRV", e)
        }
    }
}