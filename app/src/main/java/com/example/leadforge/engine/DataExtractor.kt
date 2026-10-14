package com.example.leadforge.engine

import com.example.leadforge.model.ExtractedData
import java.net.URI
import java.net.URL
import java.util.regex.Pattern

object DataExtractor {

    private val EMAIL_REGEX = Pattern.compile(
        "\\b[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)+\\b"
    )

    private val MAILTO_REGEX = Pattern.compile(
        "href=[\"']mailto:([^\"'?#]+)",
        Pattern.CASE_INSENSITIVE
    )

    private val TEL_REGEX = Pattern.compile(
        "href=[\"']tel:([^\"'?#]+)",
        Pattern.CASE_INSENSITIVE
    )

    private val PHONE_CANDIDATE_REGEX = Pattern.compile(
        "(?<!\\d)(?:\\+?\\d[\\d\\s().\\-]{7,}\\d)(?!\\d)"
    )

    private val SOCIAL_PATTERNS = mapOf(
        "linkedin" to Pattern.compile("https?://(?:www\\.)?linkedin\\.com/(?:company|in)/[^\"'\\s<>]+", Pattern.CASE_INSENSITIVE),
        "facebook" to Pattern.compile("https?://(?:www\\.)?facebook\\.com/[^\"'\\s<>]+", Pattern.CASE_INSENSITIVE),
        "instagram" to Pattern.compile("https?://(?:www\\.)?instagram\\.com/[^\"'\\s<>]+", Pattern.CASE_INSENSITIVE),
        "x" to Pattern.compile("https?://(?:www\\.)?(?:x\\.com|twitter\\.com)/[^\"'\\s<>]+", Pattern.CASE_INSENSITIVE),
        "youtube" to Pattern.compile("https?://(?:www\\.)?youtube\\.com/(?:@[^\"'\\s<>]+|c/[^\"'\\s<>]+)", Pattern.CASE_INSENSITIVE),
        "tiktok" to Pattern.compile("https?://(?:www\\.)?tiktok\\.com/@[^\"'\\s<>]+", Pattern.CASE_INSENSITIVE)
    )

    private val ROLE_EMAILS = setOf(
        "info", "hello", "contact", "sales", "support", "admin",
        "office", "business", "marketing", "salesteam", "enquiry",
        "inquiry", "help", "service", "team"
    )

    private val BLOCK_MARKERS = listOf(
        "captcha",
        "verify you are human",
        "unusual traffic",
        "access denied",
        "automated queries",
        "robot check"
    )

    private val GENERIC_TITLE_PREFIXES = setOf(
        "home", "contact us", "contact", "about us", "about", "welcome",
        "index", "landing", "homepage", "untitled"
    )

    fun isBlockedPage(html: String, statusCode: Int): Boolean {
        if (statusCode in setOf(403, 429, 503)) return true
        val snippet = html.take(100_000).lowercase()
        return BLOCK_MARKERS.any { marker -> snippet.contains(marker) }
    }

    fun extract(html: String, baseUrl: String, includeRoleEmails: Boolean = true): ExtractedData {
        val title = extractTagContent(html, "title")
        val metaDesc = extractMetaContent(html, "description")
        val ogTitle = extractMetaProperty(html, "og:title")
        val ogDesc = extractMetaProperty(html, "og:description")
        val finalTitle = ogTitle.ifEmpty { title }
        val finalDesc = ogDesc.ifEmpty { metaDesc }

        val cleanText = stripHtmlTags(html)
        val emails = extractEmails(html + " " + cleanText, includeRoleEmails)
        val phones = extractPhones(html, cleanText)
        val socials = extractSocials(html)
        val links = extractLinks(html, baseUrl)
        val businessName = inferBusinessName(finalTitle, baseUrl)
        val address = extractAddress(html, cleanText)

        return ExtractedData(
            title = finalTitle,
            description = finalDesc,
            text = cleanText.take(150_000),
            businessName = businessName,
            emails = emails,
            phones = phones.map { it.first },
            phoneE164 = phones.map { it.second },
            socials = socials,
            address = address,
            links = links
        )
    }

