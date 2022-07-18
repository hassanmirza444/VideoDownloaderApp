package com.upscapesoft.videodownloaderapp.interfaces;

import com.upscapesoft.videodownloaderapp.helper.DownloadVideo;

//interface created outside DownloadsInactive in a different file to avoid cyclic inheritance
public interface OnDownloadWithNewLinkListener {
    void onDownloadWithNewLink(DownloadVideo download);
}
