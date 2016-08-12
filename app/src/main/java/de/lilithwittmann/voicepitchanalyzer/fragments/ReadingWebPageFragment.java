package de.lilithwittmann.voicepitchanalyzer.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.*;
import de.lilithwittmann.voicepitchanalyzer.R;
import de.lilithwittmann.voicepitchanalyzer.utils.CacheableWebViewClient;

/**
 * Reading material for display while doing a recording. Loads a user input url from settings.
 */
public class ReadingWebPageFragment extends Fragment {
    public ReadingWebPageFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reading_web_page, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        WebView webView = (WebView) view.findViewById(R.id.webView);
        initWebView(webView);
        webView.loadUrl(getUrlFromSettings());
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

        webView.setWebViewClient(client);
    }

    private String getUrlFromSettings() {
        SharedPreferences prefs = getPreferences(getContext());
        String webPage = prefs.getString(getString(R.string.settings_web_url), "google.com");

        // possible for http://mysite.com can to have dns record and none for http://www.mysite.com
        // so if just http:// we assume that's what they meant but if http:// missing we assume they want the www.
        if (!webPage.startsWith("http://"))
            webPage = "http://www." + webPage.replace("wwww.","");

        return webPage;
    }
}
