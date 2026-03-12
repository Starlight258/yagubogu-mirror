package com.yagubogu.ui.login

import cocoapods.GoogleSignIn.GIDConfiguration
import cocoapods.GoogleSignIn.GIDSignIn
import cocoapods.GoogleSignIn.GIDSignInResult
import com.yagubogu.BuildKonfig
import com.yagubogu.ui.login.auth.GoogleCredentialManager
import com.yagubogu.ui.login.auth.GoogleCredentialResult
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSError
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController
import kotlin.coroutines.resume

@OptIn(ExperimentalForeignApi::class)
class IosGoogleCredentialManager : GoogleCredentialManager {

    init {
        GIDSignIn.sharedInstance.configuration = GIDConfiguration(
            clientID = BuildKonfig.IOS_CLIENT_ID,
            serverClientID = BuildKonfig.WEB_CLIENT_ID,
        )
    }

    override suspend fun getGoogleCredentialResult(): GoogleCredentialResult {
        // Google 로그인 UI를 띄울 루트 VC가 없으면 즉시 실패 반환
        val rootViewController = getRootViewController()
            ?: return GoogleCredentialResult.Failure(IllegalStateException("No root view controller"))

        // GIDSignIn 완료 콜백을 코루틴으로 변환
        return suspendCancellableCoroutine { continuation ->
            GIDSignIn.sharedInstance.signInWithPresentingViewController(
                presentingViewController = rootViewController,
                completion = { result: GIDSignInResult?, error: NSError? ->
                    continuation.resume(handleSignInCompletion(result, error))
                },
            )
        }
    }

    // 현재 앱 윈도우의 루트 뷰 컨트롤러 반환
    private fun getRootViewController(): UIViewController? =
        UIApplication.sharedApplication.keyWindow?.rootViewController

    // 로그인 콜백 결과를 GoogleCredentialResult로 변환
    private fun handleSignInCompletion(
        result: GIDSignInResult?,
        error: NSError?,
    ): GoogleCredentialResult = when {
        error != null -> handleSignInError(error)
        else -> result?.user?.idToken?.tokenString
            ?.let { GoogleCredentialResult.Success(it) }
            ?: GoogleCredentialResult.Failure(Exception("ID token is null"))
    }

    // 에러 코드로 사용자 취소와 실제 오류를 구분
    private fun handleSignInError(error: NSError): GoogleCredentialResult =
        if (error.code.toInt() == kGIDSignInErrorCodeCanceled) {
            GoogleCredentialResult.Cancel
        } else {
            GoogleCredentialResult.Failure(Exception(error.localizedDescription))
        }

    override suspend fun signOut(): Result<Unit> =
        runCatching {
            GIDSignIn.sharedInstance.signOut()
        }

    companion object {
        private const val kGIDSignInErrorCodeCanceled = -5
    }
}
