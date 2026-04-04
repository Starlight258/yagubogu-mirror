import ComposeApp
import GoogleMobileAds
import GoogleSignIn
import SwiftUI
import UIKit

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

    // AdMob SDK 초기화 및 Kotlin 브릿지 팩토리 등록
    init() {
        MobileAds.shared.start(completionHandler: nil)
        setupBannerAdProvider()
    }

    // Kotlin BannerAdProvider에 GADBannerView 생성 팩토리 주입
    private func setupBannerAdProvider() {
        BannerAdProvider.shared.create = { adUnitId, heightPx in
            let bannerView = BannerView(adSize: adSizeFor(cgSize: CGSize(width: 0, height: CGFloat(heightPx))))
            bannerView.adUnitID = adUnitId
            if let rootVC = UIApplication.shared.connectedScenes
                .compactMap({ $0 as? UIWindowScene })
                .flatMap({ $0.windows })
                .first(where: { $0.isKeyWindow })?.rootViewController
            {
                bannerView.rootViewController = rootVC
            }
            bannerView.load(Request())
            return bannerView
        }
    }
}
