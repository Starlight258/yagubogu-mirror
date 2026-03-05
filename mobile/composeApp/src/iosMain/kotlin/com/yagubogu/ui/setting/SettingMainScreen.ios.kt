package com.yagubogu.ui.setting

import androidx.compose.material3.SnackbarHostState
import co.touchlab.kermit.Logger
import coil3.Uri
import coil3.toUri
import com.yagubogu.ui.util.showSingleSnackbar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import org.jetbrains.compose.resources.getString
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSDate
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.timeIntervalSince1970
import platform.Foundation.writeToFile
import platform.UIKit.UIGraphicsBeginImageContextWithOptions
import platform.UIKit.UIGraphicsEndImageContext
import platform.UIKit.UIGraphicsGetImageFromCurrentImageContext
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.setting_edit_profile_image_processing_failed
import yagubogu.composeapp.generated.resources.setting_edit_profile_image_upload_failed
import kotlin.coroutines.cancellation.CancellationException

@OptIn(ExperimentalForeignApi::class)
actual suspend fun handleImagePickerKMPCroppedImage(
    snackBarScope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    sourceImageUri: Uri,
    onProfileImageUpload: suspend (Uri, String, Long) -> Result<Unit>
) {
    // Todo: 딸깍한 Ios 코드임, 구현 검증 필요
    runCatching {
        // 1. 이미지 로드
        val path = sourceImageUri.path ?: error("경로를 찾을 수 없음")
        val originalImage = UIImage.imageWithContentsOfFile(path) ?: error("이미지를 불러올 수 없음")

        // 2. 리사이징 (500x500)
        val newSize = CGSizeMake(500.0, 500.0)
        UIGraphicsBeginImageContextWithOptions(newSize, false, 1.0)
        originalImage.drawInRect(CGRectMake(0.0, 0.0, 500.0, 500.0))
        val resizedImage = UIGraphicsGetImageFromCurrentImageContext()
        UIGraphicsEndImageContext()

        if (resizedImage == null) error("이미지 리사이징 실패")

        // 3. JPEG 압축 (화질 0.9)
        val imageData = UIImageJPEGRepresentation(resizedImage, 0.9) ?: error("이미지 압축 실패")

        // 4. 임시 파일로 저장 (파일 크기 및 URI 획득을 위함)
        val tempFileName = "profile_${NSDate().timeIntervalSince1970}.jpg"
        val tempFilePath = NSTemporaryDirectory() + tempFileName

        // NSData를 물리적 파일로 저장
        imageData.writeToFile(tempFilePath, true)

        val convertedImageUri = tempFilePath.toUri()
        val fileSize = imageData.length.toLong()
        val mimeType = "image/jpeg"

        // 5. 서버 업로드 실행
        onProfileImageUpload(convertedImageUri, mimeType, fileSize)
    }.fold(
        onSuccess = { result ->
            result.onFailure { e ->
                if (e is CancellationException) throw e
                snackbarHostState.showSingleSnackbar(
                    scope = snackBarScope,
                    message = getString(Res.string.setting_edit_profile_image_upload_failed),
                )
            }
        },
        onFailure = { e ->
            if (e is CancellationException) throw e
            Logger.withTag("handleImagePickerKMPCroppedImage").e(e) { "iOS 이미지 전처리 실패" }
            snackbarHostState.showSingleSnackbar(
                scope = snackBarScope,
                message = getString(Res.string.setting_edit_profile_image_processing_failed),
            )
        }
    )
}