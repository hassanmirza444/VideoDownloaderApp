package com.upscapesoft.videodownloaderapp.browser;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.webkit.WebSettingsCompat;
import androidx.webkit.WebViewFeature;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.upscapesoft.videodownloaderapp.MyApp;
import com.upscapesoft.videodownloaderapp.R;
import com.upscapesoft.videodownloaderapp.activities.MainActivity;
import com.upscapesoft.videodownloaderapp.fragments.base.BaseFragment;
import com.upscapesoft.videodownloaderapp.utils.HistorySQLite;
import com.upscapesoft.videodownloaderapp.utils.Utils;
import com.upscapesoft.videodownloaderapp.utils.VisitedPage;
import com.upscapesoft.videodownloaderapp.views.CustomMediaController;
import com.upscapesoft.videodownloaderapp.views.CustomVideoView;

import java.util.Arrays;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

public class BrowserWindow extends BaseFragment implements View.OnClickListener, MainActivity.OnBackPressedListener {

    private String url;
    private View view;
    private TouchableWebView page;
    private SSLSocketFactory defaultSSLSF;

    private FrameLayout videoFoundTV;
    private CustomVideoView videoFoundView;
    private FloatingActionButton videosFoundHUD;

    private View foundVideosWindow;
    private VideoList videoList;
    private ImageView foundVideosClose;

    private ProgressBar loadingPageProgress;

    private int orientation;
    private boolean loadedFirsTime;

    private List<String> blockedWebsites;
    private BottomSheetDialog dialog;

    private Activity activity;
    private InterstitialAd mInterstitialAd;
    private Context context;

