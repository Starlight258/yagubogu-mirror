package com.yagubogu.data.repository.member

import com.yagubogu.data.util.ApiException
import com.yagubogu.ui.util.UiText
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.setting_edit_nickname_duplicate
import yagubogu.composeapp.generated.resources.setting_edit_nickname_invalid_format
import yagubogu.composeapp.generated.resources.setting_edit_nickname_member_not_found
import yagubogu.composeapp.generated.resources.setting_edit_nickname_network_error
import yagubogu.composeapp.generated.resources.setting_edit_nickname_no_permission
import yagubogu.composeapp.generated.resources.setting_edit_nickname_server_error
import yagubogu.composeapp.generated.resources.setting_edit_nickname_too_long
import yagubogu.composeapp.generated.resources.setting_edit_nickname_unknown_error

sealed interface NicknameUpdateError {
    data object DuplicateNickname : NicknameUpdateError // 409: 이미 존재함

    data object InvalidNickname : NicknameUpdateError // 400, 422: 형식이 잘못됨

    data object MemberNotFound : NicknameUpdateError // 404: 탈퇴했거나 없는 회원

    data object NoPermission : NicknameUpdateError // 403: 권한 없음

    data object PayloadTooLarge : NicknameUpdateError // 413: 데이터가 너무 큼

    data object ServerError : NicknameUpdateError // 500, 502: 서버 장애

    data object NetworkIssue : NicknameUpdateError // 네트워크 연결 실패

    data class Unknown(
        val message: String?,
    ) : NicknameUpdateError // 기타
}

fun Throwable.toNicknameUpdateError(): NicknameUpdateError =
    when (this) {
        is NicknameUpdateException -> this.error
        is ApiException.Conflict -> NicknameUpdateError.DuplicateNickname // 409
        is ApiException.UnprocessableEntity -> NicknameUpdateError.InvalidNickname // 422
        is ApiException.BadRequest -> NicknameUpdateError.InvalidNickname // 400
        is ApiException.Forbidden -> NicknameUpdateError.NoPermission // 403
        is ApiException.NotFound -> NicknameUpdateError.MemberNotFound // 404
        is ApiException.ServerError -> NicknameUpdateError.ServerError // 5xx
        is ApiException.NetworkError -> NicknameUpdateError.NetworkIssue // 네트워크 미연결
        else -> NicknameUpdateError.Unknown(message)
    }

fun NicknameUpdateError.toUiText(): UiText =
    when (this) {
        NicknameUpdateError.DuplicateNickname -> UiText.StringRes(Res.string.setting_edit_nickname_duplicate)
        NicknameUpdateError.InvalidNickname -> UiText.StringRes(Res.string.setting_edit_nickname_invalid_format)
        NicknameUpdateError.MemberNotFound -> UiText.StringRes(Res.string.setting_edit_nickname_member_not_found)
        NicknameUpdateError.NoPermission -> UiText.StringRes(Res.string.setting_edit_nickname_no_permission)
        NicknameUpdateError.PayloadTooLarge -> UiText.StringRes(Res.string.setting_edit_nickname_too_long)
        NicknameUpdateError.ServerError -> UiText.StringRes(Res.string.setting_edit_nickname_server_error)
        NicknameUpdateError.NetworkIssue -> UiText.StringRes(Res.string.setting_edit_nickname_network_error)
        is NicknameUpdateError.Unknown ->
            message?.let { UiText.DynamicString(it) }
                ?: UiText.StringRes(Res.string.setting_edit_nickname_unknown_error)
    }
