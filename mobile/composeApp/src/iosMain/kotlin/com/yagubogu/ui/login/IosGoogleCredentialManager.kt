package com.yagubogu.ui.login

import com.yagubogu.BuildKonfig
import com.yagubogu.ui.login.auth.GoogleCredentialManager
import com.yagubogu.ui.login.auth.GoogleCredentialResult
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class IosGoogleCredentialManager(
    private val delegate: GoogleSignInDelegate,
) : GoogleCredentialManager {

    init {
        // BuildKonfig의 클라이언트 ID를 Swift 델리게이트로 전달해 GIDSignIn을 구성한다.
        // 기존 CocoaPods 방식의 GIDConfiguration 설정을 Swift 측으로 위임한다.
        delegate.configure(
            iosClientId = BuildKonfig.IOS_CLIENT_ID,
            serverClientId = BuildKonfig.WEB_CLIENT_ID,
        )
    }

    override suspend fun getGoogleCredentialResult(): GoogleCredentialResult =
        suspendCancellableCoroutine { continuation ->
            delegate.signIn(
                onSuccess = { idToken -> continuation.resume(GoogleCredentialResult.Success(idToken)) },
                onCancel = { continuation.resume(GoogleCredentialResult.Cancel) },
                onFailure = { message -> continuation.resume(GoogleCredentialResult.Failure(Exception(message))) },
            )
        }

    override suspend fun signOut(): Result<Unit> =
        runCatching {
            delegate.signOut()
        }
}