    public BrowserWindow(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void onClick(View v) {
        if (v == videosFoundHUD) {
            if (videoList.getSize() > 0) {
                dialog.show();
            } else {
                showGuide();
            }
        } else if (v == foundVideosClose) {
            dialog.dismiss();
        }
    }

    private void showGuide() {
        final Dialog guide = new Dialog(getContext());
        guide.setContentView(R.layout.popup_guide_download_lay);

        Button okayBtn = guide.findViewById(R.id.okayBtn);
        okayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                guide.dismiss();
            }
        });

        guide.show();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle data = getArguments();
        url = data.getString("url");
        defaultSSLSF = HttpsURLConnection.getDefaultSSLSocketFactory();
        blockedWebsites = Arrays.asList(getResources().getStringArray(R.array.blocked_sites));
        setRetainInstance(true);
        context = getContext();
    }

    private void createVideosFoundTV() {
        videoFoundTV = view.findViewById(R.id.videoFoundTV);
        videoFoundView = view.findViewById(R.id.videoFoundView);
        CustomMediaController mediaFoundController = view.findViewById(R.id.mediaFoundController);
        mediaFoundController.setFullscreenEnabled();
        videoFoundView.setMediaController(mediaFoundController);
        videoFoundTV.setVisibility(View.GONE);
    }

    private void createVideosFoundHUD() {
        videosFoundHUD = view.findViewById(R.id.videosFoundHUD);
        videosFoundHUD.setOnClickListener(this);
    }

    private void createFoundVideosWindow() {

        dialog = new BottomSheetDialog(activity);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.bottom_sheet_video_qualities_lay);

        RecyclerView qualities = dialog.findViewById(R.id.qualities_rv);
        ImageView dismiss = dialog.findViewById(R.id.dismiss);

        assert dismiss != null;
        dismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        foundVideosWindow = view.findViewById(R.id.foundVideosWindow);
        if (videoList != null) {
            videoList.recreateVideoList(qualities);
        } else {
            videoList = new VideoList(activity, qualities) {
                @Override
                void onItemDeleted() {
                    dialog.dismiss();
                    if (mInterstitialAd != null) {
                        mInterstitialAd.show(getBaseActivity());
                    }
                    updateFoundVideosBar();
                }

                @Override
                void onVideoPlayed(String url) {
                    dialog.dismiss();
                    updateVideoPlayer(url);
                }
            };
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        if (view == null || getResources().getConfiguration().orientation != orientation) {
            int visibility = View.VISIBLE;
            if (view != null) {
                visibility = view.getVisibility();
            }
            view = inflater.inflate(R.layout.browser_lay, container, false);
            view.setVisibility(visibility);
            if (page == null) {
                page = view.findViewById(R.id.page);
            } else {
                View page1 = view.findViewById(R.id.page);
                ((ViewGroup) view).removeView(page1);
                ((ViewGroup) page.getParent()).removeView(page);
                ((ViewGroup) view).addView(page);
                ((ViewGroup) view).bringChildToFront(view.findViewById(R.id.videosFoundHUD));
                ((ViewGroup) view).bringChildToFront(view.findViewById(R.id.foundVideosWindow));
            }
            loadingPageProgress = view.findViewById(R.id.loadingPageProgress);
            loadingPageProgress.setVisibility(View.GONE);

            createVideosFoundHUD();
            createVideosFoundTV();
            createFoundVideosWindow();
            updateFoundVideosBar();
            webViewLightDark();
        }

        return view;
    }

    @SuppressLint({"SetJavaScriptEnabled", "JavascriptInterface"})
    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        if (!loadedFirsTime) {
            page.getSettings().setJavaScriptEnabled(true);
            page.getSettings().setDomStorageEnabled(true);
            page.getSettings().setAllowUniversalAccessFromFileURLs(true);
            page.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
            page.setWebViewClient(new WebViewClient() {//it seems not setting webclient, launches
                //default browser instead of opening the page in webview
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                    if(blockedWebsites.contains(Utils.Companion.getBaseDomain(request.getUrl().toString()))){
                        Log.d("vdd", "URL : " + request.getUrl().toString());
                        new AlertDialog.Builder(activity)
                                .setMessage("Youtube is not supported according to google policy.")
                                .setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                                .create()
                                .show();
                        return true;
                    }
                    return super.shouldOverrideUrlLoading(view, request);
                }

                @Override
                public void onPageStarted(final WebView webview, final String url, Bitmap favicon) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            EditText urlBox = getBaseActivity().findViewById(R.id.inputURLText);
                            urlBox.setText(url);
                            urlBox.setSelection(urlBox.getText().length());
                            BrowserWindow.this.url = url;
                        }
                    });
                    view.findViewById(R.id.loadingProgress).setVisibility(View.GONE);
                    loadingPageProgress.setVisibility(View.VISIBLE);
                    super.onPageStarted(webview, url, favicon);
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    loadingPageProgress.setVisibility(View.GONE);
                }

                @Override
                public void onLoadResource(final WebView view, final String url) {
                    Log.d("fb :", "URL: " + url);
                    final String viewUrl = view.getUrl();
                    final String title = view.getTitle();

                    new VideoContentSearch(activity, url, viewUrl, title) {
                        @Override
                        public void onStartInspectingURL() {
                            Utils.Companion.disableSSLCertificateChecking();
                        }

                        @Override
                        public void onFinishedInspectingURL(boolean finishedAll) {
                            HttpsURLConnection.setDefaultSSLSocketFactory(defaultSSLSF);
                        }

                        @Override
                        public void onVideoFound(String size, String type, String link, String name, String page, boolean chunked, String website, boolean audio) {
                            videoList.addItem(size, type, link, name, page, chunked, website, audio);
                            updateFoundVideosBar();
                        }
                    }.start();
                }

                @Override
                public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                    if (activity != null) {
                        Log.d("VDDebug", "Url: " + url);
                        if (activity.getSharedPreferences("settings", 0).getBoolean(getString(R
                                .string.adBlockON), true)
                                && (url.contains("ad") || url.contains("banner") || url.contains("pop"))
                                && getBaseActivity().getBrowserManager().checkUrlIfAds(url)) {
                            Log.d("VDDebug", "Ads detected: " + url);
                            return new WebResourceResponse(null, null, null);
                        }
                    }
                    return super.shouldInterceptRequest(view, url);
                }

                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && getBaseActivity() !=
                            null) {
                        if (MyApp.getInstance().getSharedPreferences("settings", 0).getBoolean(getString
                                (R.string.adBlockON), true)
                                && (request.getUrl().toString().contains("ad") ||
                                request.getUrl().toString().contains("banner") ||
                                request.getUrl().toString().contains("pop"))
                                && getBaseActivity().getBrowserManager().checkUrlIfAds(request.getUrl()
                                .toString())) {
                            Log.i("VDInfo", "Ads detected: " + request.getUrl().toString());
                            return new WebResourceResponse(null, null, null);
                        } else return null;
                    } else {
                        return shouldInterceptRequest(view, request.getUrl().toString());
                    }
                }


            });
            page.setWebChromeClient(new WebChromeClient() {
                @Override
                public void onProgressChanged(WebView view, int newProgress) {
                    loadingPageProgress.setProgress(newProgress);
                }

                @Override
                public void onReceivedTitle(WebView view, String title) {
                    super.onReceivedTitle(view, title);
                    videoList.deleteAllItems();
                    updateFoundVideosBar();
                    VisitedPage vp = new VisitedPage();
                    vp.title = title;
                    vp.link = view.getUrl();
                    HistorySQLite db = new HistorySQLite(activity);
                    db.addPageToHistory(vp);
                    db.close();
                }

                @Override
                public Bitmap getDefaultVideoPoster() {
                    return Bitmap.createBitmap(50, 50, Bitmap.Config.ARGB_8888);
                }


            });

            page.loadUrl(url);
            loadedFirsTime = true;
        } else {
            EditText urlBox = getBaseActivity().findViewById(R.id.inputURLText);
            urlBox.setText(url);
            urlBox.setSelection(urlBox.getText().length());
        }
    }

    private void webViewLightDark() {

        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        switch (currentNightMode) {
            case Configuration.UI_MODE_NIGHT_YES:
                if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
                    WebSettingsCompat.setForceDark(page.getSettings(), WebSettingsCompat.FORCE_DARK_ON);
                }
                break;

            case Configuration.UI_MODE_NIGHT_NO:
                if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
                    WebSettingsCompat.setForceDark(page.getSettings(), WebSettingsCompat.FORCE_DARK_OFF);
                }
                break;

            default:
                //

        }

    }

    @Override
    public void onDestroy() {
        page.stopLoading();
        page.destroy();
        super.onDestroy();
    }

    private void updateFoundVideosBar() {
        if (videoList.getSize() > 0) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    videosFoundHUD.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.tint_color));
                    Animation expandIn = AnimationUtils.loadAnimation(activity.getApplicationContext(), R.anim.expand_in);
                    videosFoundHUD.startAnimation(expandIn);
                }
            });

        } else {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    videosFoundHUD.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.grey));
                    if (foundVideosWindow.getVisibility() == View.VISIBLE)
                        foundVideosWindow.setVisibility(View.GONE);
                }
            });
        }
    }

    private void updateVideoPlayer(String url){
        videoFoundTV.setVisibility(View.VISIBLE);
        Uri uri = Uri.parse(url);
        videoFoundView.setVideoURI(uri);
        videoFoundView.start();
    }

    @Override
    public void onBackpressed() {
        if (foundVideosWindow.getVisibility() == View.VISIBLE && !videoFoundView.isPlaying() && videoFoundTV.getVisibility() == View.GONE) {
            foundVideosWindow.setVisibility(View.GONE);
        } else if (videoFoundView.isPlaying() || videoFoundTV.getVisibility() == View.VISIBLE) {
            videoFoundView.closePlayer();
            videoFoundTV.setVisibility(View.GONE);
        } else if (page.canGoBack()) {
            page.goBack();
        } else {
            getBaseActivity().getBrowserManager().closeWindow(BrowserWindow.this);
        }
    }

    public String getUrl() {
        return url;
    }

    @Override
    public void onPause() {
        super.onPause();
        page.onPause();
        Log.d("debug", "onPause: ");
    }

    @Override
    public void onResume() {
        super.onResume();
        page.onResume();
        Log.d("debug", "onResume: ");
    }


}
