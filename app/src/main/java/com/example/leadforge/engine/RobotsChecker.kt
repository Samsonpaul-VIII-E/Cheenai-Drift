package com.example.leadforge.engine

import java.net.URI
import java.net.URL
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

class RobotsChecker(private val httpClient: OkHttpClient) {

    private val domainCache = ConcurrentHashMap<String, RobotsRules>()

    data class RobotsRules(
        val fetchedAt: Long,
        val disallowedPaths: List<String>
    )

    suspend fun isAllowed(urlStr: String, userAgent: String = "LeadForge-X"): Boolean = withContext(Dispatchers.IO) {
        val uri = try { URI(urlStr) } catch (e: Exception) { return@withContext false }
        val host = uri.host?.lowercase() ?: return@withContext false
        val path = uri.path ?: "/"

        val cached = domainCache[host]
        if (cached != null && (System.currentTimeMillis() - cached.fetchedAt < 86_400_000L)) {
            return@withContext isPathAllowed(path, cached.disallowedPaths)
        }

        // Fetch robots.txt
        val robotsUrl = "${uri.scheme ?: "https"}://$host/robots.txt"
        val disallowed = mutableListOf<String>()

        try {
            val request = Request.Builder()
                .url(robotsUrl)
                .header("User-Agent", userAgent)
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: ""
                    var relevantUserAgent = false

                    for (line in body.lines()) {
                        val trimmed = line.trim()
                        if (trimmed.startsWith("#") || trimmed.isEmpty()) continue

                        val lower = trimmed.lowercase()
                        if (lower.startsWith("user-agent:")) {
                            val ua = trimmed.substringAfter(":").trim()
                            relevantUserAgent = (ua == "*" || ua.contains("leadforge", ignoreCase = true))
                        } else if (relevantUserAgent && lower.startsWith("disallow:")) {
                            val disallowedPath = trimmed.substringAfter(":").trim()
                            if (disallowedPath.isNotEmpty()) {
                                disallowed.add(disallowedPath)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Fail-open for missing/unreachable robots.txt (404/network), but record cached state
        }

        val rules = RobotsRules(System.currentTimeMillis(), disallowed)
        domainCache[host] = rules
        isPathAllowed(path, disallowed)
    }

    private fun isPathAllowed(path: String, disallowed: List<String>): Boolean {
        for (d in disallowed) {
            if (d == "/" || path.startsWith(d)) {
                return false
            }
        }
        return true
    }
}
