package com.example.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

object ImageStorageHelper {

    private const val DESTINATIONS_DIR = "destinations_media"

    /**
     * Copies a Uri selected from PhotoPicker / Gallery into the application's
     * internal storage directory, making it accessible offline and durable across restarts.
     * Returns the absolute file path as a URI string.
     */
    fun saveImageToInternalStorage(context: Context, sourceUri: Uri): String? {
        return try {
            val destinationFolder = File(context.filesDir, DESTINATIONS_DIR).apply {
                if (!exists()) mkdirs()
            }
            val fileName = "dest_${UUID.randomUUID()}_${System.currentTimeMillis()}.jpg"
            val targetFile = File(destinationFolder, fileName)

            val inputStream: InputStream? = context.contentResolver.openInputStream(sourceUri)
            val outputStream = FileOutputStream(targetFile)

            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }

            // Return file uri string suitable for Coil
            Uri.fromFile(targetFile).toString()
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback to the original Uri string if local save fails
            sourceUri.toString()
        }
    }

    /**
     * Check if a given image path is valid
     */
    fun isValidImagePath(path: String?): Boolean {
        if (path.isNullOrBlank()) return false
        return if (path.startsWith("file://")) {
            val file = File(Uri.parse(path).path ?: "")
            file.exists() && file.length() > 0
        } else {
            true // Network url, content uri, or drawable resource string
        }
    }
}
