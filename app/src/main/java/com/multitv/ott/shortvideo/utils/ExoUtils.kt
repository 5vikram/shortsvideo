package com.multitv.ott.shortvideo.uttls

import android.content.Context
import android.net.Uri
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.C.TRACK_TYPE_TEXT
import com.google.android.exoplayer2.Format
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Tracks
import com.google.android.exoplayer2.drm.DrmSessionManager
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.TrackGroup
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.MappingTrackSelector
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.util.Util
import java.util.*

object ExoUtils {

    fun buildM3u8MediaSource(
        context: Context,
        mediaItem: MediaItem,
        uri: String
    ): MediaSource {
        val uriData = Uri.parse(uri)
        val type: Int = Util.inferContentType(uriData.lastPathSegment.toString())
        val dataSourceFactory: DataSource.Factory = DefaultDataSource.Factory(context)
        val dataSourceFactory1 = DefaultHttpDataSource.Factory()

        return HlsMediaSource.Factory(dataSourceFactory1)
            .setAllowChunklessPreparation(true)
            .createMediaSource(mediaItem)
    }


    fun buildMediaSource(
        context: Context,
        mediaItem: MediaItem,
        uri: String,
        drmSessionManager: DrmSessionManager
    ): MediaSource {

        val uriData = Uri.parse(uri)
        val type: Int = Util.inferContentType(uriData.lastPathSegment.toString())
        val dataSourceFactory: DataSource.Factory = DefaultDataSource.Factory(context)
        val dataSourceFactory1 = DefaultHttpDataSource.Factory()
        return when (type) {
            C.TYPE_HLS -> HlsMediaSource.Factory(dataSourceFactory1)
                .setAllowChunklessPreparation(true)
                .createMediaSource(mediaItem)
            C.TYPE_DASH -> DashMediaSource.Factory(dataSourceFactory)
                .setDrmSessionManagerProvider { unusedMediaItem: MediaItem? -> drmSessionManager }
                .createMediaSource(MediaItem.fromUri(uri))
            C.TYPE_OTHER -> ProgressiveMediaSource.Factory(dataSourceFactory1)
                .createMediaSource(MediaItem.fromUri(uri))
            else -> throw IllegalStateException("Unsupported type: $type")
        }
    }


    fun buildDashMediaSource(mediaItem: MediaItem, context: Context): MediaSource {
        //val dataSourceFactory = DefaultHttpDataSource.Factory()
        val dataSourceFactory: DataSource.Factory = DefaultDataSource.Factory(context)
        return DashMediaSource.Factory(dataSourceFactory)
            .createMediaSource(mediaItem)
    }



    private var videoFormatArrayList = ArrayList<Format>()
    private var audioFormatArrayList = ArrayList<Format>()
    private var subtitleArrayList = ArrayList<Format>()








    private fun isVideoRenderer(
        mappedTrackInfo: MappingTrackSelector.MappedTrackInfo, rendererIndex: Int
    ): Boolean {
        val trackGroupArray = mappedTrackInfo.getTrackGroups(rendererIndex)
        if (trackGroupArray.length == 0) return false
        val trackType = mappedTrackInfo.getRendererType(rendererIndex)
        return C.TRACK_TYPE_VIDEO == trackType
    }

    private fun isAudioRenderer(
        mappedTrackInfo: MappingTrackSelector.MappedTrackInfo, rendererIndex: Int
    ): Boolean {
        val trackGroupArray = mappedTrackInfo.getTrackGroups(rendererIndex)
        if (trackGroupArray.length == 0) return false
        val trackType = mappedTrackInfo.getRendererType(rendererIndex)
        return C.TRACK_TYPE_AUDIO == trackType
    }


    private fun isTextRenderer(
        mappedTrackInfo: MappingTrackSelector.MappedTrackInfo, rendererIndex: Int
    ): Boolean {
        val trackGroupArray = mappedTrackInfo.getTrackGroups(rendererIndex)
        if (trackGroupArray.length == 0) return false
        val trackType = mappedTrackInfo.getRendererType(rendererIndex)
        return C.TRACK_TYPE_TEXT == trackType
    }
}