package com.habitmind.data.media

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Helper class for managing local media files
 * Handles storage for images, voice notes, and other media
 */
class MediaStorageHelper(private val context: Context) {
    
    companion object {
        private const val IMAGES_DIR = "journal_images"
        private const val VOICE_DIR = "voice_notes"
        private const val EXPORT_DIR = "exports"
    }
    
    // Get or create directory for images
    fun getImagesDirectory(): File {
        val dir = File(context.filesDir, IMAGES_DIR)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }
    
    // Get or create directory for voice notes
    fun getVoiceDirectory(): File {
        val dir = File(context.filesDir, VOICE_DIR)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }
    
    // Get or create directory for exports
    fun getExportDirectory(): File {
        val dir = File(context.filesDir, EXPORT_DIR)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }
    
    // Generate unique filename for image
    fun generateImageFilename(): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        return "IMG_$timestamp.jpg"
    }
    
    // Generate unique filename for voice note
    fun generateVoiceFilename(): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        return "VOICE_$timestamp.m4a"
    }
    
    // Create a new image file
    fun createImageFile(): File {
        return File(getImagesDirectory(), generateImageFilename())
    }
    
    // Create a new voice note file
    fun createVoiceFile(): File {
        return File(getVoiceDirectory(), generateVoiceFilename())
    }
    
    // Copy URI content to local storage
    suspend fun copyUriToLocalStorage(uri: Uri, targetDir: File, filename: String): File? {
        return try {
            val targetFile = File(targetDir, filename)
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            }
            targetFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    // Delete a media file
    fun deleteMediaFile(path: String): Boolean {
        return try {
            File(path).delete()
        } catch (e: Exception) {
            false
        }
    }
    
    // Get file size in bytes
    fun getFileSize(path: String): Long {
        return try {
            File(path).length()
        } catch (e: Exception) {
            0L
        }
    }
    
    // Check if file exists
    fun fileExists(path: String): Boolean {
        return File(path).exists()
    }
    
    // Get all image files
    fun getAllImages(): List<File> {
        return getImagesDirectory().listFiles()?.toList() ?: emptyList()
    }
    
    // Get all voice notes
    fun getAllVoiceNotes(): List<File> {
        return getVoiceDirectory().listFiles()?.toList() ?: emptyList()
    }
    
    // Calculate total storage used
    fun getTotalStorageUsed(): Long {
        val imagesSize = getAllImages().sumOf { it.length() }
        val voiceSize = getAllVoiceNotes().sumOf { it.length() }
        return imagesSize + voiceSize
    }
    
    // Format storage size for display
    fun formatStorageSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
            else -> "${bytes / (1024 * 1024 * 1024)} GB"
        }
    }
}
