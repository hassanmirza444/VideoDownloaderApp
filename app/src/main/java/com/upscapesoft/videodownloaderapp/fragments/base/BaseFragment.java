package com.upscapesoft.videodownloaderapp.fragments.base;

import androidx.fragment.app.Fragment;

import com.upscapesoft.videodownloaderapp.MyApp;
import com.upscapesoft.videodownloaderapp.activities.MainActivity;

public class BaseFragment extends Fragment {

    public MainActivity getBaseActivity() {
        return (MainActivity) getActivity();
    }

    public MyApp getMyApp() {
        return (MyApp) getActivity().getApplication();
    }
}
