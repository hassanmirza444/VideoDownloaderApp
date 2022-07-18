package com.upscapesoft.videodownloaderapp.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.infideap.drawerbehavior.Advance3DDrawerLayout;
import com.upscapesoft.videodownloaderapp.MyApp;
import com.upscapesoft.videodownloaderapp.R;
import com.upscapesoft.videodownloaderapp.browser.BrowserManager;
import com.upscapesoft.videodownloaderapp.data.Constants;
import com.upscapesoft.videodownloaderapp.fragments.DownloadsFragment;
import com.upscapesoft.videodownloaderapp.fragments.DownloadsCompFragment;
import com.upscapesoft.videodownloaderapp.fragments.SettingsFragment;
import com.upscapesoft.videodownloaderapp.helper.WebConnect;
import com.upscapesoft.videodownloaderapp.utils.ThemeSettings;
import com.upscapesoft.videodownloaderapp.utils.Utils;
/*import com.vimalcvs.switchdn.DayNightSwitch;
import com.vimalcvs.switchdn.DayNightSwitchAnimListener;
import com.vimalcvs.switchdn.DayNightSwitchListener;*/

public class MainActivity extends AppCompatActivity implements View.OnClickListener, TextView.OnEditorActionListener{

    private static final String DOWNLOAD = "Downloads";
    private static final String HISTORY = "History";
    private static final String SETTING = "Settings";
    private EditText searchTextBar;
    private ImageView btnSearchCancel;
    private BrowserManager browserManager;
    private Uri appLinkData;
    private FragmentManager manager;
    private BottomNavigationView navView;

    private ImageView appMenuBtn;
    private ImageView appSettingsBtn;
    private Advance3DDrawerLayout appDrawerLayout;
   // private DayNightSwitch dayNightSwitch;
    private RelativeLayout searchView;
    private TextView appTitleName;

