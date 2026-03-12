package com.yagubogu.ui.setting

import android.app.Application
import android.graphics.Bitmap
import androidx.core.net.toUri
import co.touchlab.kermit.Logger
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.format
import id.zelory.compressor.constraint.quality
import id.zelory.compressor.constraint.resolution
import id.zelory.compressor.constraint.size
import org.koin.core.context.GlobalContext
import java.io.File
import kotlin.coroutines.cancellation.CancellationException

actual suspend fun handleImagePickerKMPCroppedImage(
    onUploadFailure: () -> Unit,
    onProcessingFailure: () -> Unit,
    sourceImageUri: String,
    onProfileImageUpload: suspend (String, String, Long) -> Result<Unit>
) {
    val context = GlobalContext.get().get<Application>()
    runCatching {
        val uri = sourceImageUri.toUri()
        val originalFile = File(uri.path ?: error("경로를 찾을 수 없음"))
        val convertedFile = Compressor.compress(context, originalFile) {
            resolution(500, 500)
            quality(90)
            format(Bitmap.CompressFormat.JPEG)
            size(5L * 1024L * 1024L)
        }

        // File 객체의 경로를 String으로 뽑아서 Coil의 Uri로 변환
        val convertedImageUri = convertedFile.absolutePath
        val fileSize = convertedFile.length()
        val mimeType = "image/jpeg"

        onProfileImageUpload(convertedImageUri, mimeType, fileSize)
    }.fold(
        onSuccess = { result: Result<Unit> ->
            result.onFailure { e ->
                if (e is CancellationException) throw e
                onUploadFailure()
            }
        },
        onFailure = { e: Throwable ->
            if (e is CancellationException) throw e
            Logger.withTag("handleImagePickerKMPCroppedImage").e(e) { "프로필 이미지 전처리 실패" }
            onProcessingFailure()
        },
    )
}
