package com.example.leadforge.engine

import com.example.leadforge.model.ExtractedData
import com.example.leadforge.model.ScoreBreakdown

object FastScorer {

    /**
     * Calculates lead intelligence score (0 to 100) and provides transparent signal breakdowns.
     * Higher weights given to actionable contact points and relevant target keywords.
     */
    fun score(data: ExtractedData, targetKeywords: List<String>): ScoreBreakdown {
        var emailPts = 0.0
        if (data.emails.isNotEmpty()) {
            emailPts = 25.0
            if (data.emails.size > 1) emailPts += 5.0
        }

        var phonePts = 0.0
        if (data.phones.isNotEmpty()) {
            phonePts = 20.0
        }

        var websitePts = 0.0
        if (data.businessName.isNotBlank() && data.businessName != "Enterprise Entity") {
            websitePts = 10.0
        }

        var socialPts = 0.0
        if (data.socials.isNotEmpty()) {
            socialPts = (data.socials.size * 5.0).coerceAtMost(15.0)
        }

        var addressPts = 0.0
        if (data.address.isNotBlank()) {
            addressPts = 10.0
        }

        var keywordPts = 0.0
        val matchedKeywords = mutableListOf<String>()
        val combinedText = "${data.title} ${data.description} ${data.text}".lowercase()

        for (kw in targetKeywords) {
            val cleanKw = kw.trim().lowercase()
            if (cleanKw.isNotEmpty() && combinedText.contains(cleanKw)) {
                matchedKeywords.add(kw.trim())
                keywordPts += 10.0
            }
        }
        keywordPts = keywordPts.coerceAtMost(25.0)

        // Multi-signal synergy bonus: if both email and phone are present
        var bonus = 0.0
        if (data.emails.isNotEmpty() && data.phones.isNotEmpty()) {
            bonus = 10.0
        }

        val total = (emailPts + phonePts + websitePts + socialPts + addressPts + keywordPts + bonus).coerceAtMost(100.0)

        val confidence = when {
            total >= 75.0 -> "HIGH CONFIDENCE"
            total >= 45.0 -> "MODERATE"
            else -> "LOW SIGNAL"
        }

        return ScoreBreakdown(
            totalScore = total,
            emailSignal = emailPts,
            phoneSignal = phonePts,
            websiteSignal = websitePts,
            socialSignal = socialPts,
            keywordSignal = keywordPts,
            addressSignal = addressPts,
            matchedKeywords = matchedKeywords,
            confidence = confidence
        )
    }
}
