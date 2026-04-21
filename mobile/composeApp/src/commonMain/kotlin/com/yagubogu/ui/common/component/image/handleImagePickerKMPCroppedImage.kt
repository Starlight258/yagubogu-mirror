package com.yagubogu.ui.common.component.image

/**
 * ImagePickerKMP로 자른 이미지를
 * 백엔드에서 요구하는 프로파일 이미지 규격(jpeg, 5mb)으로 컨버팅하여 업로드합니다.
 */
expect suspend fun handleImagePickerKMPCroppedImage(
    onUploadFailure: () -> Unit,
    onProcessingFailure: () -> Unit,
    sourceImageUri: String,
    onProfileImageUpload: suspend (String, String, Long) -> Result<Unit>,
)
