package com.multitv.ott.shortvideo

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.view.View
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.view.animation.TranslateAnimation
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.OrientationHelper
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelector
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.exoplayer2.upstream.*
import com.google.android.exoplayer2.video.VideoSize
import com.jaeger.library.StatusBarUtil
import com.multitv.ott.shortvideo.databinding.ShortVideoLayoutBinding
import com.multitv.ott.shortvideo.listener.OnLoadMoreListener
import com.multitv.ott.shortvideo.listener.OnViewPagerListener
import com.multitv.ott.shortvideo.model.AuthModel
import com.multitv.ott.shortvideo.network.CommonApiListener
import com.multitv.ott.shortvideo.network.CommonApiPresenterImpl
import com.multitv.ott.shortvideo.network.Json
import com.multitv.ott.shortvideo.utils.ScreenUtils
import com.multitv.ott.shortvideo.utils.Uttils
import com.multitv.ott.shortvideo.utils.ViewPagerLayoutManager
import com.multitv.ott.shortvideo.uttls.CommonUtils
import com.multitv.ott.shortvideo.uttls.PlayerConstant.ALLOCATION_SIZE
import com.multitv.ott.shortvideo.uttls.PlayerConstant.BACKWARD_INCREMENT
import com.multitv.ott.shortvideo.uttls.PlayerConstant.BACK_BUFFER_DURATION
import com.multitv.ott.shortvideo.uttls.PlayerConstant.BUFFER_FOR_PLAYBACK
import com.multitv.ott.shortvideo.uttls.PlayerConstant.BUFFER_FOR_PLAYBACK_AFTER_RE_BUFFER
import com.multitv.ott.shortvideo.uttls.PlayerConstant.FORWARD_INCREMENT
import com.multitv.ott.shortvideo.uttls.PlayerConstant.MAX_BUFFER_DURATION
import com.multitv.ott.shortvideo.uttls.PlayerConstant.MIN_BUFFER_DURATION

class ShortsVideoActivity : AppCompatActivity(), OnLoadMoreListener {

    private val contentHomeList = ArrayList<ContentItem>()
    private var layoutManager: ViewPagerLayoutManager? = null
    private var shortsVideoAdapter: ShortsVideoAdapter? = null

    private var endPointContentListUrl =
        "/device/android/current_offset/0/max_counter/100/cat_id/3437"

    private var authUrl =
        "https://expo.multitvsolution.com/api/v6/get/validate/token/package_id/12/token/"

    //15zh353kd4dese
    private var mCurPos = 0

    private lateinit var styledPlayerView: StyledPlayerView
    private lateinit var videoImageView: ImageView
    private lateinit var videoPauseButton: ImageView
    private lateinit var videoPlayButton: ImageView
    private var music01: ImageView? = null
    private var music02: ImageView? = null
    private var ivSoundTrack_HomeFragLay: ImageView? = null

    private var mediaPlayer: ExoPlayer? = null
    private var trackSelector: DefaultTrackSelector? = null
    private var audioManager: AudioManager? = null
    private var videoAdsUrl = ""

    private var seekBackIncrementMs = BACKWARD_INCREMENT
    private var seekForwardIncrementMs = FORWARD_INCREMENT

    private var vaildationTokenRequest: String? = null
    private lateinit var authModel: AuthModel


    private lateinit var binding: ShortVideoLayoutBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StatusBarUtil.setTransparent(this)
        binding = DataBindingUtil.setContentView(this, R.layout.short_video_layout)
        vaildationTokenRequest = intent?.getStringExtra(Uttils.TOKEN)

