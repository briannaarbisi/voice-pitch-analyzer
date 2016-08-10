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
import android.widget.Toast;
import de.lilithwittmann.voicepitchanalyzer.R;

/**
 * Reading material for display while doing a recording. Loads a user input url from settings.
 */
public class ReadingWebPageFragment extends Fragment {
    public ReadingWebPageFragment()
    {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reading_web_page, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedPreferences prefs = getPreferences(getContext());
        String webPage = prefs.getString(getString(R.string.settings_web_url), "google.com");

        WebView webView = (WebView) view.findViewById(R.id.webView);
        initWebView(webView);

        if (!webPage.startsWith("http://"))
            webPage = "http://www." + webPage;

        webView.loadUrl(webPage);
    }

    private void initWebView(WebView webView) {
        // todo caching
//        webView.getSettings().setJavaScriptEnabled(true);

        final Activity activity = getActivity();
        webView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                activity.setProgress(progress * 1000);
            }
        });

        webView.setWebViewClient(new WebViewClient() {
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(activity, "Error loading user input web page, "
                        + description, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(
                    context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
    }
}
