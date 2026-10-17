package com.madminute.app;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Hosts the drill UI (assets/index.html) in a WebView.
 *
 * On every launch it downloads the latest index.html from BuildConfig.UPDATE_URL
 * in the background and caches it. The cached copy is used from then on, so the
 * app can be updated without reinstalling. If the download fails, the last
 * cached copy (or the copy bundled in the APK) is used.
 */
public class MainActivity extends Activity {
    private static final String TAG = "MadMinute";
    // Same base URL for bundled and downloaded pages so localStorage (best scores) is shared.
    private static final String BASE_URL = "https://appassets.androidplatform.net/";
    private static final String CACHE_FILE = "index.html";
    private static final int MAX_HTML_BYTES = 2 * 1024 * 1024;

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        webView = new WebView(this);
        setContentView(webView);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setTextZoom(100);

        webView.addJavascriptInterface(new AppBridge(), "MadMinuteApp");
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                // Any link inside the page opens in the phone's browser.
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, request.getUrl()));
                } catch (Exception e) {
                    Log.w(TAG, "No app to open " + request.getUrl(), e);
                }
                return true;
            }
        });

        dropCacheIfApkIsNewer();
        loadPage();
        fetchUpdate();
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.destroy();
        }
        super.onDestroy();
    }

    private void loadPage() {
        String html = readCachedHtml();
        if (html == null) {
            html = readBundledHtml();
        }
        webView.loadDataWithBaseURL(BASE_URL, html, "text/html", "UTF-8", null);
    }

    /** A freshly installed APK carries its own index.html; don't let an older download shadow it. */
    private void dropCacheIfApkIsNewer() {
        File cache = new File(getFilesDir(), CACHE_FILE);
        if (!cache.exists()) {
            return;
        }
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
            if (cache.lastModified() < info.lastUpdateTime) {
                cache.delete();
            }
        } catch (Exception e) {
            Log.w(TAG, "Could not compare cache age", e);
        }
    }

    private String readCachedHtml() {
        File cache = new File(getFilesDir(), CACHE_FILE);
        if (!cache.exists()) {
            return null;
        }
        try (InputStream in = new java.io.FileInputStream(cache)) {
            return readAll(in);
        } catch (IOException e) {
            Log.w(TAG, "Bad cached page, using bundled copy", e);
            return null;
        }
    }

    private String readBundledHtml() {
        try (InputStream in = getAssets().open(CACHE_FILE)) {
            return readAll(in);
        } catch (IOException e) {
            throw new IllegalStateException("index.html missing from assets", e);
        }
    }

    private void fetchUpdate() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                downloadUpdate();
            }
        }, "update-check").start();
    }

    private void downloadUpdate() {
        HttpURLConnection conn = null;
        try {
            // Cache-buster so the CDN hands back the newest file.
            URL url = new URL(BuildConfig.UPDATE_URL + "?t=" + System.currentTimeMillis());
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            conn.setUseCaches(false);
            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                Log.i(TAG, "Update check: HTTP " + conn.getResponseCode());
                return;
            }
            String html;
            try (InputStream in = conn.getInputStream()) {
                html = readAll(in);
            }
            if (!looksLikeThePage(html)) {
                Log.w(TAG, "Downloaded file doesn't look like the app page; ignoring");
                return;
            }
            String current = readCachedHtml();
            if (current == null) {
                current = readBundledHtml();
            }
            if (html.equals(current)) {
                return;
            }
            writeCache(html);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (!isFinishing()) {
                        webView.evaluateJavascript(
                                "window.mmUpdateReady && window.mmUpdateReady()", null);
                    }
                }
            });
        } catch (Exception e) {
            Log.i(TAG, "Update check failed (offline?)", e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private static boolean looksLikeThePage(String html) {
        String lower = html.toLowerCase();
        return lower.contains("<html") && lower.contains("</html>") && html.contains("mad-minute-app");
    }

    private void writeCache(String html) throws IOException {
        File tmp = new File(getFilesDir(), CACHE_FILE + ".tmp");
        try (FileOutputStream out = new FileOutputStream(tmp)) {
            out.write(html.getBytes(StandardCharsets.UTF_8));
            out.getFD().sync();
        }
        if (!tmp.renameTo(new File(getFilesDir(), CACHE_FILE))) {
            tmp.delete();
            throw new IOException("Could not save downloaded page");
        }
    }

    private static String readAll(InputStream in) throws IOException {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        byte[] chunk = new byte[8192];
        int n;
        while ((n = in.read(chunk)) != -1) {
            buf.write(chunk, 0, n);
            if (buf.size() > MAX_HTML_BYTES) {
                throw new IOException("Page too large");
            }
        }
        return new String(buf.toByteArray(), StandardCharsets.UTF_8);
    }

    /** Called from the page: window.MadMinuteApp.* */
    private class AppBridge {
        @JavascriptInterface
        public void applyUpdate() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loadPage();
                }
            });
        }

        @JavascriptInterface
        public String appVersion() {
            return BuildConfig.VERSION_NAME;
        }
    }
}
