package de.lilithwittmann.voicepitchanalyzer.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.util.Log;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

// started/cleaned up from tutorials.jenkov.com/android/android-web-apps-using-android-webview.html
public class CacheableWebViewClient extends WebViewClient {
    private static String LOG_TAG = "reader web view cacher";

    private Activity activity = null;
    private UrlCache urlCache = null;
    private int yScroll;
    private int xScroll;

    public CacheableWebViewClient(Activity activity) {
        this.activity = activity;
        this.urlCache = new UrlCache(activity);
    }

    public void addCacheableUrl(String url) {
        this.urlCache.registerHtml(url, UrlCache.ONE_HOUR);
    }

    public void setScrollPosition(int x, int y) {
        xScroll = x;
        yScroll = y;
    }

    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        if (urlCache.getCacheEntry(url) == null)
            return false;

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        activity.startActivity(intent);

        return true;
    }

    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        view.scrollTo(xScroll, yScroll);
    }

    public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
        return this.urlCache.load(url);
    }

    @SuppressWarnings("deprecation")
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        Toast.makeText(activity, "Error loading user input web page, "
                + description, Toast.LENGTH_SHORT).show();
    }

    protected static class UrlCache {
        public static final long ONE_SECOND = 1000L;
        public static final long ONE_MINUTE = 60L * ONE_SECOND;
        public static final long ONE_HOUR = 60L * ONE_MINUTE;
        public static final long ONE_DAY = 24 * ONE_HOUR;

        private static final String CACHE_CHILD_DIR = "web-view-cache";
        public static final String ENTRIES_FILE_NAME = "web-cache-entries.json";
        private static boolean isFirstRun = true;

        private Map<String, CacheEntry> cacheEntries = new HashMap<>();
        protected Activity activity = null;
        protected File rootDir = null;

        public UrlCache(Activity activity) {
            this.activity = activity;
            this.rootDir = new File(activity.getFilesDir() + "/" + CACHE_CHILD_DIR);
            if (!rootDir.exists())
                rootDir.mkdir();

            initializeEntries();
        }

        public void registerHtml(String url, long maxAgeMillis) {
            if (getCacheEntry(url) != null) {
                getCacheEntry(url).url = url;
                getCacheEntry(url).maxAgeMillis = maxAgeMillis;
                saveCacheEntries();
                return;
            }

            String cacheFileName = UUID.randomUUID().toString() + ".html";
            CacheEntry entry = new CacheEntry(url, cacheFileName, maxAgeMillis);

            addCacheEntry(url, entry);
        }

        public WebResourceResponse load(String url) {
            CacheEntry entry = getCacheEntry(url);
            if (entry == null)
                return null;

            File cachedFile = entry.getCachedFile();
            if (cachedFile.exists()) {
                // don't dcacelete cache file if web connection bad so still useable offline
                if (entry.isExpired() && isOnline()) {
                    cachedFile.delete();
                    Log.d(LOG_TAG, "Deleting from cache: " + url);

                    return loadFromWeb(entry);
                }

                Log.d(LOG_TAG, "Loaded from cache: " + entry.url);
                return loadFromCache(entry);
            }

            return loadFromWeb(entry);
        }

        private WebResourceResponse loadFromCache(CacheEntry entry) {
            try {
                FileInputStream in = new FileInputStream(entry.getCachedFile());
                return new WebResourceResponse(entry.mimeType, entry.encoding, in);
            } catch (FileNotFoundException e) {
                Log.d(LOG_TAG, "Error loading cached file: " + entry.getCachedFile().getPath() + " : "
                        + e.getMessage(), e);
            }

            return null;
        }

        private WebResourceResponse loadFromWeb(CacheEntry entry) {
            try {
                downloadAndStore(entry);
                return loadFromCache(entry);
            } catch (Exception e) {
                Log.d(LOG_TAG, "Error reading file over network: " + entry.url, e);
            }

            return null;
        }

        private void downloadAndStore(CacheEntry entry) throws IOException {
            URL urlObj = new URL(entry.url);
            URLConnection urlConnection = urlObj.openConnection();
            InputStream urlInput = urlConnection.getInputStream();
            FileOutputStream fileOutputStream = new FileOutputStream(entry.getCachedFile());

            int data = urlInput.read();
            while (data != -1) {
                fileOutputStream.write(data);
                data = urlInput.read();
            }

            urlInput.close();
            fileOutputStream.close();
            Log.d(LOG_TAG, "Cache file: " + entry.fileName + " stored. ");
        }

        private void initializeEntries() {
            loadCacheEntries();

            if (isFirstRun)
                clearAllExpired();
            isFirstRun = false;
        }

        private void saveCacheEntries() {
            Gson gson = new GsonBuilder().create();
            String json = gson.toJson(cacheEntries);

            try {
                FileOutputStream output = activity.openFileOutput(ENTRIES_FILE_NAME, Context.MODE_PRIVATE);
                IOUtils.write(json, output);
            } catch (IOException e) {
                Log.d(LOG_TAG, "Failed to write cache entries json to storage");
            }
        }

        private void loadCacheEntries() {
            String json;
            try {
                FileInputStream cacheStream = activity.openFileInput(ENTRIES_FILE_NAME);
                json = IOUtils.toString(cacheStream, "UTF-8");
            } catch (IOException e) {
                return;
            }

            Gson gson = new GsonBuilder().create();
            Type typeOfHashMap = new TypeToken<Map<String, CacheEntry>>() {
            }.getType();
            cacheEntries = gson.fromJson(json, typeOfHashMap);

            // must set rootdir since inner class accescing the parent rootDir field
            // appears to not work when creating the objects from gson
            for (Map.Entry<String, CacheEntry> entryOn : cacheEntries.entrySet())
                entryOn.getValue().rootDir = rootDir;
        }

        private void clearAllExpired() {
            List<String> urlsToRemove = new ArrayList<>();

            for (Map.Entry<String, CacheEntry> entryOn : cacheEntries.entrySet()) {
                File file = entryOn.getValue().getCachedFile();

                if (file.exists() && entryOn.getValue().isExpired() && isOnline()) {
                    file.delete();
                    urlsToRemove.add(entryOn.getKey());
                }
            }

            for (String urlOn : urlsToRemove)
                cacheEntries.remove(urlOn);

            saveCacheEntries();
        }

        private CacheEntry getCacheEntry(String url) {
            return cacheEntries.get(cleanUrl(url));
        }

        private void addCacheEntry(String url, CacheEntry entry) {
            cacheEntries.put(cleanUrl(url), entry);
            entry.rootDir = rootDir;
            saveCacheEntries();
        }

        private String cleanUrl(String url) {
            url = url.replace("http://", "").replace("www.", "");
            url = url.replaceAll("/$", "");

            return url.toLowerCase();
        }

        private boolean isOnline() {
            try {
                ConnectivityManager cm = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
                return cm.getActiveNetworkInfo().isConnectedOrConnecting();
            } catch (Exception e) {
                return false;
            }
        }

        protected class CacheEntry {
            public String url;
            public String fileName;
            public String mimeType = "text/html";
            public String encoding = "UTF-8";
            public long maxAgeMillis;
            public File rootDir;

            protected CacheEntry(String url, String fileName, long maxAgeMillis) {
                this.url = url;
                this.fileName = fileName;
                this.maxAgeMillis = maxAgeMillis;
            }

            protected File getCachedFile() {
                return new File(rootDir.getPath() + File.separator + fileName);
            }

            private boolean isExpired() {
                long cacheEntryAge = System.currentTimeMillis() - getCachedFile().lastModified();
                return cacheEntryAge > maxAgeMillis;
            }
        }
    }
}