    fun extractEmails(rawText: String, includeRole: Boolean = true): List<String> {
        val found = mutableSetOf<String>()

        // 1. Direct regex on clean text and HTML
        val matcher = EMAIL_REGEX.matcher(rawText)
        while (matcher.find()) {
            val email = matcher.group().lowercase().trim('.', ',', ';', ':', ')', '(', ']', '[', '<', '>', '"', '\'', ' ')
            if (email.contains("@") && email.contains(".") && !email.endsWith(".png") && !email.endsWith(".jpg") && !email.endsWith(".svg")) {
                val prefix = email.substringBefore("@")
                if (includeRole || prefix !in ROLE_EMAILS) {
                    found.add(email)
                }
            }
        }

        // 2. Explicit mailto: href targets
        val mailtoMatcher = MAILTO_REGEX.matcher(rawText)
        while (mailtoMatcher.find()) {
            val email = mailtoMatcher.group(1).lowercase().trim()
            if (email.contains("@") && email.contains(".")) {
                val prefix = email.substringBefore("@")
                if (includeRole || prefix !in ROLE_EMAILS) {
                    found.add(email)
                }
            }
        }

        return found.sorted()
    }

    fun extractPhones(rawHtml: String, cleanText: String): List<Pair<String, String>> {
        val results = mutableListOf<Pair<String, String>>()
        val seen = mutableSetOf<String>()

        // 1. Direct tel: links (highest accuracy)
        val telMatcher = TEL_REGEX.matcher(rawHtml)
        while (telMatcher.find()) {
            val raw = telMatcher.group(1).trim()
            val digits = raw.filter { it.isDigit() }
            if (digits.length in 7..15 && seen.add(digits)) {
                val formatted = if (raw.startsWith("+")) raw else "+$raw"
                results.add(Pair(raw, formatted))
            }
        }

        // 2. Candidate phone numbers in text
        val matcher = PHONE_CANDIDATE_REGEX.matcher(cleanText)
        while (matcher.find()) {
            val candidate = matcher.group().trim().trim('.', ',', ';', ':', ' ')
            val digitsOnly = candidate.filter { it.isDigit() }
            if (digitsOnly.length in 8..15 && seen.add(digitsOnly)) {
                val formatted = if (candidate.startsWith("+")) candidate else "+$candidate"
                results.add(Pair(candidate, formatted))
            }
        }
        return results.take(10)
    }

    fun extractSocials(text: String): Map<String, String> {
        val out = mutableMapOf<String, String>()
        for ((network, pattern) in SOCIAL_PATTERNS) {
            val matcher = pattern.matcher(text)
            if (matcher.find()) {
                val url = matcher.group().trim('"', '\'', ' ', '<', '>')
                out[network] = url
            }
        }
        return out
    }

    fun extractLinks(html: String, baseUrl: String): List<String> {
        val linkPattern = Pattern.compile("href=[\"']([^\"'#]+)[\"']", Pattern.CASE_INSENSITIVE)
        val matcher = linkPattern.matcher(html)
        val baseUri = try { URI(baseUrl) } catch (e: Exception) { null } ?: return emptyList()
        val results = mutableSetOf<String>()

        while (matcher.find()) {
            val raw = matcher.group(1).trim()
            if (raw.startsWith("javascript:", true) || raw.startsWith("mailto:", true) || raw.startsWith("tel:", true)) {
                continue
            }
            try {
                val resolved = baseUri.resolve(raw).toString()
                if (resolved.startsWith("http://", true) || resolved.startsWith("https://", true)) {
                    results.add(resolved)
                }
            } catch (e: Exception) {
                // Ignore unresolvable link
            }
        }
        return results.toList()
    }

