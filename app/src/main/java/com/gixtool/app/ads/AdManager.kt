package com.gixtool.app.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.FrameLayout
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

object AdConfig {
    const val BANNER_ID      = "ca-app-pub-6718038985057828/5364644957"
    const val INTERSTITIAL_ID = "ca-app-pub-6718038985057828/4460277306"
    const val REWARDED_ID    = "ca-app-pub-6718038985057828/7252441697"
}

class AdManager(private val context: Context) {

    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null
    private val tag = "AdManager"

    fun initialize(onReady: () -> Unit = {}) {
        MobileAds.initialize(context) {
            Log.d(tag, "AdMob initialized")
            loadInterstitial()
            loadRewarded()
            onReady()
        }
    }

    fun loadBanner(container: FrameLayout) {
        val banner = AdView(context).apply {
            adUnitId = AdConfig.BANNER_ID
            setAdSize(AdSize.BANNER)
        }
        container.removeAllViews()
        container.addView(banner)
        banner.loadAd(AdRequest.Builder().build())
    }

    private fun loadInterstitial() {
        InterstitialAd.load(context, AdConfig.INTERSTITIAL_ID,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    Log.d(tag, "Interstitial loaded")
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e(tag, "Interstitial failed: ${error.message}")
                    interstitialAd = null
                }
            })
    }

    private fun loadRewarded() {
        RewardedAd.load(context, AdConfig.REWARDED_ID,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    Log.d(tag, "Rewarded loaded")
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e(tag, "Rewarded failed: ${error.message}")
                    rewardedAd = null
                }
            })
    }

    fun showInterstitial(activity: Activity, onDone: () -> Unit) {
        val ad = interstitialAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() { loadInterstitial(); onDone() }
                override fun onAdFailedToShowFullScreenContent(e: AdError) { onDone() }
            }
            ad.show(activity)
        } else {
            loadInterstitial()
            onDone()
        }
    }

    fun showRewarded(activity: Activity, onRewarded: () -> Unit, onDone: () -> Unit) {
        val ad = rewardedAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() { loadRewarded(); onDone() }
                override fun onAdFailedToShowFullScreenContent(e: AdError) { onDone() }
            }
            ad.show(activity) { onRewarded() }
        } else {
            loadRewarded()
            onDone()
        }
    }
}
