package com.yagubogu.ui.setting

import androidx.compose.runtime.Composable
import co.touchlab.kermit.Logger
import com.yagubogu.ui.util.UiText
import io.github.ismoy.imagepickerkmp.domain.config.CameraCaptureConfig
import io.github.ismoy.imagepickerkmp.domain.config.CropConfig
import io.github.ismoy.imagepickerkmp.domain.models.CompressionLevel
import io.github.ismoy.imagepickerkmp.domain.models.GalleryPhotoResult
import io.github.ismoy.imagepickerkmp.domain.models.MimeType.Companion.ALL_SUPPORTED_TYPES
import io.github.ismoy.imagepickerkmp.presentation.ui.components.GalleryPickerLauncher
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.setting_edit_profile_image_selection_failed

@Composable
fun ProfileImagePicker(
    onPhotosSelected: (String) -> Unit,
    onError: (UiText) -> Unit,
    onClosePicker: () -> Unit,
) {
    val logger: Logger = Logger.withTag("ProfileImagePicker")
    logger.d { "ProfileImagePicker 열림" }
    GalleryPickerLauncher(
        allowMultiple = false,
        mimeTypes = ALL_SUPPORTED_TYPES,
        onPhotosSelected = { photos: List<GalleryPhotoResult> ->
            logger.d { "onPhotosSelected, 사진 개수: ${photos.size}" }
            onClosePicker()

            val photo: GalleryPhotoResult? = photos.firstOrNull()
            if (photo == null) {
                logger.w { "선택된 사진이 없습니다" }
                return@GalleryPickerLauncher
            }
            onPhotosSelected(photo.uri)
            onClosePicker()
        },
        onError = { exception: Exception ->
            logger.e(exception) { "GalleryPicker 에러 발생" }
            onError(UiText.StringRes(Res.string.setting_edit_profile_image_selection_failed))
            onClosePicker()
        },
        onDismiss = {
            logger.d { "GalleryPicker 닫힘" }
            onClosePicker()
        },
        enableCrop = true,
        cameraCaptureConfig =
            CameraCaptureConfig(
                compressionLevel = CompressionLevel.HIGH,
                cropConfig =
                    CropConfig(
                        enabled = true,
                        aspectRatioLocked = true,
                        circularCrop = true,
                        squareCrop = false,
                        freeformCrop = false,
                    ),
            ),
    )
}
