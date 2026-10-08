package cr.una.delta.frontend_kode.presentation.ui.components.cameraUtils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun rememberCameraLauncher(
    context: Context,
    onImageCaptured: (File) -> Unit,
    onError: (Exception) -> Unit
): () -> Unit {
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var imageFile by remember { mutableStateOf<File?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && imageFile != null) {
            onImageCaptured(imageFile!!)
        } else {
            onError(Exception("Captura cancelada o falló"))
        }
    }

    return {
        try {
            val file = createImageFile(context)
            imageFile = file
            imageUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            launcher.launch(imageUri!!)
        } catch (e: Exception) {
            onError(e)
        }
    }
}

/**
 * Reduce y recomprime la foto para que pese poco (evita el límite de subida y
 * acelera el OCR). Si algo falla, devuelve el archivo original.
 */
fun compressImage(source: File, maxDim: Int = 1280, quality: Int = 80): File {
    return try {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(source.absolutePath, bounds)
        var sample = 1
        val largest = maxOf(bounds.outWidth, bounds.outHeight)
        while (largest > 0 && largest / sample > maxDim) sample *= 2
        val opts = BitmapFactory.Options().apply { inSampleSize = sample }
        val bmp = BitmapFactory.decodeFile(source.absolutePath, opts) ?: return source
        val out = File(source.parentFile, "cmp_${source.name}")
        FileOutputStream(out).use { bmp.compress(Bitmap.CompressFormat.JPEG, quality, it) }
        bmp.recycle()
        if (out.length() in 1 until source.length()) out else source
    } catch (e: Exception) {
        source
    }
}

private fun createImageFile(context: Context): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val storageDir = context.cacheDir
    return File.createTempFile(
        "JPEG_${timeStamp}_",
        ".jpg",
        storageDir
    )
}