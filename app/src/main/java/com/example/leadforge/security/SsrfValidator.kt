package com.example.leadforge.security

import java.net.InetAddress
import java.net.URI
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed class ValidationResult {
    data class Valid(val canonicalUrl: String, val host: String, val resolvedIps: List<String>) : ValidationResult()
    data class Rejected(val reason: String, val inputUrl: String) : ValidationResult()
}

object SsrfValidator {

    private val ALLOWED_SCHEMES = setOf("http", "https")
    private val FORBIDDEN_HOSTNAMES = setOf(
        "localhost",
        "localhost.localdomain",
        "ip6-localhost",
        "ip6-loopback",
        "0.0.0.0",
        "metadata.google.internal",
        "169.254.169.254"
    )

    private val ALLOWED_PORTS = setOf(80, 443, 8080, 8443, 3000, 5000, 8000)

    /**
     * Validates whether a target URL is a safe public web destination.
     * Enforces scheme, port, DNS resolution, and blocks all loopback, RFC1918 private,
     * link-local, multicast, and cloud metadata addresses.
     */
    suspend fun validate(urlStr: String): ValidationResult = withContext(Dispatchers.IO) {
        val trimmed = urlStr.trim()
        if (trimmed.isEmpty()) {
            return@withContext ValidationResult.Rejected("URL cannot be empty", urlStr)
        }

        val uri: URI = try {
            val normalized = if (!trimmed.startsWith("http://", ignoreCase = true) &&
                !trimmed.startsWith("https://", ignoreCase = true)
            ) {
                "https://$trimmed"
            } else {
                trimmed
            }
            URI(normalized)
        } catch (e: Exception) {
            return@withContext ValidationResult.Rejected("Malformed URL: ${e.message}", urlStr)
        }

        val scheme = uri.scheme?.lowercase() ?: ""
        if (scheme !in ALLOWED_SCHEMES) {
            return@withContext ValidationResult.Rejected("Forbidden scheme '$scheme'. Only HTTP and HTTPS are permitted.", urlStr)
        }

        val host = uri.host?.lowercase()?.trim('[', ']') ?: ""
        if (host.isEmpty()) {
            return@withContext ValidationResult.Rejected("Target host is missing or invalid", urlStr)
        }

        if (host in FORBIDDEN_HOSTNAMES || host.endsWith(".localhost") || host.endsWith(".local")) {
            return@withContext ValidationResult.Rejected("SSRF Shield: Host '$host' is blocked (loopback/local)", urlStr)
        }

        val port = uri.port
        if (port != -1 && port !in ALLOWED_PORTS) {
            return@withContext ValidationResult.Rejected("SSRF Shield: Non-standard web port $port is blocked", urlStr)
        }

        // DNS resolution check
        val resolvedIps = mutableListOf<String>()
        try {
            val addresses = InetAddress.getAllByName(host)
            for (addr in addresses) {
                val ipStr = addr.hostAddress ?: ""
                resolvedIps.add(ipStr)

                if (addr.isLoopbackAddress) {
                    return@withContext ValidationResult.Rejected("SSRF Shield: Resolved to loopback address ($ipStr)", urlStr)
                }
                if (addr.isSiteLocalAddress) {
                    return@withContext ValidationResult.Rejected("SSRF Shield: Resolved to RFC 1918 private network ($ipStr)", urlStr)
                }
                if (addr.isLinkLocalAddress) {
                    return@withContext ValidationResult.Rejected("SSRF Shield: Resolved to link-local address ($ipStr)", urlStr)
                }
                if (addr.isMulticastAddress) {
                    return@withContext ValidationResult.Rejected("SSRF Shield: Resolved to multicast address ($ipStr)", urlStr)
                }
                if (addr.isAnyLocalAddress) {
                    return@withContext ValidationResult.Rejected("SSRF Shield: Resolved to any-local wildcard address ($ipStr)", urlStr)
                }

                // Check CGNAT (100.64.0.0/10) and Cloud Metadata (169.254.169.254)
                val rawBytes = addr.address
                if (rawBytes.size == 4) {
                    val b0 = rawBytes[0].toInt() and 0xFF
                    val b1 = rawBytes[1].toInt() and 0xFF
                    if (b0 == 100 && (b1 in 64..127)) {
                        return@withContext ValidationResult.Rejected("SSRF Shield: Resolved to Shared Carrier NAT address ($ipStr)", urlStr)
                    }
                    if (b0 == 169 && b1 == 254) {
                        return@withContext ValidationResult.Rejected("SSRF Shield: Cloud metadata address blocked ($ipStr)", urlStr)
                    }
                }
            }
        } catch (e: Exception) {
            return@withContext ValidationResult.Rejected("DNS Resolution failed for '$host': ${e.message}", urlStr)
        }

        val canonical = uri.toASCIIString()
        ValidationResult.Valid(canonical, host, resolvedIps)
    }

    fun isSameDomain(urlA: String, urlB: String): Boolean {
        return try {
            val hostA = URL(urlA).host.lowercase()
            val hostB = URL(urlB).host.lowercase()
            hostA == hostB || hostA.endsWith(".$hostB") || hostB.endsWith(".$hostA")
        } catch (e: Exception) {
            false
        }
    }
}
