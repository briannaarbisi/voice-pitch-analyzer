package de.lilithwittmann.voicepitchanalyzer.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.*;
import de.lilithwittmann.voicepitchanalyzer.R;
import de.lilithwittmann.voicepitchanalyzer.utils.CacheableWebViewClient;

/**
 * Reading material for display while doing a recording. Loads a user input url from settings.
 */
public class WebReadingFragment extends Fragment {
    public WebReadingFragment() {
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reading_web_page, container, false);
    }

    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        WebView webView = (WebView) view.findViewById(R.id.webView);
        initWebView(webView);
        webView.loadUrl(getUrlFromSettings());
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveScrollPosition();
    }

    public void onPause() {
        super.onPause();
        saveScrollPosition();
    }

    private SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
    }

    private void initWebView(WebView webView) {
        final Activity activity = getActivity();
        webView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                activity.setProgress(progress * 1000);
            }
        });

        CacheableWebViewClient client = new CacheableWebViewClient(getActivity());
        client.addCacheableUrl(getUrlFromSettings());
        int[] scrollPos = loadLastScrollPosition();
        client.setScrollPosition(scrollPos[0], scrollPos[1]);

        webView.setWebViewClient(client);
    }

    private String getUrlFromSettings() {
        SharedPreferences prefs = getPreferences(getContext());
        String url = prefs.getString(getString(R.string.settings_web_url), getString(R.string.default_web_url));

        // possible for http://mysite.com can to have dns record and none for http://www.mysite.com
        // so if just http:// we assume that's what they meant but if http:// missing we assume they want the www.
        if (!url.startsWith("http://"))
            url = "http://www." + url.replace("wwww.", "");

        return url;
    }

    private int[] loadLastScrollPosition() {
        SharedPreferences prefs = getPreferences(getContext());
        String[] scrollPos = prefs.getString(getString(R.string.web_scroll_position), "0,0").split(",");
        int x = Integer.parseInt(scrollPos[0]);
        int y = Integer.parseInt(scrollPos[1]);

        return new int[]{x, y};
    }

    private void saveScrollPosition() {
        if (getView() == null)
            return;
        WebView view = (WebView) getView().findViewById(R.id.webView);
        SharedPreferences prefs = getPreferences(getContext());

        String scrollPos = view.getScrollX() + "," + view.getScrollY();
        prefs.edit().putString(getString(R.string.web_scroll_position), scrollPos).apply();
    }
}
