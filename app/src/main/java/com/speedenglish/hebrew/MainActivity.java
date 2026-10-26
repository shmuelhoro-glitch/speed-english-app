package com.speedenglish.hebrew;

import android.app.Activity;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.graphics.Color;
import java.util.Locale;

public class MainActivity extends Activity {
    private WebView webView;
    private TextToSpeech tts;

    public class SpeechBridge {
        @JavascriptInterface public void speak(String text) {
            if (text == null || text.length() > 150) return;
            runOnUiThread(() -> {
                if (tts != null) tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "word");
            });
        }
    }

    public class HapticBridge {
        @JavascriptInterface public void vibrate(boolean correct) {
            runOnUiThread(() -> {
                Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                if (vibrator != null && vibrator.hasVibrator()) {
                    long duration = correct ? 35 : 85;
                    vibrator.vibrate(VibrationEffect.createOneShot(duration, correct ? 90 : 155));
                }
            });
        }
    }

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.rgb(62, 18, 91));
        getWindow().setNavigationBarColor(Color.rgb(62, 18, 91));
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS && tts != null) tts.setLanguage(Locale.US);
        });
        webView = new WebView(this);
        webView.setBackgroundColor(Color.rgb(62, 18, 91));
        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient());
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setSupportZoom(false);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(false);
        settings.setAllowFileAccessFromFileURLs(false);
        settings.setAllowUniversalAccessFromFileURLs(false);
        webView.addJavascriptInterface(new SpeechBridge(), "AndroidSpeech");
        webView.addJavascriptInterface(new HapticBridge(), "AndroidHaptics");
        setContentView(webView);
        webView.loadUrl("file:///android_asset/index.html");
    }

    @Override public void onBackPressed() {
        webView.evaluateJavascript("(function(){var b=document.getElementById('backBtn');if(b&&!document.getElementById('game').classList.contains('hidden')){b.click();return true;}var h=document.getElementById('homeBtn');if(h&&!document.getElementById('stats').classList.contains('hidden')){h.click();return true;}return false;})()", value -> {
            if (!"true".equals(value)) super.onBackPressed();
        });
    }

    @Override protected void onDestroy() {
        if (tts != null) { tts.stop(); tts.shutdown(); }
        if (webView != null) webView.destroy();
        super.onDestroy();
    }
}
