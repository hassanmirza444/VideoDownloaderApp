package com.upscapesoft.videodownloaderapp.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.upscapesoft.videodownloaderapp.R;
import com.upscapesoft.videodownloaderapp.activities.MainActivity;
import com.upscapesoft.videodownloaderapp.fragments.base.BaseFragment;
import com.upscapesoft.videodownloaderapp.interfaces.Tracking;

public class DownloadsFragment extends BaseFragment implements MainActivity.OnBackPressedListener, Tracking, DownloadsInProgressFragment.OnNumDownloadsInProgressChangeListener,
        DownloadsCompletedFragment.OnNumDownloadsCompletedChangeListener, DownloadsInProgressFragment.OnAddDownloadedVideoToCompletedListener {
    private static final String PROGRESS = "downloadsInProgress";
    private View view;
    private Handler mainHandler;
    private Tracking tracking;

    private ViewPager pager;
    private DownloadsInProgressFragment downloadsInProgress;

    private Activity activity;

    public DownloadsFragment(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void onDestroy() {
        Fragment fragment;
        if ((fragment = getFragmentManager().findFragmentByTag(PROGRESS)) != null) {
            getFragmentManager().beginTransaction().remove(fragment).commit();
        }
        super.onDestroy();
    }

    
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        setRetainInstance(true);

        if (view == null) {
            view = inflater.inflate(R.layout.downloads_progress_lay, container, false);
            getBaseActivity().setOnBackPressedListener(this);

            mainHandler = new Handler(Looper.getMainLooper());
            tracking = new Tracking();

            pager = view.findViewById(R.id.progress_pager);
            pager.setAdapter(new PagerAdapter());

            downloadsInProgress = new DownloadsInProgressFragment(activity);

            downloadsInProgress.setOnNumDownloadsInProgressChangeListener(this);

            getFragmentManager().beginTransaction().add(pager.getId(), downloadsInProgress,
                    PROGRESS).commit();

            downloadsInProgress.setTracking(this);

            downloadsInProgress.setOnAddDownloadedVideoToCompletedListener(this);

        }

        return view;
    }

    @Override
    public void onViewCreated(View view,  Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        pager.setCurrentItem(0);
    }

    @Override
    public void onBackpressed() {
        getBaseActivity().getBrowserManager().unhideCurrentWindow();
        getFragmentManager().beginTransaction().remove(this).commit();
    }

    @Override
    public void onNumDownloadsInProgressChange() {
        //
    }

    @Override
    public void onNumDownloadsCompletedChange() {
        //
    }

    @Override
    public void onAddDownloadedVideoToCompleted(String name, String type) {
        //
    }

    class Tracking implements Runnable {

        @Override
        public void run() {

            if (getFragmentManager() != null && getFragmentManager().findFragmentByTag
                    (PROGRESS) != null) {
                downloadsInProgress.updateDownloadItem();
            }
            mainHandler.postDelayed(this, 1000);
        }
    }

    public void startTracking() {
        getActivity().runOnUiThread(tracking);
    }

    public void stopTracking() {
        mainHandler.removeCallbacks(tracking);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (getFragmentManager().findFragmentByTag(PROGRESS) != null) {
                    downloadsInProgress.updateDownloadItem();
                }
            }
        });
    }

    class PagerAdapter extends androidx.viewpager.widget.PagerAdapter {
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            return downloadsInProgress;
        }

        @Override
        public int getCount() {
            return 1;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            //nada
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return ((Fragment) object).getView() == view;
        }
    }

}