        if (!vaildationTokenRequest.isNullOrEmpty())
            authenticationToken()
        else
            finish()
    }

    private fun authenticationToken() {
        val header = HashMap<String, String>()
        binding.loadMoreProgressbar.visibility = View.VISIBLE

        //var contentListUrl=authModel.masterUrls.

        CommonApiPresenterImpl(object : CommonApiListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onSuccess(response: String?) {
                binding.loadMoreProgressbar.visibility = View.GONE
                authModel = Json.parse(response, AuthModel::class.java) as AuthModel

                if (authModel.code == 1) {
                    layoutManager =
                        ViewPagerLayoutManager(this@ShortsVideoActivity, OrientationHelper.VERTICAL)
                    layoutManager?.initialPrefetchItemCount = 3
                    binding.tictocRecyclerview.setItemViewCacheSize(20)
                    layoutManager?.setExtraLayoutSpace(ScreenUtils.getScreenHeight(this@ShortsVideoActivity))
                    binding.tictocRecyclerview.layoutManager = layoutManager
                    binding.tictocRecyclerview.isNestedScrollingEnabled = true

                    shortsVideoAdapter =
                        ShortsVideoAdapter(
                            this@ShortsVideoActivity,
                            contentHomeList,
                            binding.tictocRecyclerview,
                            this@ShortsVideoActivity
                        )
                    binding.tictocRecyclerview.adapter = shortsVideoAdapter

                    getVideoDetailsData(false)

                    layoutManager?.setOnViewPagerListener(object : OnViewPagerListener {
                        override fun onInitComplete() {
                            setVideoPlayer(mCurPos)
                        }

                        override fun onPageRelease(isNext: Boolean, position: Int) {

                        }

                        override fun onPageSelected(position: Int, isBottom: Boolean) {

                            if (mCurPos == position) return

                            setVideoPlayer(position)

                        }

                        override fun loadImageNextPerviousItem(isNext: Boolean, position: Int) {

                        }

                    })

                } else {
                    Toast.makeText(
                        this@ShortsVideoActivity,
                        Uttils.INVAILD_USER,
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            }

            override fun onError(message: String?) {
                binding.loadMoreProgressbar.visibility = View.GONE
                Toast.makeText(
                    this@ShortsVideoActivity,
                    Uttils.INVAILD_USER,
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }

        }).getRequest(authUrl + vaildationTokenRequest, "Auth Url", header)

    }


    private fun getVideoDetailsData(isLoadMoreVideo: Boolean) {
        if (isLoadMoreVideo)
            binding.loadMoreProgressbar.visibility = View.VISIBLE
        else
            binding.centerProgressbar.visibility = View.VISIBLE

        val header = HashMap<String, String>()
        val params = HashMap<String, String>()

        var contentListUrl = authModel.masterUrls?.contentList + endPointContentListUrl

        CommonApiPresenterImpl(object : CommonApiListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onSuccess(response: String?) {
                val home = Json.parse(response, ShortVideo::class.java) as ShortVideo
                binding.loadMoreProgressbar.visibility = View.GONE
                binding.centerProgressbar.visibility = View.GONE
                contentHomeList.addAll(home.result?.content!!)
                if (contentHomeList.size != 0) {
                    binding.tictocRecyclerview.visibility = View.VISIBLE
                    binding.contentInfoNotFoundTV.visibility = View.GONE
                    shortsVideoAdapter?.notifyDataSetChanged()
                    binding.tictocRecyclerview.scrollToPosition(0)
                } else {
                    binding.tictocRecyclerview.visibility = View.GONE
                    binding.contentInfoNotFoundTV.visibility = View.VISIBLE
                }
            }

            override fun onError(message: String?) {
                binding.tictocRecyclerview.visibility = View.GONE
                binding.contentInfoNotFoundTV.visibility = View.VISIBLE
                binding.loadMoreProgressbar.visibility = View.GONE
            }

        }).getRequest(contentListUrl, "Content List Url", header)

    }

    private fun setVideoPlayer(position: Int) {
        releaseVideoPlayer()
        binding.loadMoreProgressbar.visibility = View.VISIBLE

        val findViewByPosition = layoutManager?.findViewByPosition(position)

        styledPlayerView =
            findViewByPosition?.findViewById(R.id.playerView) as StyledPlayerView
        videoImageView =
            findViewByPosition.findViewById(R.id.videoImageView) as ImageView

        videoPauseButton =
            findViewByPosition.findViewById(R.id.exo_pause) as ImageView

        videoPlayButton =
            findViewByPosition.findViewById(R.id.exo_play) as ImageView

        videoPlayButton.setOnClickListener {
            videoPlayButton.visibility = View.GONE
            videoPauseButton.visibility = View.VISIBLE
            mediaPlayer?.playWhenReady = true
        }

        videoPauseButton.setOnClickListener {
            videoPlayButton.visibility = View.VISIBLE
            videoPauseButton.visibility = View.GONE
            mediaPlayer?.playWhenReady = false
        }


        videoImageView.visibility = View.VISIBLE
        val imageUrl = contentHomeList[position].thumbs?.get(0)?.thumb?.large

        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(this@ShortsVideoActivity)
                .load(imageUrl)
                .error(R.color.black)
                .into(videoImageView)
        }


        music01 = findViewByPosition.findViewById(R.id.music_01) as ImageView
        music02 = findViewByPosition.findViewById(R.id.music_02) as ImageView
        ivSoundTrack_HomeFragLay =
            findViewByPosition.findViewById(R.id.ivSoundTrack_HomeFragLay) as ImageView

        callFirstAnimation()
        val rotate = RotateAnimation(
            0f,
            360f,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f
        )
        rotate.duration = 5000
        rotate.repeatCount = Animation.INFINITE
        rotate.interpolator = LinearInterpolator()
        ivSoundTrack_HomeFragLay!!.startAnimation(rotate)

        trackSelector = DefaultTrackSelector(this)

        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        val mgr = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        if (Build.VERSION.SDK_INT >= 31) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.READ_PHONE_STATE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE)
            }
        } else {
            mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE)
        }

        releaseVideoPlayer()
        val customLoadControl = getCustomLoadControl()
        mediaPlayer = getMediaPLayerInstance(customLoadControl, trackSelector!!, videoAdsUrl)
        mediaPlayer?.addListener(playerStateListener)
        styledPlayerView.player = mediaPlayer
        styledPlayerView.controllerHideOnTouch = true
        styledPlayerView.keepScreenOn = true
        styledPlayerView.useController = false
        styledPlayerView.setControllerHideDuringAds(true)
        //val isDrm = isDrmContent(videoUrl)
        val mediaItem = getMediaItem(
            contentHomeList.get(position).url.toString()
        )
        mediaPlayer?.setMediaItem(mediaItem)
        mediaPlayer?.repeatMode = Player.REPEAT_MODE_ONE
        mediaPlayer?.prepare()
        mediaPlayer?.playWhenReady = true
        updatePlayPauseButton()

        mCurPos = position
    }

    private fun updatePlayPauseButton() {
        var requestPlayPauseFocus = false
        val playing = mediaPlayer != null && mediaPlayer!!.playWhenReady
        requestPlayPauseFocus =
            requestPlayPauseFocus or (playing && videoPlayButton.isFocused)
        videoPlayButton.visibility = if (playing) FrameLayout.GONE else FrameLayout.VISIBLE
        requestPlayPauseFocus =
            requestPlayPauseFocus or (!playing && videoPauseButton.isFocused)
        videoPauseButton.visibility = if (!playing) FrameLayout.GONE else FrameLayout.VISIBLE
        if (requestPlayPauseFocus) {
            requestPlayPauseFocus()
        }
    }


    private fun requestPlayPauseFocus() {
        val playing = mediaPlayer != null && mediaPlayer!!.playWhenReady
        if (!playing)
            videoPlayButton.requestFocus()
        else
            videoPauseButton.requestFocus()

    }


    private var playerStateListener: Player.Listener = object : Player.Listener {
        override fun onPlaybackStateChanged(@Player.State playbackState: Int) {
            when (playbackState) {

                Player.STATE_READY -> {
                    styledPlayerView.visibility = View.VISIBLE
                    videoImageView.visibility = View.GONE
                    binding.loadMoreProgressbar.visibility = View.GONE
                    // styledPlayerView.bringToFront()
                    styledPlayerView.useController = true
                }

                Player.STATE_IDLE -> {
                    if (!checkForAudioFocus()) return
                    styledPlayerView.videoSurfaceView?.visibility = View.VISIBLE
                    binding.loadMoreProgressbar.visibility = View.GONE
                    videoImageView.visibility = View.GONE
                    //styledPlayerView.bringToFront()
                }

                Player.STATE_BUFFERING -> {
                    binding.loadMoreProgressbar.visibility = View.VISIBLE
                }

                Player.STATE_ENDED -> {
                    mediaPlayer?.repeatMode = Player.REPEAT_MODE_ONE
                }
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            super.onPlayerError(error)
            styledPlayerView.videoSurfaceView?.visibility = View.GONE
            binding.loadMoreProgressbar.visibility = View.GONE
            videoImageView.visibility = View.GONE
            styledPlayerView.visibility = View.GONE
            binding.loadMoreProgressbar.visibility = View.GONE
        }

        override fun onVideoSizeChanged(videoSize: VideoSize) {
            super.onVideoSizeChanged(videoSize)

            if (videoSize.width > videoSize.height)
                styledPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT)
            else
                styledPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL)


        }

    }

    private fun checkForAudioFocus(): Boolean {
        // Request audio focus for playback
        val result = audioManager?.requestAudioFocus(
            audioFocusChangeListener,  // Use the music stream.
            AudioManager.STREAM_MUSIC,  // Request permanent focus.
            AudioManager.AUDIOFOCUS_GAIN
        )
        return if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            true
        } else {
            pauseVideoPlayer()
            false
        }
    }


    val audioFocusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
            if (mediaPlayer != null && mediaPlayer!!.playWhenReady) {
                checkForAudioFocus()
            }
        }
    }


    var phoneStateListener: PhoneStateListener = object : PhoneStateListener() {
        @Deprecated("Deprecated in Java")
        override fun onCallStateChanged(state: Int, incomingNumber: String) {
            if (state == TelephonyManager.CALL_STATE_RINGING) {
                pauseVideoPlayer()
            } else if (state == TelephonyManager.CALL_STATE_IDLE) {
                if (!CommonUtils().isAppIsInBackground(this@ShortsVideoActivity)) resumeVideoPlayer()
            } else if (state == TelephonyManager.CALL_STATE_OFFHOOK) {
                pauseVideoPlayer()
            }
            super.onCallStateChanged(state, incomingNumber)
        }
    }

    private fun getMediaItem(
        videoUrl: String
    ): MediaItem {

        val mediaItemBuilder = MediaItem.Builder()
            .setUri(videoUrl)
            .setMediaMetadata(MediaMetadata.Builder().setTitle("MultiTv").build())

//        if (drm && !drmLicenseUrl.isNullOrEmpty()) {
//            val drmConfig = MediaItem.DrmConfiguration.Builder(C.WIDEVINE_UUID)
//                .setLicenseUri(drmLicenseUrl)
//                .build()
//            mediaItemBuilder.setDrmConfiguration(drmConfig)
//        }


        /* if (subtitle != null) {
             mediaItemBuilder.setSubtitleConfigurations(subtitle)
         }*/

        return mediaItemBuilder.build()
    }

    private fun getCustomLoadControl(): LoadControl {

        return DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                MIN_BUFFER_DURATION, MAX_BUFFER_DURATION,
                BUFFER_FOR_PLAYBACK, BUFFER_FOR_PLAYBACK_AFTER_RE_BUFFER
            )
            .setAllocator(DefaultAllocator(true, ALLOCATION_SIZE))
            .setBackBuffer(BACK_BUFFER_DURATION, false)
            .setPrioritizeTimeOverSizeThresholds(true)
            .setTargetBufferBytes(C.LENGTH_UNSET)
            .build()

    }

    private fun getMediaPLayerInstance(
        customLoadControl: LoadControl,
        trackSelector: TrackSelector, adsUrl: String?
    ): ExoPlayer {

        return ExoPlayer.Builder(this)
            .setLoadControl(customLoadControl)
            .setTrackSelector(trackSelector)
            .setSeekForwardIncrementMs(seekForwardIncrementMs)
            .setSeekBackIncrementMs(seekBackIncrementMs)
            .build()

    }

    fun pauseVideoPlayer() {
        if (this::styledPlayerView.isInitialized) {
            styledPlayerView.onPause()
            mediaPlayer?.playWhenReady = false
        }
    }

    fun resumeVideoPlayer() {
        if (this::styledPlayerView.isInitialized) {
            styledPlayerView.onResume()
            mediaPlayer?.playWhenReady = true
        }
    }


    fun releaseVideoPlayer() {
        if (this::styledPlayerView.isInitialized) {
            styledPlayerView.player?.release()
            mediaPlayer?.release()
        }

    }

    private fun callFirstAnimation() {
        val animation1: Animation = TranslateAnimation(0f, (-100).toFloat(), 0f, (-50).toFloat())
        animation1.interpolator = LinearInterpolator()
        animation1.duration = 2000
        animation1.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}
            override fun onAnimationEnd(animation: Animation) {
                callSecondAnimation()
            }

            override fun onAnimationRepeat(animation: Animation) {}
        })
        music01?.startAnimation(animation1)
    }

    private fun callSecondAnimation() {
        val animation2: Animation = TranslateAnimation(0f, (-100).toFloat(), 0f, (-50).toFloat())
        animation2.interpolator = LinearInterpolator()
        animation2.duration = 2000
        animation2.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}
            override fun onAnimationEnd(animation: Animation) {
                callFirstAnimation()
            }

            override fun onAnimationRepeat(animation: Animation) {}
        })
        music02?.startAnimation(animation2)
    }

    override fun onPause() {
        super.onPause()
        pauseVideoPlayer()
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseVideoPlayer()
    }

    override fun onResume() {
        super.onResume()
        resumeVideoPlayer()
    }

    override fun onLoadMore() {

    }


}