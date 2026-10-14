package com.example.leadforge.export

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.leadforge.data.entity.LeadEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import org.json.JSONArray
import org.json.JSONObject

object Exporter {

    fun exportToCsv(context: Context, leads: List<LeadEntity>): File {
        val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val fileName = "leadforge_export_$stamp.csv"
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(exportDir, fileName)

        FileOutputStream(file).bufferedWriter(Charsets.UTF_8).use { writer ->
            // UTF-8 BOM for Excel compatibility
            writer.write("\uFEFF")

            val headers = listOf(
                "Name", "Domain", "Score", "Email", "Emails_All",
                "Phone", "Phone_E164", "Address", "Category", "Title",
                "Description", "Keywords_Matched", "LinkedIn", "X_Twitter",
                "Facebook", "Instagram", "URL", "Fingerprint", "Discovered_At"
            )
            writer.write(headers.joinToString(",") { escapeCsv(it) })
            writer.newLine()

            for (lead in leads) {
                val row = listOf(
                    lead.name,
                    lead.domain,
                    lead.keywordScore.toInt().toString(),
                    lead.email,
                    lead.emails,
                    lead.phone,
                    lead.phoneE164,
                    lead.address,
                    lead.category,
                    lead.title,
                    lead.description,
                    lead.keywordsFound,
                    lead.linkedin,
                    lead.xTwitter,
                    lead.facebook,
                    lead.instagram,
                    lead.url,
                    lead.fingerprint,
                    SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date(lead.discoveredAt))
                )
                writer.write(row.joinToString(",") { escapeCsv(it) })
                writer.newLine()
            }
        }
        return file
    }

    fun exportToJson(context: Context, leads: List<LeadEntity>): File {
        val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val fileName = "leadforge_export_$stamp.json"
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(exportDir, fileName)

        val jsonArray = JSONArray()
        for (lead in leads) {
            val obj = JSONObject().apply {
                put("fingerprint", lead.fingerprint)
                put("name", lead.name)
                put("domain", lead.domain)
                put("url", lead.url)
                put("score", lead.keywordScore)
                put("email", lead.email)
                put("emails", lead.emails)
                put("phone", lead.phone)
                put("phone_e164", lead.phoneE164)
                put("address", lead.address)
                put("category", lead.category)
                put("title", lead.title)
                put("description", lead.description)
                put("keywords_found", lead.keywordsFound)
                put("linkedin", lead.linkedin)
                put("x_twitter", lead.xTwitter)
                put("facebook", lead.facebook)
                put("instagram", lead.instagram)
                put("discovered_at", lead.discoveredAt)
                put("source", lead.source)
            }
            jsonArray.put(obj)
        }

        file.writeText(jsonArray.toString(2), Charsets.UTF_8)
        return file
    }

    fun shareExportFile(context: Context, file: File, mimeType: String) {
        val uri = try {
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            android.net.Uri.fromFile(file)
        }

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "LeadForge X Export: ${file.name}")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share LeadForge Export").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }

    private fun escapeCsv(text: String): String {
        val escaped = text.replace("\"", "\"\"")
        return if (escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n") || escaped.contains("\r")) {
            "\"$escaped\""
        } else {
            escaped
        }
    }
}
