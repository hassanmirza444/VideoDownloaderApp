package com.upscapesoft.videodownloaderapp.helper;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.widget.Toast;

public abstract class DownloadPermissionHandler extends PermissionsManager implements
        PreferenceManager.OnActivityResultListener {
    private static final String PERMISSION_DENIED = "Can't download; Necessary PERMISSIONS denied.";
    private Activity activity;

    public abstract void onPermissionGranted();

    protected DownloadPermissionHandler(Activity activity) {
        super(activity);
        this.activity = activity;
    }

    @Override
    public void showRequestPermissionRationale() {
        showPermissionSummaryDialog(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                requestPermissions();
            }
        });
    }

    @Override
    public void requestDisallowedAction() {
        SharedPreferences prefs = activity.getSharedPreferences
                ("settings", 0);
        boolean requestDisallowed = prefs.getBoolean("requestDisallowed",
                false);
        if (requestDisallowed) {
            showPermissionSummaryDialog(new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    new AlertDialog.Builder(activity)
                            .setMessage("Go to Settings?")
                            .setPositiveButton("Yes", new
                                    DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            activity.startActivityForResult(new Intent(android
                                                    .provider.Settings
                                                    .ACTION_APPLICATION_DETAILS_SETTINGS,
                                                    Uri.fromParts("package", activity
                                                                    .getPackageName(),
                                                            null)), 1337);
                                        }
                                    })
                            .setNegativeButton(
                                    "No", new
                                            DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    Toast.makeText(activity, PERMISSION_DENIED, Toast.LENGTH_LONG).show();
                                                }
                                            })
                            .create()
                            .show();

                }
            });
        } else {
            prefs.edit().putBoolean("requestDisallowed", true).apply();
            onPermissionsDenied();
        }
    }

    @Override
    public void onPermissionsGranted() {
        onPermissionGranted();
    }

    @Override
    public void onPermissionsDenied() {
        Toast.makeText(activity, PERMISSION_DENIED +
                " Try again", Toast.LENGTH_LONG).show();
    }

    private void showPermissionSummaryDialog(DialogInterface.OnClickListener
                                                     okListener) {
        new AlertDialog.Builder(activity)
                .setPositiveButton("OK", okListener)
                .setMessage("This feature requires WRITE_EXTERNAL_STORAGE " +
                        "permission to save downloaded videos into the Download " +
                        "folder. Make sure to grant this permission. Otherwise, " +
                        "downloading videos is not possible.")
                .create()
                .show();
    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1337) {
            PermissionsManager downloadsPermMgr = new PermissionsManager(activity) {
                @Override
                public void showRequestPermissionRationale() {
                    //nada
                }

                @Override
                public void requestDisallowedAction() {
                    onPermissionsDenied();
                }

                @Override
                public void onPermissionsGranted() {
                    onPermissionGranted();
                }

                @Override
                public void onPermissionsDenied() {
                    Toast.makeText(activity, PERMISSION_DENIED +
                            " Try again", Toast.LENGTH_LONG).show();
                }
            };
            downloadsPermMgr.checkPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    PermissionRequestCodes.DOWNLOADS);
        }
        return true;
    }
}
