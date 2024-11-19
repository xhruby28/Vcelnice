package com.hruby.sharedresources.helpers

import android.content.Context
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.FileProvider
import java.io.File

object ImageHelper {
    // Uložit obrázek do interní paměti aplikace
    fun saveImageToInternalStorage(context: Context, uri: Uri): String? {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val file = File(context.filesDir, "stanoviste_${System.currentTimeMillis()}.jpg")
            val outputStream = file.outputStream()
            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            return file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    // Funkce pro vyfocení obrázku a jeho uložení
    fun captureImage(context: Context): Uri {
        val file = File(context.filesDir, "stanoviste_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    // Funkce pro otevření galerie a vybrání obrázku
    fun pickImageFromGallery(launcher: ActivityResultLauncher<String>) {
        launcher.launch("image/*")
    }

    fun deleteImageFromInternalStorage(filePath: String): Boolean {
        val file = File(filePath)
        return if (file.exists()) {
            file.delete()
        } else {
            false
        }
    }
}