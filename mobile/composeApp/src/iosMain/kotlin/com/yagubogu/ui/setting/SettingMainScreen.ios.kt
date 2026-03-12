package com.yagubogu.ui.setting

import co.touchlab.kermit.Logger
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSDate
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.timeIntervalSince1970
import platform.Foundation.writeToFile
import platform.UIKit.UIGraphicsBeginImageContextWithOptions
import platform.UIKit.UIGraphicsEndImageContext
import platform.UIKit.UIGraphicsGetImageFromCurrentImageContext
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import kotlin.coroutines.cancellation.CancellationException

@OptIn(ExperimentalForeignApi::class)
actual suspend fun handleImagePickerKMPCroppedImage(
    onUploadFailure: () -> Unit,
    onProcessingFailure: () -> Unit,
    sourceImageUri: String,
    onProfileImageUpload: suspend (String, String, Long) -> Result<Unit>,
) {
    // Todo: 딸깍한 Ios 코드임, 구현 검증 필요
    runCatching {
        // 1. 이미지 로드
        // (file:// 로 시작하는 경우 URL 디코딩이 필요할 수 있으므로 NSURL을 거쳐서 path를 가져옴)
        val nsUrl = NSURL(string = sourceImageUri)
        val path = nsUrl.path ?: sourceImageUri.replaceFirst("file://", "")

        val originalImage = UIImage.imageWithContentsOfFile(path) ?: error("이미지를 불러올 수 없음: $path")

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

        val convertedImageUri = tempFilePath
        val fileSize = imageData.length.toLong()
        val mimeType = "image/jpeg"

        // 5. 서버 업로드 실행
        onProfileImageUpload(convertedImageUri, mimeType, fileSize)
    }.fold(
        onSuccess = { result ->
            result.onFailure { e ->
                if (e is CancellationException) throw e
                onUploadFailure()
            }
        },
        onFailure = { e ->
            if (e is CancellationException) throw e
            Logger.withTag("handleImagePickerKMPCroppedImage").e(e) { "iOS 이미지 전처리 실패" }
            onProcessingFailure()
        },
    )
}
