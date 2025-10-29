package com.example.expensetrackerkotlin.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class FileManager(private val context: Context) {

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")

    suspend fun createExportFile(jsonContent: String): Result<File> {
        return withContext(Dispatchers.IO) {
            try {
                val timestamp = LocalDateTime.now().format(dateFormatter)
                val fileName = "expense_tracker_backup_$timestamp.json"

                // Create file in app's cache directory
                val file = File(context.cacheDir, fileName)
                file.writeText(jsonContent)

                Result.success(file)
            } catch (e: Exception) {
                Result.failure(Exception("Failed to create export file: ${e.message}", e))
            }
        }
    }

    fun shareFile(file: File): Intent {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Expense Tracker Backup")
            putExtra(Intent.EXTRA_TEXT, "Backup file created on ${LocalDateTime.now()}")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        return Intent.createChooser(shareIntent, "Share backup file")
    }

    suspend fun readImportFile(uri: Uri): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                    ?: return@withContext Result.failure(Exception("Failed to open file"))

                val content = inputStream.bufferedReader().use { it.readText() }
                inputStream.close()

                Result.success(content)
            } catch (e: Exception) {
                Result.failure(Exception("Failed to read import file: ${e.message}", e))
            }
        }
    }

    fun cleanupExportFile(file: File) {
        try {
            if (file.exists()) {
                file.delete()
            }
        } catch (e: Exception) {
            // Silently fail - it's just cleanup
        }
    }
}
