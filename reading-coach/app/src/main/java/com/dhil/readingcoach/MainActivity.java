package com.dhil.readingcoach;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * Shows the Reading Coach page. On every launch it quietly downloads the newest
 * index.html from GitHub; if it changed, the app switches to it (right away when
 * Ivana is on the home screen, otherwise next launch). No reinstall needed.
 */
public class MainActivity extends Activity {
    private static final String BASE = "https://readingcoach.app/"; // fixed origin keeps progress in localStorage
    private WebView web;
    private String current;
    private final Handler main = new Handler(Looper.getMainLooper());
    private TextToSpeech tts;
    private volatile boolean ttsReady;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        web = new WebView(this);
        web.setBackgroundColor(Color.parseColor("#fbf7ef"));
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setTextZoom(100);
        setContentView(web);
        // Lets the page speak praise out loud ("Great job!") with the phone's own voice
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.SUCCESS) return;
                tts.setLanguage(Locale.US);
                tts.setPitch(1.15f);
                ttsReady = true;
            }
        });
        web.addJavascriptInterface(new Voice(), "AndroidVoice");
        current = loadPage();
        show(current);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkForUpdate();
    }

    private class Voice {
        @JavascriptInterface
        public void speak(String text) {
            if (ttsReady && text != null) tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "cheer");
        }
    }

    @Override
    protected void onDestroy() {
        if (tts != null) tts.shutdown();
        super.onDestroy();
    }

    private File cacheFile() { return new File(getFilesDir(), "index.html"); }

    private String loadPage() {
        try {
            File f = cacheFile();
            if (f.exists()) return read(new FileInputStream(f));
            return read(getAssets().open("index.html"));
        } catch (Exception e) {
            return "<h1>Could not load Reading Coach</h1>";
        }
    }

    private void show(String html) {
        web.loadDataWithBaseURL(BASE, html, "text/html", "utf-8", null);
    }

    private void checkForUpdate() {
        final String url = BuildConfig.UPDATE_URL;
        if (url == null || url.isEmpty()) return;
        new Thread(new Runnable() {
            @Override
            public void run() {
                downloadUpdate(url);
            }
        }).start();
    }

    private void downloadUpdate(String url) {
        try {
            HttpURLConnection c = (HttpURLConnection) new URL(url + "?t=" + System.currentTimeMillis()).openConnection();
            c.setConnectTimeout(8000);
            c.setReadTimeout(8000);
            c.setUseCaches(false);
            if (c.getResponseCode() != 200) return;
            String fresh = read(c.getInputStream());
            if (!fresh.contains("Reading Coach") || fresh.equals(current)) return;
            try (FileOutputStream out = new FileOutputStream(cacheFile())) {
                out.write(fresh.getBytes(StandardCharsets.UTF_8));
            }
            main.post(new Runnable() {
                @Override
                public void run() {
                    web.evaluateJavascript(
                            "(function(){var h=document.getElementById('home');return !!(h&&!h.hidden);})()",
                            new ValueCallback<String>() {
                                @Override
                                public void onReceiveValue(String onHome) {
                                    if ("true".equals(onHome)) {
                                        current = fresh;
                                        show(fresh);
                                        Toast.makeText(MainActivity.this, "Reading Coach updated", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            });
        } catch (Exception ignored) {
            // offline: keep the copy we have
        }
    }

    private static String read(InputStream in) throws Exception {
        try (InputStream i = in; ByteArrayOutputStream o = new ByteArrayOutputStream()) {
            byte[] buf = new byte[8192];
            int n;
            while ((n = i.read(buf)) > 0) o.write(buf, 0, n);
            return o.toString("UTF-8");
        }
    }

    @Override
    public void onBackPressed() {
        web.evaluateJavascript(
                "(function(){return window.goBack?window.goBack():'exit';})()",
                new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String r) {
                        if (r != null && r.contains("exit")) finish();
                    }
                });
    }
}
