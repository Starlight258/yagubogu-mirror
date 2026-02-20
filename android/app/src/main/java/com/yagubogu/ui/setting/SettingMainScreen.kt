package com.yagubogu.ui.setting

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.yagubogu.R
import com.yagubogu.data.repository.member.NicknameUpdateError
import com.yagubogu.ui.common.component.profile.ProfileImage
import com.yagubogu.ui.setting.component.SettingButton
import com.yagubogu.ui.setting.component.SettingButtonGroup
import com.yagubogu.ui.setting.component.dialog.NicknameEditDialog
import com.yagubogu.ui.setting.model.MemberInfoItem
import com.yagubogu.ui.setting.model.SettingEvent
import com.yagubogu.ui.theme.Gray050
import com.yagubogu.ui.theme.Gray400
import com.yagubogu.ui.theme.Gray500
import com.yagubogu.ui.theme.PretendardMedium12
import com.yagubogu.ui.theme.PretendardRegular12
import com.yagubogu.ui.theme.PretendardSemiBold
import com.yagubogu.ui.theme.White
import com.yagubogu.ui.util.LocalSnackbarHostState
import com.yagubogu.ui.util.showSingleSnackbar
import com.yagubogu.ui.util.yyyyMMddFormatter
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.format
import id.zelory.compressor.constraint.quality
import id.zelory.compressor.constraint.resolution
import id.zelory.compressor.constraint.size
import io.github.ismoy.imagepickerkmp.domain.config.CameraCaptureConfig
import io.github.ismoy.imagepickerkmp.domain.config.CropConfig
import io.github.ismoy.imagepickerkmp.domain.models.CompressionLevel
import io.github.ismoy.imagepickerkmp.domain.models.GalleryPhotoResult
import io.github.ismoy.imagepickerkmp.domain.models.MimeType.Companion.ALL_SUPPORTED_TYPES
import io.github.ismoy.imagepickerkmp.presentation.ui.components.GalleryPickerLauncher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.format
import org.koin.compose.viewmodel.koinViewModel
import timber.log.Timber
import java.io.File
import kotlin.coroutines.cancellation.CancellationException

@Composable
fun SettingMainScreen(
    onSettingAccountClick: () -> Unit,
    onFavoriteTeamEditClick: () -> Unit,
    onFullScreenMode: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingViewModel = koinViewModel(),
) {
    val snackbarHostState = LocalSnackbarHostState.current
    val context: Context = LocalContext.current
    val resources: Resources = LocalResources.current
    val scope: CoroutineScope = rememberCoroutineScope()
    val memberInfoItem: State<MemberInfoItem> =
        viewModel.myMemberInfoItem.collectAsStateWithLifecycle(MemberInfoItem())

    var showNicknameEditDialog: Boolean by rememberSaveable { mutableStateOf(false) }

    val settingEvent: SettingEvent? by viewModel.settingEvent.collectAsStateWithLifecycle(null)

    var showGallery by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(showGallery) {
        onFullScreenMode(showGallery)
    }

    LaunchedEffect(Unit) {
        viewModel.fetchMemberInfo()
    }

    LaunchedEffect(settingEvent) {
        when (val event = settingEvent) {
            is SettingEvent.NicknameEditSuccess -> {
                val message =
                    resources.getString(
                        R.string.setting_edited_nickname_alert,
                        (settingEvent as SettingEvent.NicknameEditSuccess).newNickname,
                    )
                snackbarHostState.showSingleSnackbar(
                    scope = this,
                    message = message,
                )
            }

            is SettingEvent.NicknameEditFailure -> {
                val errorMessage = event.error.asString(context)
                snackbarHostState.showSingleSnackbar(
                    scope = this,
                    message = errorMessage,
                )
            }

            else -> Unit
        }
    }

    SettingMainScreen(
        onClickSettingAccount = onSettingAccountClick,
        onFavoriteTeamEditClick = onFavoriteTeamEditClick,
        onNicknameEdit = { showNicknameEditDialog = true },
        onProfileImageUpload = {
            showGallery = true
        },
        memberInfoItem = memberInfoItem.value,
        appVersion = context.getAppVersion(),
        modifier = modifier,
    )

    if (showGallery) {
        ProfileImagePicker(context, scope, viewModel::uploadProfileImage, onClosePicker = { showGallery = false })
    }

    if (showNicknameEditDialog) {
        NicknameEditDialog(
            nickname =
                viewModel.myMemberInfoItem
                    .collectAsState()
                    .value.nickName,
            onConfirm = { nickname ->
                viewModel.updateNickname(nickname)
                showNicknameEditDialog = false
            },
            onCancel = { showNicknameEditDialog = false },
        )
    }
}

