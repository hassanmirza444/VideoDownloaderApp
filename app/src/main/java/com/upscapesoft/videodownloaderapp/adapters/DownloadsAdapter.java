package com.upscapesoft.videodownloaderapp.adapters;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.upscapesoft.videodownloaderapp.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DownloadsAdapter extends RecyclerView.Adapter<DownloadsAdapter.SavedHolder> {

    private List<File> list;
    private Context context;

    public DownloadsAdapter(List<File> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public SavedHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.downloads_item_lay, parent, false);
        return new SavedHolder(view);
    }

    @Override
    @SuppressLint("RecyclerView")
    public void onBindViewHolder(@NonNull SavedHolder holder, final int position) {
        holder.fileName.setText(list.get(position).getName());

        Glide.with(context)
                .load(list.get(position).getAbsoluteFile())
                .into(holder.fileThumbnail);

        holder.fileThumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri fileUri = FileProvider.getUriForFile(context, "com.upscapesoft.videodownloaderapp.fileprovider", list.get(position));
                intent.setDataAndType(fileUri, "video/*");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                context.startActivity(intent);
            }
        });

        holder.shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareFile(list.get(position));
            }
        });

        holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteFile(list.get(position));
            }
        });
    }

    private void shareFile(final File f) {
        File file = new File(Environment.getExternalStoragePublicDirectory(context.getResources().getString(R.string.app_name)), f.getName());
        Uri fileUri = FileProvider.getUriForFile(context, "com.upscapesoft.videodownloaderapp.fileprovider", file);

        StringBuilder msg = new StringBuilder();
        msg.append(context.getResources().getString(R.string.share_message));
        msg.append("\n");
        msg.append(context.getResources().getString(R.string.link));

        if (fileUri != null) {
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // temp permission for receiving app to read this file
            shareIntent.setType("*/*");
            shareIntent.putExtra(Intent.EXTRA_TEXT, msg.toString());
            shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
            try {
                context.startActivity(Intent.createChooser(shareIntent, "Share via"));
            } catch (ActivityNotFoundException e) {
                Toast.makeText(context.getApplicationContext(), "No App Available", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void deleteFile(final File f) {
        new AlertDialog.Builder(context)
                .setMessage("Are you sure you want to delete?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (f.delete()) {
                            updateList();
                        }
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //nada
                        dialog.dismiss();
                    }
                })
                .create()
                .show();
    }

    public void updateList() {
        File videoFile = new File(Environment.getExternalStoragePublicDirectory(context.getResources().getString(R.string.app_name)).getAbsolutePath());
        if (videoFile.exists()) {
            List<File> nonExistentFiles = new ArrayList<>();
            nonExistentFiles.addAll(Arrays.asList(videoFile.listFiles()));
            this.list = nonExistentFiles;
            notifyDataSetChanged();
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class SavedHolder extends RecyclerView.ViewHolder {

        private TextView fileName;
        private ImageView fileThumbnail;
        private ImageView shareBtn;
        private ImageView deleteBtn;

        public SavedHolder(@NonNull View itemView) {
            super(itemView);
            fileName = itemView.findViewById(R.id.fileName);
            fileThumbnail = itemView.findViewById(R.id.fileThumbnail);
            shareBtn = itemView.findViewById(R.id.shareBtn);
            deleteBtn = itemView.findViewById(R.id.deleteBtn);
        }
    }

}
