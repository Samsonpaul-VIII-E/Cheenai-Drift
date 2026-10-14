package com.example.leadforge.engine

import java.security.MessageDigest

object FastFingerprint {

    /**
     * Creates a deterministic 32-character SHA-256 fingerprint from lead identity parts.
     * Matches the Python / C++ LeadForge specification.
     */
    fun compute(vararg parts: String): String {
        val normalized = parts
            .map { it.trim().lowercase() }
            .filter { it.isNotEmpty() }
            .joinToString("|")

        return sha256(normalized).take(32)
    }

    fun sha256(text: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(text.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
