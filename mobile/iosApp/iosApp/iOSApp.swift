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
        setupInterstitialAdProvider()
    }

    // Kotlin BannerAdProvider에 GADBannerView 생성 팩토리 주입
    private func setupBannerAdProvider() {
        BannerAdProvider.shared.create = { adUnitId, heightPx in
            let gadAdSize: GoogleMobileAds.AdSize
            switch heightPx {
            case 100: gadAdSize = GoogleMobileAds.AdSizeLargeBanner
            case 250: gadAdSize = GoogleMobileAds.AdSizeMediumRectangle
            default: gadAdSize = GoogleMobileAds.AdSizeBanner
            }
            let bannerView = BannerView(adSize: gadAdSize)
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

    // Kotlin InterstitialAdProvider에 preload/show 클로저 주입
    private func setupInterstitialAdProvider() {
        let coordinator = InterstitialAdCoordinator()

        InterstitialAdProvider.shared.preload = { adUnitId in
            coordinator.load(adUnitId: adUnitId)
        }

        InterstitialAdProvider.shared.show = { adUnitId in
            guard
                let rootVC = UIApplication.shared.connectedScenes
                    .compactMap({ $0 as? UIWindowScene })
                    .flatMap({ $0.windows })
                    .first(where: { $0.isKeyWindow })?.rootViewController
            else { return }
            coordinator.show(from: rootVC, adUnitId: adUnitId)
        }
    }
}
