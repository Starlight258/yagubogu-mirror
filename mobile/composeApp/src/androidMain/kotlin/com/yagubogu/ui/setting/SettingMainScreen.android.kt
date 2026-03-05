package com.yagubogu.ui.setting

import android.graphics.Bitmap
import androidx.compose.material3.SnackbarHostState
import coil3.toUri
import co.touchlab.kermit.Logger
import coil3.Uri
import com.yagubogu.YaguBoguApplication
import com.yagubogu.ui.util.showSingleSnackbar
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.format
import id.zelory.compressor.constraint.quality
import id.zelory.compressor.constraint.resolution
import id.zelory.compressor.constraint.size
import kotlinx.coroutines.CoroutineScope
import org.jetbrains.compose.resources.getString
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.setting_edit_profile_image_processing_failed
import yagubogu.composeapp.generated.resources.setting_edit_profile_image_upload_failed
import java.io.File
import kotlin.coroutines.cancellation.CancellationException

actual suspend fun handleImagePickerKMPCroppedImage(
    snackBarScope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    sourceImageUri: Uri,
    onProfileImageUpload: suspend (Uri, String, Long) -> Result<Unit>
) {
    val context = YaguBoguApplication.instance.applicationContext
    runCatching {
        val originalFile = File(sourceImageUri.path ?: error("경로를 찾을 수 없음"))
        val convertedFile = Compressor.compress(context, originalFile) {
            resolution(500, 500)
            quality(90)
            format(Bitmap.CompressFormat.JPEG)
            size(5L * 1024L * 1024L)
        }

        // File 객체의 경로를 String으로 뽑아서 Coil의 Uri로 변환
        val convertedImageUri = convertedFile.absolutePath.toUri()
        val fileSize = convertedFile.length()
        val mimeType = "image/jpeg"

        onProfileImageUpload(convertedImageUri, mimeType, fileSize)
    }.fold(
        onSuccess = { result: Result<Unit> ->
            result.onFailure { e ->
                if (e is CancellationException) throw e
                snackbarHostState.showSingleSnackbar(
                    scope = snackBarScope,
                    message = getString(Res.string.setting_edit_profile_image_upload_failed),
                )
            }
        },
        onFailure = { e: Throwable ->
            if (e is CancellationException) throw e
            Logger.withTag("handleImagePickerKMPCroppedImage").e(e) { "프로필 이미지 전처리 실패" }
            snackbarHostState.showSingleSnackbar(
                scope = snackBarScope,
                message = getString(Res.string.setting_edit_profile_image_processing_failed),
            )
        },
    )
}