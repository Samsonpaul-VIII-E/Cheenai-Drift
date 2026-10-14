package com.example

import com.example.leadforge.engine.DataExtractor
import com.example.leadforge.engine.FastFingerprint
import com.example.leadforge.engine.FastScorer
import com.example.leadforge.model.ExtractedData
import com.example.leadforge.security.SsrfValidator
import com.example.leadforge.security.ValidationResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class LeadForgeCoreUnitTest {

    @Test
    fun testFastFingerprintDeterministic() {
        val fp1 = FastFingerprint.compute("Acme Corp", "info@acme.com", "+12025550199", "https://acme.com")
        val fp2 = FastFingerprint.compute("acme corp", "info@acme.com", "+12025550199", "https://acme.com")
        assertEquals("Fingerprint must be case-insensitive and deterministic", fp1, fp2)
        assertEquals(32, fp1.length)
    }

    @Test
    fun testDataExtractorEmailsAndPhones() {
        val sampleHtml = """
            <html>
                <head>
                    <title>Acme Global - Cloud Solutions</title>
                    <meta name="description" content="Leading enterprise cloud and data architecture." />
                </head>
                <body>
                    <h1>Welcome to Acme Global</h1>
                    <p>Contact our leadership team at hello@acmepublic.org or direct line +1-555-019-2831.</p>
                    <a href="https://linkedin.com/company/acme-global">LinkedIn</a>
                    <a href="https://x.com/acmeglobal">X Profile</a>
                </body>
            </html>
        """.trimIndent()

        val extracted = DataExtractor.extract(sampleHtml, "https://acmepublic.org")

        assertEquals("Acme Global - Cloud Solutions", extracted.title)
        assertEquals("Leading enterprise cloud and data architecture.", extracted.description)
        assertTrue(extracted.emails.contains("hello@acmepublic.org"))
        assertTrue(extracted.socials.containsKey("linkedin"))
        assertEquals("https://linkedin.com/company/acme-global", extracted.socials["linkedin"])
        assertEquals("https://x.com/acmeglobal", extracted.socials["x"])
    }

    @Test
    fun testRoleEmailFiltering() {
        val text = "Contact sales@test.com and founder.john@test.com for details"
        val withRole = DataExtractor.extractEmails(text, includeRole = true)
        assertTrue(withRole.contains("sales@test.com"))
        assertTrue(withRole.contains("founder.john@test.com"))

        val withoutRole = DataExtractor.extractEmails(text, includeRole = false)
        assertFalse(withoutRole.contains("sales@test.com"))
        assertTrue(withoutRole.contains("founder.john@test.com"))
    }

    @Test
    fun testLeadScoringWeights() {
        val fullData = ExtractedData(
            title = "Apex Data",
            description = "AI and cloud platform",
            text = "Enterprise AI intelligence and automated cloud infrastructure solutions.",
            businessName = "Apex Data",
            emails = listOf("contact@apex.io"),
            phones = listOf("+15551234"),
            socials = mapOf("linkedin" to "https://linkedin.com/company/apex"),
            address = "100 Market St, San Francisco, CA"
        )

        val score = FastScorer.score(fullData, listOf("AI", "cloud", "infrastructure"))
        assertTrue("Score should be high for verified multi-channel lead with keyword hits", score.totalScore >= 75.0)
        assertEquals("HIGH CONFIDENCE", score.confidence)
        assertTrue(score.matchedKeywords.contains("AI"))
    }

    @Test
    fun testSsrfShieldBlocksLoopbackAndPrivate() = runBlocking {
        val loopback = SsrfValidator.validate("http://127.0.0.1:8080/admin")
        assertTrue(loopback is ValidationResult.Rejected)

        val localhost = SsrfValidator.validate("http://localhost/secret")
        assertTrue(localhost is ValidationResult.Rejected)

        val fileScheme = SsrfValidator.validate("file:///etc/passwd")
        assertTrue(fileScheme is ValidationResult.Rejected)

        val privateIp = SsrfValidator.validate("http://192.168.1.100")
        assertTrue(privateIp is ValidationResult.Rejected)

        val metadataIp = SsrfValidator.validate("http://169.254.169.254/computeMetadata/v1/")
        assertTrue(metadataIp is ValidationResult.Rejected)
    }

    @Test
    fun testBulkPreflightSanitizationAndDeduplication() = runBlocking {
        val rawInput = """
            https://wikimediafoundation.org
            https://www.eff.org
            wikimediafoundation.org
            http://127.0.0.1/admin
            https://www.eff.org/about
            https://www.python.org/psf
        """.trimIndent()

        val lines = rawInput.lines().map { it.trim() }.filter { it.isNotBlank() }
        val seenDomains = mutableSetOf<String>()
        var duplicates = 0
        var ssrfBlocked = 0
        val valid = mutableListOf<String>()

        for (token in lines) {
            val candidate = if (token.startsWith("http")) token else "https://$token"
            val host = java.net.URI(candidate).host?.lowercase() ?: ""
            if (seenDomains.contains(host)) {
                duplicates++
                continue
            }
            val res = SsrfValidator.validate(candidate)
            if (res !is ValidationResult.Valid) {
                ssrfBlocked++
                continue
            }
            seenDomains.add(host)
            valid.add(candidate)
        }

        assertEquals(3, valid.size)
        assertEquals(2, duplicates)
        assertEquals(1, ssrfBlocked)
    }

    @Test
    fun testWebhookPayloadGeneration() {
        val lead = com.example.leadforge.data.entity.LeadEntity(
            fingerprint = "fp_test_lead_123",
            url = "https://acme.org",
            domain = "acme.org",
            name = "Acme Global Foundation",
            category = "Nonprofit",
            email = "contact@acme.org",
            emails = "contact@acme.org; press@acme.org",
            phone = "+1-555-0199",
            phoneE164 = "+15550199",
            keywordScore = 92.5
        )

        val jsonString = com.example.leadforge.webhook.WebhookDispatcher.buildLeadPayload(lead)
        val json = org.json.JSONObject(jsonString)

        assertEquals("lead.extracted", json.getString("event"))
        assertEquals("acme.org", json.getString("domain"))
        assertEquals("Acme Global Foundation", json.getString("company_name"))
        assertEquals("TIER_A", json.getString("tier"))
        assertEquals(92.5, json.getDouble("score"), 0.01)
        assertEquals("contact@acme.org", json.getString("primary_email"))
        assertEquals(2, json.getJSONArray("emails").length())
    }

    @Test
    fun testWebhookThresholdEvaluation() {
        val tierAThreshold = com.example.leadforge.model.WebhookTierThreshold.TIER_A_ONLY
        val tierABThreshold = com.example.leadforge.model.WebhookTierThreshold.TIERS_A_AND_B
        val allThreshold = com.example.leadforge.model.WebhookTierThreshold.ALL_LEADS

        assertTrue(92.0 >= tierAThreshold.minScore)
        assertFalse(85.0 >= tierAThreshold.minScore)
        assertTrue(72.0 >= tierABThreshold.minScore)
        assertFalse(65.0 >= tierABThreshold.minScore)
        assertTrue(10.0 >= allThreshold.minScore)
    }
}
