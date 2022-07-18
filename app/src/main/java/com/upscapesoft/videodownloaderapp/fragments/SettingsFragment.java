package com.upscapesoft.videodownloaderapp.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.widget.SwitchCompat;

import com.upscapesoft.videodownloaderapp.R;
import com.upscapesoft.videodownloaderapp.activities.HistoryActivity;
import com.upscapesoft.videodownloaderapp.activities.MainActivity;
import com.upscapesoft.videodownloaderapp.activities.PremiumActivity;
import com.upscapesoft.videodownloaderapp.fragments.base.BaseFragment;
import com.upscapesoft.videodownloaderapp.helper.AdController;

public class SettingsFragment extends BaseFragment implements MainActivity.OnBackPressedListener, View.OnClickListener {
    private View view;

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        setRetainInstance(true);

        if (view == null) {
            view = inflater.inflate(R.layout.fragment_settings, container, false);

            getBaseActivity().setOnBackPressedListener(this);
            final SharedPreferences prefs = getActivity().getSharedPreferences("settings", 0);

            //Back
            ImageView btnSettingsBack = view.findViewById(R.id.backBtn);
            btnSettingsBack.setOnClickListener(this);

            // Switch vibrate switch
            SwitchCompat vibrateSwitch = view.findViewById(R.id.vibrateSwitch);
            boolean vibrateON = prefs.getBoolean(getString(R.string.vibrateON), true);
            vibrateSwitch.setChecked(vibrateON);
            vibrateSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    prefs.edit().putBoolean(getString(R.string.vibrateON), isChecked).commit();
                }
            });

            // Switch sound switch
            SwitchCompat soundSwitch = view.findViewById(R.id.soundSwitch);
            boolean soundON = prefs.getBoolean(getString(R.string.soundON), true);
            soundSwitch.setChecked(soundON);
            soundSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    prefs.edit().putBoolean(getString(R.string.soundON), isChecked).commit();
                }
            });

            // Switch ad blocker switch
            SwitchCompat adBlockerSwitch = view.findViewById(R.id.adBlockerSwitch);
            boolean adBlockOn = prefs.getBoolean(getString(R.string.adBlockON), true);
            adBlockerSwitch.setChecked(adBlockOn);
            adBlockerSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    prefs.edit().putBoolean(getString(R.string.adBlockON), isChecked).commit();
                }
            });

            view.findViewById(R.id.history).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getBaseActivity(), HistoryActivity.class);
                    startActivity(intent);
                }
            });

            /*admob*/
            LinearLayout adContainer = view.findViewById(R.id.banner_container);
            AdController.loadBannerAd(getActivity(), adContainer);
            AdController.loadInterAd(getActivity());

        }
        return view;
    }

    @Override
    public void onBackpressed() {
        getBaseActivity().getBrowserManager().unhideCurrentWindow();
        getFragmentManager().beginTransaction().remove(this).commit();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.backBtn) {
            getActivity().onBackPressed();
        }
    }

}
