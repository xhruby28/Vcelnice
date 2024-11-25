package com.hruby.sharedresources.helpers

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException

object ImageHelper {
    // Vytvoří cestu k obrázku v interním uložišti
    fun getImageFile(context: Context, fileName: String): File {
        val imagesDir = File(context.filesDir, "images") // Složka pro obrázky
        if (!imagesDir.exists()) {
            imagesDir.mkdirs() // Vytvoření složky, pokud neexistuje
        }
        return File(imagesDir, fileName)
    }

    // Zkontroluje dostupnost interního uložiště
    private fun isStorageAvailable(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }

    // Uloží obrázek do interní paměti aplikace
    fun saveImageToInternalStorage(context: Context, uri: Uri): String? {
        return try {
            if (!isStorageAvailable()) {
                throw IOException("Interní úložiště není dostupné.")
            }

            val inputStream = context.contentResolver.openInputStream(uri) ?: throw IOException("Nelze otevřít stream pro URI.")
            val fileName = "stanoviste_${System.currentTimeMillis()}.jpg"
            val file = getImageFile(context, fileName)

            file.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }

            file.relativePath(context)?.takeIf { file.exists() }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Funkce pro vyfocení obrázku a jeho uložení
    fun captureImage(context: Context): Uri? {
        return try {
            val fileName = "stanoviste_${System.currentTimeMillis()}.jpg"
            val file = getImageFile(context, fileName)

            if (!file.exists()) {
                file.createNewFile()
            }
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Funkce pro otevření galerie a vybrání obrázku
    fun pickImageFromGallery(launcher: ActivityResultLauncher<String>) {
        launcher.launch("image/*")
    }

    fun deleteImageFromInternalStorage(filePath: String): Boolean {
        val file = File(filePath)
        return file.exists() && file.delete()
    }

    private fun File.relativePath(context: Context): String? {
        val imagesDir = File(context.filesDir, "images")
        return if (absolutePath.startsWith(imagesDir.absolutePath)) {
            "images/${name}"
        } else {
            null
        }
    }
}