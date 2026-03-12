import SwiftUI
import GoogleSignIn

@main
struct iOSApp: App {
    var body: some Scene {
        WindowGroup {
            ContentView()
                // Google OAuth 콜백 URL 처리 (Info.plist의 REVERSED_CLIENT_ID 스킴으로 재진입 시 호출)
                .onOpenURL { url in
                    GIDSignIn.sharedInstance.handle(url)
                }
        }
    }
}