@Composable
private fun ProfileImagePicker(
    context: Context,
    scope: CoroutineScope,
    onUpload: suspend (Uri, String, Long) -> Result<Unit>,
    onClosePicker: () -> Unit,
) {
    val snackbarHostState = LocalSnackbarHostState.current
    val activity = context as? ComponentActivity
    when (activity is ComponentActivity) {
        true -> {
            GalleryPickerLauncher(
                allowMultiple = false,
                mimeTypes = ALL_SUPPORTED_TYPES,
                onPhotosSelected = { photos: List<GalleryPhotoResult> ->
                    Timber.d("onPhotosSelected, 사진 개수: ${photos.size}")
                    onClosePicker()

                    val photo: GalleryPhotoResult? = photos.firstOrNull()
                    if (photo == null) {
                        Timber.w("선택된 사진이 없습니다")
                        return@GalleryPickerLauncher
                    }
                    scope.launch {
                        runCatching {
                            handleImagePickerKMPCroppedImage(
                                context = context,
                                snackBarScope = scope,
                                snackbarHostState = snackbarHostState,
                                sourceImageUri =
                                    if (photo.uri.startsWith("file://")) {
                                        photo.uri.toUri()
                                    } else {
                                        File(photo.uri).toUri()
                                    },
                                onProfileImageUpload = onUpload,
                            )
                        }.getOrElse { exception: Throwable ->
                            Timber.e(exception, "이미지 처리 중 예외 발생")
                            snackbarHostState.showSingleSnackbar(
                                scope = scope,
                                message = context.getString(R.string.setting_edit_profile_image_processing_failed),
                            )
                        }
                    }
                },
                onError = { exception: Exception ->
                    Timber.e(exception, "GalleryPicker 에러 발생")
                    snackbarHostState.showSingleSnackbar(
                        scope = scope,
                        message = context.getString(R.string.setting_edit_profile_image_selection_failed),
                    )
                    onClosePicker()
                },
                onDismiss = {
                    Timber.d("GalleryPicker 닫힘")
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

        false -> {
            Timber.e("Context가 ComponentActivity가 아닙니다: ${context.javaClass.name}")
            LaunchedEffect(Unit) {
                snackbarHostState.showSingleSnackbar(
                    scope = scope,
                    message = context.getString(R.string.setting_edit_profile_image_selection_failed),
                )
                onClosePicker()
            }
        }
    }
}

@Composable
private fun SettingMainScreen(
    onClickSettingAccount: () -> Unit,
    onNicknameEdit: () -> Unit,
    onProfileImageUpload: () -> Unit,
    onFavoriteTeamEditClick: () -> Unit,
    memberInfoItem: MemberInfoItem,
    appVersion: String,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(Gray050)
                .padding(top = 8.dp, start = 20.dp, end = 20.dp, bottom = 20.dp)
                .verticalScroll(state = rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        MyProfile(memberInfoItem = memberInfoItem)

        SettingButtonGroup {
            SettingButton(
                text = stringResource(R.string.setting_edit_profile_image),
                onClick = onProfileImageUpload,
            )
            SettingButton(
                text = stringResource(R.string.setting_edit_nickname),
                onClick = onNicknameEdit,
            )
            SettingButton(
                text = stringResource(R.string.setting_edit_my_team),
                onClick = onFavoriteTeamEditClick,
            )
            SettingButton(
                text = stringResource(R.string.setting_manage_account),
                onClick = onClickSettingAccount,
            )
        }

        SettingButtonGroup {
            SettingButton(
                text = stringResource(R.string.setting_notice),
                onClick = { context.openUrl(NOTICE_URL) },
            )
            SettingButton(
                text = stringResource(R.string.setting_contact_us),
                onClick = { context.openUrl(CONTACT_URL) },
            )
            SettingButton(text = stringResource(R.string.setting_open_source_license), onClick = {
                context.startActivity(Intent(context, OssLicensesMenuActivity::class.java))
            })
        }

        Text(
            text = stringResource(R.string.setting_app_version, appVersion),
            textAlign = TextAlign.Center,
            style = PretendardMedium12,
            color = Gray400,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
        )
    }
}

@Composable
private fun MyProfile(
    memberInfoItem: MemberInfoItem,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(White, RoundedCornerShape(12.dp)),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(30.dp))
        ProfileImage(memberInfoItem.profileImageUrl, modifier = Modifier.size(100.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = memberInfoItem.nickName, style = PretendardSemiBold, fontSize = 24.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text =
                stringResource(
                    R.string.setting_main_sign_up_date,
                    memberInfoItem.createdAt.format(yyyyMMddFormatter),
                ),
            style = PretendardRegular12,
            color = Gray500,
        )
        Spacer(modifier = Modifier.height(30.dp))
    }
}

private fun NicknameUpdateError.asString(context: Context): String =
    when (this) {
        NicknameUpdateError.DuplicateNickname ->
            context.getString(R.string.setting_edit_nickname_duplicate)

        NicknameUpdateError.InvalidNickname ->
            context.getString(R.string.setting_edit_nickname_invalid_format)

        NicknameUpdateError.MemberNotFound ->
            context.getString(R.string.setting_edit_nickname_member_not_found)

        NicknameUpdateError.NoPermission ->
            context.getString(R.string.setting_edit_nickname_no_permission)

        NicknameUpdateError.PayloadTooLarge ->
            context.getString(R.string.setting_edit_nickname_too_long)

        NicknameUpdateError.ServerError ->
            context.getString(R.string.setting_edit_nickname_server_error)

        is NicknameUpdateError.Unknown ->
            message ?: context.getString(R.string.setting_edit_nickname_unknown_default)
    }

private fun Context.getAppVersion(): String =
    try {
        val packageInfo: PackageInfo =
            packageManager.getPackageInfo(packageName, 0)
        packageInfo.versionName ?: DEFAULT_VERSION_NAME
    } catch (e: PackageManager.NameNotFoundException) {
        Timber.d("앱 버전 로드 실패 ${e.message}")
        DEFAULT_VERSION_NAME
    }

/**
 * ImagePickerKMP로 자른 이미지를
 * 백엔드에서 요구하는 프로파일 이미지 규격(jpeg, 5mb)으로 컨버팅하여 업로드합니다.
 */
private suspend fun handleImagePickerKMPCroppedImage(
    context: Context,
    snackBarScope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    sourceImageUri: Uri,
    onProfileImageUpload: suspend (Uri, String, Long) -> Result<Unit>,
) {
    runCatching {
        val originalFile = File(sourceImageUri.path ?: error("경로를 찾을 수 없음"))
        val convertedProfileImage =
            Compressor.compress(context, originalFile) {
                resolution(500, 500)
                quality(90)
                format(Bitmap.CompressFormat.JPEG)
                size(5L * 1024L * 1024L)
            }

        val convertedImageUri = convertedProfileImage.toUri()
        val fileSize = convertedProfileImage.length()
        val mimeType = "image/jpeg"

        onProfileImageUpload(convertedImageUri, mimeType, fileSize)
    }.fold(
        onSuccess = { result: Result<Unit> ->
            result.onFailure { e ->
                if (e is CancellationException) throw e
                snackbarHostState.showSingleSnackbar(
                    scope = snackBarScope,
                    message = context.getString(R.string.setting_edit_profile_image_upload_failed),
                )
            }
        },
        onFailure = { e: Throwable ->
            if (e is CancellationException) throw e
            Timber.e(e, "프로필 이미지 전처리 실패")
            snackbarHostState.showSingleSnackbar(
                scope = snackBarScope,
                message = context.getString(R.string.setting_edit_profile_image_processing_failed),
            )
        },
    )
}

private fun Context.openUrl(url: String) {
    val intent = Intent(Intent.ACTION_VIEW, url.toUri())
    startActivity(intent)
}

private const val NOTICE_URL =
    "https://scented-allosaurus-6df.notion.site/251ad073c10b805baf8af1a7badd20e7?pvs=74"
private const val CONTACT_URL = "https://forms.gle/wBhXjfTLyobZa19K8"
private const val DEFAULT_VERSION_NAME = "x.x.x"

@Preview(showBackground = true)
@Composable
private fun SettingMainScreenPreview() {
    SettingMainScreen(
        onClickSettingAccount = {},
        onNicknameEdit = {},
        onProfileImageUpload = {},
        onFavoriteTeamEditClick = {},
        memberInfoItem = MemberInfoItem(nickName = "야구보구"),
        appVersion = "1.0.0",
    )
}
