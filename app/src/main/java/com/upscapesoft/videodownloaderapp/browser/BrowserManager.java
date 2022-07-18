package com.upscapesoft.videodownloaderapp.browser;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.upscapesoft.videodownloaderapp.R;
import com.upscapesoft.videodownloaderapp.fragments.base.BaseFragment;
import com.upscapesoft.videodownloaderapp.utils.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class BrowserManager extends BaseFragment {

    private AdBlocker adBlock;
    private List<BrowserWindow> windows;
    private List<String> blockedWebsites;

    private Activity activity;

    public BrowserManager(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        Log.d("debug", "Browser Manager added");
        windows = new ArrayList<>();
        File file = new File(getActivity().getFilesDir(), "ad_filters.dat");
        try {
            if (file.exists()) {
                Log.d("debug", "file exists");
                FileInputStream fileInputStream = new FileInputStream(file);
                try (ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream)) {
                    adBlock = (AdBlocker) objectInputStream.readObject();
                }
                fileInputStream.close();
            } else {
                adBlock = new AdBlocker();
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream)) {
                    objectOutputStream.writeObject(adBlock);
                }
                fileOutputStream.close();
            }
        } catch (IOException | ClassNotFoundException ignored) {
            //
        }
        updateAdFilters();
        blockedWebsites = Arrays.asList(getResources().getStringArray(R.array.blocked_sites));
    }

    public void newWindow(String url) {
        if(blockedWebsites.contains(Utils.Companion.getBaseDomain(url))){
            final Dialog dialog = new Dialog(getContext());
            dialog.setContentView(R.layout.popup_youtube_not_supported_lay);

            Button okayBtn = dialog.findViewById(R.id.okayBtn);
            okayBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });

            dialog.show();
        } else {
            Bundle data = new Bundle();
            data.putString("url", url);
            BrowserWindow window = new BrowserWindow(activity);
            window.setArguments(data);
            getFragmentManager().beginTransaction()
                    .add(R.id.homeContainer, window, null)
                    .commit();
            windows.add(window);
            getBaseActivity().setOnBackPressedListener(window);
            if (windows.size() > 1) {
                window = windows.get(windows.size() - 2);
                if (window != null && window.getView() != null) {
                    window.getView().setVisibility(View.GONE);
                    window.onPause();
                }
            }
        }
    }

    public void closeWindow(BrowserWindow window) {
        final EditText inputURLText = getBaseActivity().findViewById(R.id.inputURLText);
        windows.remove(window);
        getFragmentManager().beginTransaction().remove(window).commit();
        if (!windows.isEmpty()) {
            BrowserWindow topWindow = windows.get(windows.size() - 1);
            if (topWindow != null && topWindow.getView() != null) {
                topWindow.onResume();
                topWindow.getView().setVisibility(View.VISIBLE);
            }
            if (topWindow != null) {

                inputURLText.setText(topWindow.getUrl());
                inputURLText.setSelection(inputURLText.getText().length());
                getBaseActivity().setOnBackPressedListener(topWindow);
            }
        } else {
            inputURLText.getText().clear();
            getBaseActivity().setOnBackPressedListener(null);
        }
    }

    public void closeAllWindow() {
        if(!windows.isEmpty()){
            for (Iterator<BrowserWindow> iterator = windows.iterator(); iterator.hasNext(); ) {
                BrowserWindow window = iterator.next();
                getFragmentManager().beginTransaction().remove(window).commit();
                iterator.remove();
            }
            getBaseActivity().setOnBackPressedListener(null);
        }else {
            getBaseActivity().setOnBackPressedListener(null);
        }
    }

    public void hideCurrentWindow() {
        if (!windows.isEmpty()) {
            BrowserWindow topWindow = windows.get(windows.size() - 1);
            if (topWindow.getView() != null) {
                topWindow.getView().setVisibility(View.GONE);
            }
        }
    }

    public void unhideCurrentWindow() {
        if (!windows.isEmpty()) {
            BrowserWindow topWindow = windows.get(windows.size() - 1);
            if (topWindow.getView() != null) {
                topWindow.getView().setVisibility(View.VISIBLE);
                getBaseActivity().setOnBackPressedListener(topWindow);
            }
        } else {
            getBaseActivity().setOnBackPressedListener(null);
        }
    }

    public void pauseCurrentWindow() {
        if (!windows.isEmpty()) {
            BrowserWindow topWindow = windows.get(windows.size() - 1);
            if (topWindow.getView() != null) {
                topWindow.onPause();
            }
        }
    }

    public void resumeCurrentWindow() {
        if (!windows.isEmpty()) {
            BrowserWindow topWindow = windows.get(windows.size() - 1);
            if (topWindow.getView() != null) {
                topWindow.onResume();
                getBaseActivity().setOnBackPressedListener(topWindow);
            }
        } else {
            getBaseActivity().setOnBackPressedListener(null);
        }
    }

    public void updateAdFilters() {
        adBlock.update(getContext());
    }

    public boolean checkUrlIfAds(String url) {
        return adBlock.checkThroughFilters(url);
    }
}
