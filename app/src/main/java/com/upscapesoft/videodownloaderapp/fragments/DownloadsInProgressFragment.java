package com.upscapesoft.videodownloaderapp.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.format.Formatter;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.upscapesoft.videodownloaderapp.R;
import com.upscapesoft.videodownloaderapp.dialogs.RenameFileDialog;
import com.upscapesoft.videodownloaderapp.fragments.base.BaseFragment;
import com.upscapesoft.videodownloaderapp.helper.DownloadManager;
import com.upscapesoft.videodownloaderapp.helper.DownloadPermissionHandler;
import com.upscapesoft.videodownloaderapp.helper.DownloadQueues;
import com.upscapesoft.videodownloaderapp.helper.DownloadVideo;
import com.upscapesoft.videodownloaderapp.helper.PermissionRequestCodes;
import com.upscapesoft.videodownloaderapp.interfaces.OnDownloadWithNewLinkListener;
import com.upscapesoft.videodownloaderapp.interfaces.Tracking;
import com.upscapesoft.videodownloaderapp.utils.Utils;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class DownloadsInProgressFragment extends BaseFragment implements DownloadManager.OnDownloadFinishedListener, DownloadManager.OnLinkNotFoundListener, OnDownloadWithNewLinkListener {

    private View view;
    private List<DownloadVideo> downloads;
    private RecyclerView downloadsList;
    private DownloadQueues queues;
    private FloatingActionButton downloadsStartPauseButton;

    private Tracking tracking;
    private RecyclerView.OnItemTouchListener downloadsListItemTouchDisabler;

    private OnAddDownloadedVideoToCompletedListener onAddDownloadedVideoToCompletedListener;
    private OnAddDownloadItemToInactiveListener onAddDownloadItemToInactiveListener;
    private OnNumDownloadsInProgressChangeListener onNumDownloadsInProgressChangeListener;

    private RenameFileDialog activeRenameDialog;

    private Activity activity;

    public DownloadsInProgressFragment(Activity activity) {
        this.activity = activity;
    }

    public interface OnAddDownloadedVideoToCompletedListener {
        void onAddDownloadedVideoToCompleted(String name, String type);
    }

    public interface OnAddDownloadItemToInactiveListener {
        void onAddDownloadItemToInactive(DownloadVideo inactiveDownload);
    }

    public interface OnNumDownloadsInProgressChangeListener {
        void onNumDownloadsInProgressChange();
    }

    public int getNumDownloadsInProgress() {
        return downloads.size();
    }

    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setRetainInstance(true);

        downloads = new ArrayList<>();
        queues = DownloadQueues.load(activity);
        downloads = queues.getList();

        if (view == null) {
            view = inflater.inflate(R.layout.download_in_progress_lay, container, false);

            downloadsList = view.findViewById(R.id.downloadsList);
            LinearLayout empty = view.findViewById(R.id.empty_progress);

            if (!downloads.isEmpty()) {
                empty.setVisibility(View.INVISIBLE);
                downloadsList.setLayoutManager(new LinearLayoutManager(activity));
                downloadsList.setAdapter(new DownloadListAdapter());
                downloadsList.setHasFixedSize(true);
            } else {
                empty.setVisibility(View.VISIBLE);
            }


            downloadsStartPauseButton = view.findViewById(R.id.downloadsStartPauseButton);

            downloadsStartPauseButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Utils.Companion.isServiceRunning(DownloadManager.class, activity.getApplicationContext())) {
                        pauseDownload();
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            new DownloadPermissionHandler(activity) {
                                @Override
                                public void onPermissionGranted() {
                                    startDownload();
                                }
                            }.checkPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                    PermissionRequestCodes.DOWNLOADS);
                        } else startDownload();
                    }
                }
            });

            DownloadManager.setOnDownloadFinishedListener(this);
            DownloadManager.setOnLinkNotFoundListener(this);
        }

        return view;
    }

    @Override
    public void onDestroyView() {
        DownloadManager.setOnDownloadFinishedListener(null);
        DownloadManager.setOnLinkNotFoundListener(null);
        super.onDestroyView();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        if (Utils.Companion.isServiceRunning(DownloadManager.class, activity.getApplicationContext())) {
            downloadsStartPauseButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause));
            if (getAdapter() != null) {
                getAdapter().unpause();
            }
            if (tracking != null) {
                tracking.startTracking();
            }
        } else {
            downloadsStartPauseButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_play));
            if (getAdapter() != null) {
                getAdapter().pause();
            }
            if (tracking != null) {
                tracking.stopTracking();
            }
        }
        
        downloadsListItemTouchDisabler = new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                return true;
            }

            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent e) {
                //
            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
                //
            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getAdapter() != null) {
            getAdapter().notifyDataSetChanged();
        }
        if (onNumDownloadsInProgressChangeListener != null) {
            onNumDownloadsInProgressChangeListener.onNumDownloadsInProgressChange();
        }
    }


    @Override
    public void onDownloadWithNewLink(final DownloadVideo download) {
        Log.i("VDDebug", "download with new link");
        if (Utils.Companion.isServiceRunning(DownloadManager.class, getActivity().getApplicationContext())) {
            pauseDownload();
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                downloads.add(0, download);
                saveQueues();
                getAdapter().notifyItemInserted(0);
                onNumDownloadsInProgressChangeListener.onNumDownloadsInProgressChange();
                startDownload();
            }
        });
    }

    public void setTracking(Tracking tracking) {
        this.tracking = tracking;
    }

    public void startDownload() {
        Intent downloadService = getMyApp().getDownloadService();
        if (!downloads.isEmpty()) {
            DownloadVideo topVideo = downloads.get(0);
            downloadService.putExtra("link", topVideo.link);
            downloadService.putExtra("name", topVideo.name);
            downloadService.putExtra("type", topVideo.type);
            downloadService.putExtra("size", topVideo.size);
            downloadService.putExtra("page", topVideo.page);
            downloadService.putExtra("chunked", topVideo.chunked);
            downloadService.putExtra("website", topVideo.website);
            getMyApp().startService(downloadService);
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    downloadsStartPauseButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause));
                    if (getAdapter() != null) {
                        getAdapter().unpause();
                    }
                }
            });
            if (tracking != null) {
                tracking.startTracking();
            }
        }
    }

    public void pauseDownload() {
        DownloadManager.stop();
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                downloadsStartPauseButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_play));
                if (tracking != null) {
                    tracking.stopTracking();
                }
                if (getAdapter() != null) {
                    getAdapter().pause();
                }
            }
        });
    }

    @Override
    public void onDownloadFinished() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (activeRenameDialog != null && activeRenameDialog.isActive()) {
                    activeRenameDialog.dismiss();
                }

                downloadsStartPauseButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_play));
                if (tracking != null) {
                    tracking.stopTracking();
                }
                if (!downloads.isEmpty()) {
                    String name = downloads.get(0).name;
                    String type = downloads.get(0).type;
                    downloads.remove(0);
                    saveQueues();
                    onAddDownloadedVideoToCompletedListener.onAddDownloadedVideoToCompleted(name, type);
                    if (getAdapter() != null) {
                        getAdapter().notifyItemRemoved(0);
                    }
                    onNumDownloadsInProgressChangeListener.onNumDownloadsInProgressChange();
                }
                startDownload();
            }
        });
    }

    @Override
    public void onLinkNotFound() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                downloadsStartPauseButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_play));
                if (tracking != null) {
                    tracking.stopTracking();
                }
                if (!downloads.isEmpty()) {
                    DownloadVideo video = downloads.get(0);
                    DownloadVideo inactiveDownload = new DownloadVideo();
                    inactiveDownload.name = video.name;
                    inactiveDownload.link = video.link;
                    inactiveDownload.page = video.page;
                    inactiveDownload.size = video.size;
                    inactiveDownload.type = video.type;
                    downloads.remove(0);
                    saveQueues();
                    onAddDownloadItemToInactiveListener.onAddDownloadItemToInactive(inactiveDownload);
                    if (getAdapter() != null) {
                        getAdapter().notifyItemRemoved(0);
                    }
                    onNumDownloadsInProgressChangeListener.onNumDownloadsInProgressChange();
                }
                startDownload();
            }
        });
    }

    public void updateDownloadItem() {
        if (getAdapter() != null) {
            getAdapter().notifyItemChanged(0);
        }
    }

    public void setOnAddDownloadedVideoToCompletedListener
            (OnAddDownloadedVideoToCompletedListener onAddDownloadedVideoToCompletedListener) {
        this.onAddDownloadedVideoToCompletedListener = onAddDownloadedVideoToCompletedListener;
    }

    public void setOnAddDownloadItemToInactiveListener(OnAddDownloadItemToInactiveListener
                                                               onAddDownloadItemToInactiveListener) {
        this.onAddDownloadItemToInactiveListener = onAddDownloadItemToInactiveListener;
    }

    public void setOnNumDownloadsInProgressChangeListener(OnNumDownloadsInProgressChangeListener
                                                                  onNumDownloadsInProgressChangeListener) {
        this.onNumDownloadsInProgressChangeListener = onNumDownloadsInProgressChangeListener;
    }


    public DownloadListAdapter getAdapter() {
        return (DownloadListAdapter) downloadsList.getAdapter();
    }

    public List<DownloadVideo> getDownloads() {
        return downloads;
    }

    public float getDownloadListHeight() {
        return downloadsList.getHeight();
    }

    public void disableDownloadListTouch() {
        downloadsList.addOnItemTouchListener(downloadsListItemTouchDisabler);
    }

    public void enableDownloadListTouch() {
        downloadsList.removeOnItemTouchListener(downloadsListItemTouchDisabler);
    }

    public void saveQueues() {
        queues.save(activity);
    }

    public class DownloadListAdapter extends RecyclerView.Adapter<DownloadItem> {
        private int selectedItemPosition = -1;
        private boolean paused;

        
        @Override
        public DownloadItem onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(activity);

            return new DownloadItem(inflater.inflate(R.layout.download_in_progress_item_lay, parent, false));
        }

        @Override
        public void onBindViewHolder(DownloadItem holder, int position) {
            holder.bind(downloads.get(position));
        }

        @Override
        public int getItemCount() {
            return downloads.size();
        }

        public int getSelectedItemPosition() {
            return selectedItemPosition;
        }

        public void setSelectedItemPosition(int position) {
            selectedItemPosition = position;
        }

        public void pause() {
            paused = true;
        }

        public void unpause() {
            paused = false;
        }

        public boolean isPaused() {
            return paused;
        }
    }

    public class DownloadItem extends RecyclerView.ViewHolder implements ViewTreeObserver
            .OnGlobalLayoutListener {
        private TextView name;
        private ProgressBar progress;
        private TextView status;
        private ImageView menu;

        private boolean adjustedlayout;
        private int nameMaxWidth;

        DownloadItem(final View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.downloadVideoName);
            progress = itemView.findViewById(R.id.downloadProgressBar);
            status = itemView.findViewById(R.id.downloadProgressText);
            itemView.getViewTreeObserver().addOnGlobalLayoutListener(this);
            adjustedlayout = false;
            itemView.findViewById(R.id.download_progress_menu).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final PopupMenu popup = new PopupMenu(activity.getApplicationContext(), view);
                    popup.getMenuInflater().inflate(R.menu.download_progress_menu, popup.getMenu());
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem item) {
                            int i = item.getItemId();
                            if (i == R.id.downloadProgressDelete) {
                                new AlertDialog.Builder(activity)
                                        .setMessage("Do you want to delete this item?")
                                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                int position = getAdapterPosition();
                                                if (position != 0) {
                                                    downloads.remove(position);
                                                    saveQueues();
                                                    getAdapter().notifyItemRemoved(position);
                                                } else {
                                                    downloads.remove(position);
                                                    saveQueues();
                                                    getAdapter().notifyItemRemoved(position);
                                                    startDownload();
                                                }
                                                onNumDownloadsInProgressChangeListener.onNumDownloadsInProgressChange();
                                            }
                                        })
                                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        })
                                        .create()
                                        .show();
                                return true;
                            }else if (i == R.id.downloadProgressRename){
                                final int itemToRenamePosition = getAdapterPosition();
                                if (itemToRenamePosition == -1)
                                    return true;

                                activeRenameDialog = new RenameFileDialog(
                                        activity,
                                        name.getText().toString()) {
                                    @Override
                                    public void onDismiss(DialogInterface dialog) {
                                        activeRenameDialog = null;
                                    }

                                    @Override
                                    public void onOK(String newName) {
                                        queues.renameItem(itemToRenamePosition, newName);
                                        File renamedFile = new File(Environment.getExternalStoragePublicDirectory(getString(R.string.app_name)), downloads.get
                                                (itemToRenamePosition).name);
                                        File file = new File(Environment.getExternalStoragePublicDirectory(getString(R.string.app_name)), name.getText().toString());
                                        if (file.exists()) {
                                            if (file.renameTo(renamedFile)) {
                                                saveQueues();
                                                getAdapter().notifyItemChanged(itemToRenamePosition);
                                            } else {
                                                downloads.get(itemToRenamePosition).name = name.getText()
                                                        .toString();
                                                Toast.makeText(activity, "Failed: Cannot rename file",
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            saveQueues();
                                            getAdapter().notifyItemChanged(itemToRenamePosition);
                                        }
                                        activeRenameDialog = null;
                                    }
                                };
                                return true;
                            }
                            else {
                                return onMenuItemClick(item);
                            }
                        }
                    });
                    popup.show();
                }
            });

        }

        void bind(DownloadVideo video) {
            name.setText(video.name);
            String extString = "." + video.type;
            String downloaded;
            File file = new File(Environment.getExternalStoragePublicDirectory(getString(R.string.app_name)), video.name + extString);
            if (file.exists()) {
                if (video.size != null) {
                    long downloadedSize = file.length();
                    downloaded = Formatter.formatFileSize(activity, downloadedSize);
                    double percent = 100d * downloadedSize / Long.parseLong(video.size);
                    if (percent > 100d) {
                        percent = 100d;
                    }
                    DecimalFormat percentFormat = new DecimalFormat("00.00");
                    String percentFormatted = percentFormat.format(percent);
                    progress.setProgress((int) percent);
                    String formattedSize = Formatter.formatFileSize(activity, Long
                            .parseLong(video.size));
                    String statusString = downloaded + " / " + formattedSize + " " + percentFormatted +
                            "%";
                    status.setText(statusString);
                } else {
                    long downloadedSize = file.length();
                    downloaded = Formatter.formatShortFileSize(activity, downloadedSize);
                    status.setText(downloaded);
                    if (!getAdapter().isPaused()) {
                        if (!progress.isIndeterminate()) {
                            progress.setIndeterminate(true);
                        }
                    } else {
                        progress.setIndeterminate(false);
                    }
                }
            } else {
                if (video.size != null) {
                    String formattedSize = Formatter.formatShortFileSize(activity, Long
                            .parseLong(video.size));
                    String statusString = "0KB / " + formattedSize + " 0%";
                    status.setText(statusString);
                    progress.setProgress(0);
                } else {
                    String statusString = "0kB";
                    status.setText(statusString);
                    progress.setProgress(0);
                }
            }

            if (getAdapter().getSelectedItemPosition() == getAdapterPosition()) {
                itemView.setVisibility(View.INVISIBLE);
            } else {
                itemView.setVisibility(View.VISIBLE);
            }
        }

        public String getStatus() {
            return status.getText().toString();
        }

        public int getProgress() {
            return progress.getProgress();
        }

        public int getNameMaxWidth() {
            return nameMaxWidth;
        }

        @Override
        public void onGlobalLayout() {
            if (!adjustedlayout && itemView.getWidth() != 0) {
                int totalMargin = (int) TypedValue.applyDimension(TypedValue
                                .COMPLEX_UNIT_DIP, 35,
                        activity.getResources().getDisplayMetrics());
                nameMaxWidth = itemView.getMeasuredWidth() - totalMargin ;
                name.setMaxWidth(nameMaxWidth);
                adjustedlayout = true;
            }
        }
    }

}
