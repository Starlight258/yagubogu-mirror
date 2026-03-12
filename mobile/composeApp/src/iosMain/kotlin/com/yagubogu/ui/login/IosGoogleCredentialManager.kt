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

    override suspend fun getGoogleCredentialResult(): GoogleCredentialResult =
        suspendCancellableCoroutine { continuation ->
            val rootViewController: UIViewController? =
                UIApplication.sharedApplication.keyWindow?.rootViewController

            if (rootViewController == null) {
                continuation.resume(
                    GoogleCredentialResult.Failure(IllegalStateException("No root view controller"))
                )
                return@suspendCancellableCoroutine
            }

            GIDSignIn.sharedInstance.signInWithPresentingViewController(
                presentingViewController = rootViewController,
                completion = { result: GIDSignInResult?, error: NSError? ->
                    if (error != null) {
                        if (error.code.toInt() == kGIDSignInErrorCodeCanceled) {
                            continuation.resume(GoogleCredentialResult.Cancel)
                        } else {
                            continuation.resume(
                                GoogleCredentialResult.Failure(Exception(error.localizedDescription)),
                            )
                        }
                    } else {
                        val idToken = result?.user?.idToken?.tokenString
                        if (idToken != null) {
                            continuation.resume(GoogleCredentialResult.Success(idToken))
                        } else {
                            continuation.resume(
                                GoogleCredentialResult.Failure(Exception("ID token is null")),
                            )
                        }
                    }
                },
            )
        }

    override suspend fun signOut(): Result<Unit> =
        runCatching {
            GIDSignIn.sharedInstance.signOut()
        }

    companion object {
        private const val kGIDSignInErrorCodeCanceled = -5
    }
}
