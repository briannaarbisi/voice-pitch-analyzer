package de.lilithwittmann.voicepitchanalyzer.fragments;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.*;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import de.lilithwittmann.voicepitchanalyzer.R;
import de.lilithwittmann.voicepitchanalyzer.activities.AboutActivity;

public class SettingsFragment extends Fragment {
    private static final String LOG_TAG = SettingsFragment.class.getSimpleName();

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadSavedSettings();

        Button aboutButton = (Button) view.findViewById(R.id.aboutButton);
        aboutButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(getContext(), AboutActivity.class));
            }
        });

        Switch webPageSwitch = (Switch) getView().findViewById(R.id.webPageSwitch);
        webPageSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                getView().findViewById(R.id.editWebPageText).setEnabled(isChecked);
            }
        });
    }

    private void loadSavedSettings() {
        if (getView() == null)
            return;

        SharedPreferences sharedPref =
                getContext().getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        boolean isUseWebPageText = sharedPref.getBoolean(getString(R.string.settings_use_web_page), false);
        Switch webPageSwitch = (Switch) getView().findViewById(R.id.webPageSwitch);
        webPageSwitch.setChecked(isUseWebPageText);

        String wePageSite = sharedPref.getString(getString(R.string.settings_web_url), "google.com");
        EditText webPageEditText = (EditText) getView().findViewById(R.id.editWebPageText);
        webPageEditText.setText(wePageSite);
        webPageEditText.setEnabled(isUseWebPageText);
    }
}