    private Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        activity = this;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.primary));
        }

        Intent appLinkIntent = getIntent();
        appLinkData = appLinkIntent.getData();

        manager = this.getSupportFragmentManager();
        // This is for creating browser manager fragment
        if ((browserManager = (BrowserManager) this.getSupportFragmentManager().findFragmentByTag("BM")) == null)
        {
            browserManager = new BrowserManager(MainActivity.this);

            manager.beginTransaction()
                    .add(browserManager, "BM").commit();

        }

        // Bottom navigation
        navView = findViewById(R.id.bottomNavigationView);
        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);


        ImageView instagram = findViewById(R.id.instagramBtn);
        ImageView facebook = findViewById(R.id.facebookBtn);
        ImageView twitter = findViewById(R.id.twitterBtn);
        ImageView reddit = findViewById(R.id.redditBtn);
        ImageView tumblr = findViewById(R.id.tumbleBtn);
        ImageView tiktok = findViewById(R.id.tiktokBtn);


        instagram.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                browserManager.newWindow("https://www.instagram.com/");
            }
        });

        facebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                browserManager.newWindow("https://www.facebook.com/");
            }
        });

        twitter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                browserManager.newWindow("https://www.twitter.com/");
            }
        });

        reddit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                browserManager.newWindow("https://www.reddit.com/");
            }
        });

        tumblr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                browserManager.newWindow("https://www.tumblr.com/");
            }
        });

        tiktok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                browserManager.newWindow("https://www.tiktok.com/");
            }
        });

        Button guide = findViewById(R.id.howToUseBtn);
        guide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, HelpActivity.class);
                startActivity(intent);
            }
        });

        setUPBrowserToolbarView();
        initViews();
        contentMain();
        appDrawer();
        lightDarkMode();
    }

    private void initViews() {
        appMenuBtn = findViewById(R.id.appMenuBtn);
        appDrawerLayout = findViewById(R.id.appDrawerLayout);
        //dayNightSwitch = findViewById(R.id.dayNightSwitch);
        appSettingsBtn = findViewById(R.id.appSettingsBtn);
        searchView = findViewById(R.id.searchView);
        appTitleName = findViewById(R.id.appTitleName);
    }

    private void contentMain() {
        appMenuBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openAppDrawer();
            }
        });

        appSettingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                settingsClicked();
            }
        });
    }

    private void appDrawer() {
        //appDrawerLayout = findViewById(R.id.appDrawerLayout)
        appDrawerLayout.setViewScale(GravityCompat.START, 0.9f); //set height scale for main view (0f to 1f)
        appDrawerLayout.setViewElevation(
                GravityCompat.START,
                30f
        ); //set main view elevation when drawer open (dimension)
        appDrawerLayout.setViewScrimColor(
                GravityCompat.START,
                ContextCompat.getColor(this, R.color.primary)
        ); //set drawer overlay coloe (color)
        appDrawerLayout.setDrawerElevation(30f);

        appDrawerLayout.setRadius(GravityCompat.START, 25f);
        appDrawerLayout.setViewRotation(GravityCompat.START, 0f);
        appDrawerLayout.closeDrawer(GravityCompat.START);
        appDrawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {

            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {

            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });

        initAppDrawer();
    }

    private void closeAppDrawer() {
        appDrawerLayout.closeDrawer(
                GravityCompat.START
        );
    }

    private void openAppDrawer() {
        appDrawerLayout.openDrawer(
                GravityCompat.START
        );
    }

    private void initAppDrawer() {
        findViewById(R.id.navHomeBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeAppDrawer();
            }
        });

        findViewById(R.id.navInAppPurchasesBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeAppDrawer();
                startActivity(new Intent(MainActivity.this, PremiumActivity.class));
            }
        });

        findViewById(R.id.navShareBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeAppDrawer();
                Utils.Companion.shareApp(activity, getResources().getString(R.string.app_name));
            }
        });

        findViewById(R.id.navRateBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeAppDrawer();
                Utils.Companion.rateApp(activity);
            }
        });

        findViewById(R.id.navPrivacyPolicyBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeAppDrawer();
                Utils.Companion.openUrl(activity, Constants.PRIVACY_POLICY_URL);
            }
        });

        findViewById(R.id.navAboutBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeAppDrawer();
                startActivity(new Intent(MainActivity.this, AboutActivity.class));
            }
        });

        findViewById(R.id.navMoreBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeAppDrawer();
                Utils.Companion.moreApps(activity);
            }
        });
    }

    private void lightDarkMode() {
     /*   dayNightSwitch.setDuration(450);
        dayNightSwitch.setIsNight(ThemeSettings.getInstance(this).nightMode);
        dayNightSwitch.setListener(new DayNightSwitchListener() {
            @Override
            public void onSwitch(boolean isNight) {
                ThemeSettings.getInstance(MainActivity.this).nightMode = isNight;
                ThemeSettings.getInstance(MainActivity.this).refreshTheme();
            }
        });

        dayNightSwitch.setAnimListener(new DayNightSwitchAnimListener() {
            @Override
            public void onAnimEnd() {
                Intent intent = new Intent(MainActivity.this, MainActivity.this.getClass());
                intent.putExtras(getIntent());
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            }
            @Override
            public void onAnimValueChanged(float v) {

            }
            @Override
            public void onAnimStart() {
            }
        });*/
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ThemeSettings.getInstance(this).save(this);
    }

    private void setUPBrowserToolbarView(){

        // Toolbar search
        btnSearchCancel = findViewById(R.id.clearBtn);
        btnSearchCancel.setOnClickListener(this);
        FloatingActionButton btnSearch = findViewById(R.id.searchBtn);
        searchTextBar = findViewById(R.id.inputURLText);

        /*hide/show clear button in search view*/
        TextWatcher searchViewTextWatcher = new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.toString().trim().length()==0){
                    btnSearchCancel.setVisibility(View.GONE);
                } else {
                    btnSearchCancel.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //nada
            }

            @Override
            public void afterTextChanged(Editable s) {
                //nada
            }
        };
        searchTextBar.addTextChangedListener(searchViewTextWatcher);
        searchTextBar.setOnEditorActionListener(this);
        btnSearch.setOnClickListener(this);

        //Toolbar home button
        ImageView toolbarHome = findViewById(R.id.homeBtn);
        toolbarHome.setOnClickListener(this);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navHome:
                    homeClicked();
                    searchView.setVisibility(View.VISIBLE);
                    appTitleName.setText(getResources().getString(R.string.app_name));
                    return true;
                case R.id.navDownloads:
                    downloadClicked();
                    searchView.setVisibility(View.GONE);
                    appTitleName.setText("Downloads");
                    return true;
                case R.id.navProgress:
                    historyClicked();
                    searchView.setVisibility(View.GONE);
                    appTitleName.setText("Download Progress");
                    return true;
                default:
                    break;
            }
            return false;
        }
    };

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.clearBtn:
                searchTextBar.getText().clear();
                break;
            case R.id.homeBtn:
                searchTextBar.getText().clear();
                getBrowserManager().closeAllWindow();
                break;
            case R.id.searchBtn:
                new WebConnect(searchTextBar, this).connect();
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
        boolean handled = false;
        if (actionId == EditorInfo.IME_ACTION_GO) {
            new WebConnect(searchTextBar, this).connect();
        }
        return handled;
    }

    @Override
    public void onBackPressed() {
        if (manager.findFragmentByTag(DOWNLOAD) != null ||
                manager.findFragmentByTag(HISTORY) != null) {
            MyApp.getInstance().getOnBackPressedListener().onBackpressed();
            browserManager.resumeCurrentWindow();
            navView.setSelectedItemId(R.id.bottomNavigationView);
        }
        else if (manager.findFragmentByTag(SETTING) != null) {
            MyApp.getInstance().getOnBackPressedListener().onBackpressed();
            browserManager.resumeCurrentWindow();
            navView.setVisibility(View.VISIBLE);
        }
        else if (MyApp.getInstance().getOnBackPressedListener() != null) {
            MyApp.getInstance().getOnBackPressedListener().onBackpressed();
        } else {
            new AlertDialog.Builder(this)
                    .setMessage("Are you sure you want to exit?")
                    .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            MainActivity.super.onBackPressed();
                        }
                    })
                    .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //nada
                        }
                    })
                    .create()
                    .show();
        }
    }

    public BrowserManager getBrowserManager() {
        return browserManager;
    }

    public interface OnBackPressedListener {
        void onBackpressed();
    }

    public void setOnBackPressedListener(OnBackPressedListener onBackPressedListener) {
        MyApp.getInstance().setOnBackPressedListener(onBackPressedListener);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (appLinkData != null) {
            browserManager.newWindow(appLinkData.toString());
        }
    }

    public void browserClicked() {
        browserManager.unhideCurrentWindow();
    }

    public void downloadClicked(){
        closeHistory();
        if (manager.findFragmentByTag(DOWNLOAD) == null) {
            browserManager.hideCurrentWindow();
            browserManager.pauseCurrentWindow();
            manager.beginTransaction().add(R.id.mainContent, new DownloadsCompFragment(), DOWNLOAD).commit();
        }
    }


    public void historyClicked(){
        closeDownloads();
        if (manager.findFragmentByTag(HISTORY) == null) {
            browserManager.hideCurrentWindow();
            browserManager.pauseCurrentWindow();
            manager.beginTransaction().add(R.id.mainContent, new DownloadsFragment(MainActivity.this), HISTORY).commit();
        }
    }

    private void settingsClicked(){
        if (manager.findFragmentByTag(SETTING) == null) {
            browserManager.hideCurrentWindow();
            browserManager.pauseCurrentWindow();
            navView.setVisibility(View.GONE);
            manager.beginTransaction().add(R.id.mainContent, new SettingsFragment(), SETTING).commit();
        }
    }

    public void homeClicked(){
        browserManager.unhideCurrentWindow();
        browserManager.resumeCurrentWindow();
        closeDownloads();
        closeHistory();
    }

    private void closeDownloads() {
        Fragment fragment = manager.findFragmentByTag(DOWNLOAD);
        if (fragment != null) {
            manager.beginTransaction().remove(fragment).commit();
        }
    }

    private void closeHistory() {
        Fragment fragment = manager.findFragmentByTag(HISTORY);
        if (fragment != null) {
            manager.beginTransaction().remove(fragment).commit();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        onRequestPermissionsResultCallback.onRequestPermissionsResult(requestCode, permissions,
                grantResults);
    }

    private ActivityCompat.OnRequestPermissionsResultCallback onRequestPermissionsResultCallback;

    public void setOnRequestPermissionsResultListener(ActivityCompat
                                                              .OnRequestPermissionsResultCallback
                                                              onRequestPermissionsResultCallback) {
        this.onRequestPermissionsResultCallback = onRequestPermissionsResultCallback;
    }

}