    private fun extractTagContent(html: String, tag: String): String {
        val p = Pattern.compile("<$tag[^>]*>(.*?)</$tag>", Pattern.CASE_INSENSITIVE or Pattern.DOTALL)
        val m = p.matcher(html)
        return if (m.find()) stripHtmlTags(m.group(1)).trim() else ""
    }

    private fun extractMetaContent(html: String, name: String): String {
        val p = Pattern.compile("<meta[^>]+name=[\"']$name[\"'][^>]+content=[\"'](.*?)[\"']", Pattern.CASE_INSENSITIVE)
        val m = p.matcher(html)
        if (m.find()) return stripHtmlTags(m.group(1)).trim()

        val pRev = Pattern.compile("<meta[^>]+content=[\"'](.*?)[\"'][^>]+name=[\"']$name[\"']", Pattern.CASE_INSENSITIVE)
        val mRev = pRev.matcher(html)
        return if (mRev.find()) stripHtmlTags(mRev.group(1)).trim() else ""
    }

    private fun extractMetaProperty(html: String, prop: String): String {
        val p = Pattern.compile("<meta[^>]+property=[\"']$prop[\"'][^>]+content=[\"'](.*?)[\"']", Pattern.CASE_INSENSITIVE)
        val m = p.matcher(html)
        if (m.find()) return stripHtmlTags(m.group(1)).trim()

        val pRev = Pattern.compile("<meta[^>]+content=[\"'](.*?)[\"'][^>]+property=[\"']$prop[\"']", Pattern.CASE_INSENSITIVE)
        val mRev = pRev.matcher(html)
        return if (mRev.find()) stripHtmlTags(mRev.group(1)).trim() else ""
    }

    fun stripHtmlTags(html: String): String {
        var cleaned = html.replace(Regex("(?is)<script.*?</script>"), " ")
        cleaned = cleaned.replace(Regex("(?is)<style.*?</style>"), " ")
        cleaned = cleaned.replace(Regex("<[^>]+>"), " ")
        cleaned = cleaned.replace("&nbsp;", " ")
        cleaned = cleaned.replace("&amp;", "&")
        cleaned = cleaned.replace("&quot;", "\"")
        cleaned = cleaned.replace("&apos;", "'")
        cleaned = cleaned.replace("&lt;", "<")
        cleaned = cleaned.replace("&gt;", ">")
        return cleaned.replace(Regex("\\s+"), " ").trim()
    }

    private fun inferBusinessName(title: String, url: String): String {
        if (title.isNotEmpty()) {
            val parts = title.split(" – ", " — ", " - ", " | ", " • ", " : ")
            for (part in parts) {
                val candidate = part.trim()
                if (candidate.lowercase() !in GENERIC_TITLE_PREFIXES && candidate.length in 2..45) {
                    return candidate
                }
            }
        }
        return try {
            val host = URL(url).host.removePrefix("www.")
            val domainName = host.substringBefore(".")
            domainName.replaceFirstChar { it.uppercase() }
        } catch (e: Exception) {
            "Verified Organization"
        }
    }

    private fun extractAddress(html: String, text: String): String {
        // 1. Check <address> tag
        val addrTagContent = extractTagContent(html, "address")
        if (addrTagContent.isNotBlank() && addrTagContent.length in 10..120) {
            return addrTagContent
        }

        // 2. Pattern matching street + number in text
        val regex = Pattern.compile(
            "\\b\\d{1,5}\\s+[A-Za-z0-9.\\s]{3,35}\\s+(?:Street|St|Avenue|Ave|Road|Rd|Boulevard|Blvd|Drive|Dr|Suite|Ste|Way|Lane|Ln|Pkwy|Parkway)\\b[^\\n]{0,45}",
            Pattern.CASE_INSENSITIVE
        )
        val m = regex.matcher(text)
        return if (m.find()) m.group().trim() else ""
    }
}
