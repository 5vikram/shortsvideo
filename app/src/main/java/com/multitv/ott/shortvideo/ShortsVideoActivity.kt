package com.multitv.ott.shortvideo

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.view.animation.TranslateAnimation
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.OrientationHelper
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelector
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.DefaultTimeBar
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.exoplayer2.ui.TimeBar
import com.google.android.exoplayer2.ui.TimeBar.OnScrubListener
import com.google.android.exoplayer2.upstream.*
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.google.android.exoplayer2.video.VideoSize
import com.multitv.ott.shortvideo.adapter.ShortsVideoAdapter
import com.multitv.ott.shortvideo.appcontroller.ApplicationController
import com.multitv.ott.shortvideo.databinding.ShortVideoLayoutBinding
import com.multitv.ott.shortvideo.listener.OnLoadMoreListener
import com.multitv.ott.shortvideo.listener.OnViewPagerListener
import com.multitv.ott.shortvideo.listener.ShareVideoListener
import com.multitv.ott.shortvideo.model.AuthModel
import com.multitv.ott.shortvideo.model.USerFollowData
import com.multitv.ott.shortvideo.model.USerLikeData
import com.multitv.ott.shortvideo.model.UserBehivourData
import com.multitv.ott.shortvideo.network.CommonApiListener
import com.multitv.ott.shortvideo.network.CommonApiPresenterImpl
import com.multitv.ott.shortvideo.network.Json
import com.multitv.ott.shortvideo.utils.CacheUttils
import com.multitv.ott.shortvideo.utils.ScreenUtils
import com.multitv.ott.shortvideo.utils.SharedPreference
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject


class ShortsVideoActivity : AppCompatActivity(), OnLoadMoreListener, ShareVideoListener {

    private val contentHomeList = ArrayList<ContentItem>()
    private var layoutManager: ViewPagerLayoutManager? = null
    private var shortsVideoAdapter: ShortsVideoAdapter? = null

    private var endPointContentListUrl =
        "https://expo.multitvsolution.com/api/v6/content/list/token/15zh353kd4dese/device/android/current_offset/0/max_counter/100/cat_id/3437"

    private var authUrl =
        "https://expo.multitvsolution.com/api/v6/get/validate/token/package_id/12/token/15zh353kd4dese"

    //15zh353kd4dese
    private var mCurPos = 0

    private lateinit var styledPlayerView: StyledPlayerView
    private lateinit var videoImageView: ImageView
    private lateinit var videoPauseButton: ImageView
    private lateinit var videoPlayButton: ImageView
    private lateinit var bookmarkImageView: ImageView

    private lateinit var muteButton: ImageView
    private lateinit var unmuteButton: ImageView
    private lateinit var exo_position: TextView
    private lateinit var likeImageView: ImageView


    private var music01: ImageView? = null
    private var music02: ImageView? = null
    private var ivSoundTrack_HomeFragLay: ImageView? = null

    private var mediaPlayer: ExoPlayer? = null
    private var trackSelector: DefaultTrackSelector? = null
    private var audioManager: AudioManager? = null
    private var videoAdsUrl = ""

    private var seekBackIncrementMs = BACKWARD_INCREMENT
    private var seekForwardIncrementMs = FORWARD_INCREMENT

    // private var vaildationTokenRequest: String? = null
    private lateinit var authModel: AuthModel


    private lateinit var binding: ShortVideoLayoutBinding

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //StatusBarUtil.setTransparent(this)
        binding = DataBindingUtil.setContentView(this, R.layout.short_video_layout)
        //  vaildationTokenRequest = intent?.getStringExtra(Uttils.TOKEN)

        //authenticationToken()

        layoutManager = ViewPagerLayoutManager(this@ShortsVideoActivity, OrientationHelper.VERTICAL)
        layoutManager?.initialPrefetchItemCount = 3
        binding.tictocRecyclerview.setItemViewCacheSize(20)
        layoutManager?.setExtraLayoutSpace(ScreenUtils.getScreenHeight(this@ShortsVideoActivity))
        binding.tictocRecyclerview.layoutManager = layoutManager
        binding.tictocRecyclerview.isNestedScrollingEnabled = true

        shortsVideoAdapter = ShortsVideoAdapter(
            this@ShortsVideoActivity,
            contentHomeList,
            binding.tictocRecyclerview,
            this@ShortsVideoActivity,
            this@ShortsVideoActivity
        )
        binding.tictocRecyclerview.adapter = shortsVideoAdapter


        val homeData = CacheUttils.getHomeCacheData()
        if (homeData != null && homeData.result != null && homeData.result.content != null && homeData.result.content.size > 0) {
            binding.loadMoreProgressbar.visibility = View.GONE
            binding.centerProgressbar.visibility = View.GONE
            contentHomeList.addAll(homeData.result.content)
            if (contentHomeList.size != 0) {
                binding.tictocRecyclerview.visibility = View.VISIBLE
                binding.contentInfoNotFoundTV.visibility = View.GONE
                shortsVideoAdapter?.notifyDataSetChanged()
                binding.tictocRecyclerview.scrollToPosition(0)
            } else {
                binding.tictocRecyclerview.visibility = View.GONE
                binding.contentInfoNotFoundTV.visibility = View.VISIBLE
                getVideoDetailsData(false)
            }
        } else {
            getVideoDetailsData(false)
        }

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

                    shortsVideoAdapter = ShortsVideoAdapter(
                        this@ShortsVideoActivity,
                        contentHomeList,
                        binding.tictocRecyclerview,
                        this@ShortsVideoActivity,
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
                        this@ShortsVideoActivity, Uttils.INVAILD_USER, Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            }

            override fun onError(message: String?) {
                binding.loadMoreProgressbar.visibility = View.GONE
                Toast.makeText(
                    this@ShortsVideoActivity, Uttils.INVAILD_USER, Toast.LENGTH_SHORT
                ).show()
                finish()
            }

        }).getRequest(authUrl, "Auth Url", header)

    }


    private fun getVideoDetailsData(isLoadMoreVideo: Boolean) {
        if (isLoadMoreVideo) binding.loadMoreProgressbar.visibility = View.VISIBLE
        else binding.centerProgressbar.visibility = View.VISIBLE

        val header = HashMap<String, String>()
        val params = HashMap<String, String>()

        var contentListUrl = endPointContentListUrl

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

    private fun userBehivourRequest() {
        val header = HashMap<String, String>()
        val params = HashMap<String, String>()
        val userId = SharedPreference().getPreferenceString(this, "user_id")

        val url =
            "https://expo.multitvsolution.com/api/v6/content/behavior/token/15zh353kd4dese/u_id/" + userId.toString() + "/c_id/" + contentHomeList.get(
                mCurPos
            ).id.toString() + "/p_id/" + contentHomeList.get(mCurPos).userId


        CommonApiPresenterImpl(object : CommonApiListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onSuccess(response: String) {
                Log.e("Vikram:::", response)
                val userBehivourData =
                    Json.parse(response, UserBehivourData::class.java) as UserBehivourData

                if (userBehivourData.code == 1) {
                    if (userBehivourData.result?.isLike == 1) {
                        likeImageView.setImageResource(R.drawable.like)
                        isFavourite = 0
                    } else {
                        likeImageView.setImageResource(R.drawable.unlike)
                        isFavourite = 1

                    }

                    if (userBehivourData.result?.isFavorite == 1) {
                        bookmarkImageView.setImageResource(R.drawable.bokkmark_selected)
                        isWatchlist = 0
                    } else {
                        bookmarkImageView.setImageResource(R.drawable.bokkmark_unselected)
                        isWatchlist = 1
                    }

                    if (userBehivourData.result!!.isFollow == 1) {
                        followButton.setText("Unfollow")
                        isFollow = 0
                    } else {
                        isFollow = 1
                        followButton.setText("Follow")
                    }
                }


            }

            override fun onError(message: String?) {
            }

        }).getRequest(
            url, "Login", header
        )
    }

    private var isFavourite = 0
    private var isFollow = 0
    private var isWatchlist = 0


    private fun bookMarkAPiRequest() {
        val header = HashMap<String, String>()
        val params = HashMap<String, String>()
        val userId = SharedPreference().getPreferenceString(this, "user_id")
        val userName = SharedPreference().getPreferenceString(this, "user_info")
        params["u_id"] = userId.toString()
        params["c_id"] = contentHomeList.get(mCurPos).id.toString()
        params["tp"] = "favorite"

        params["u_name"] = userName.toString()
        params["c_name"] = "short videos"

        params["st"] = "" + isWatchlist

        //var contentListUrl=authModel.masterUrls.

        CommonApiPresenterImpl(object : CommonApiListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onSuccess(response: String) {
                Log.e("Vikram:::", response)

                val userBehivourData =
                    Json.parse(response, USerLikeData::class.java) as USerLikeData

                if (userBehivourData.code == 1) {
                    if (userBehivourData.action.equals("1")) {
                        isWatchlist = 0
                        bookmarkImageView.setImageResource(R.drawable.bokkmark_selected)
                    } else {
                        isWatchlist = 1
                        bookmarkImageView.setImageResource(R.drawable.bokkmark_unselected)
                    }
                } else {
                    Toast.makeText(
                        this@ShortsVideoActivity,
                        "Something went wrong , please try again.",
                        Toast.LENGTH_SHORT
                    ).show()
                }


            }

            override fun onError(message: String?) {
                Toast.makeText(
                    this@ShortsVideoActivity,
                    "Something went wrong , please try again.",
                    Toast.LENGTH_SHORT
                ).show()
            }

        }).postRequest(
            "https://expo.multitvsolution.com/api/v6/user/behavior_post/token/15zh353kd4dese/device/web",
            "Favourite",
            params,
            header
        )
    }


    private fun likeApiRequest() {
        val header = HashMap<String, String>()
        val params = HashMap<String, String>()
        val userId = SharedPreference().getPreferenceString(this, "user_id")
        val userName = SharedPreference().getPreferenceString(this, "user_info")
        params["u_id"] = userId.toString()
        params["c_id"] = contentHomeList.get(mCurPos).id.toString()
        params["tp"] = "like"

        params["u_name"] = userName.toString()
        params["c_name"] = "short videos"

        params["st"] = "" + isFavourite


        //var contentListUrl=authModel.masterUrls.

        CommonApiPresenterImpl(object : CommonApiListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onSuccess(response: String) {
                Log.e("Vikram:::", response)

                val userBehivourData =
                    Json.parse(response, USerLikeData::class.java) as USerLikeData

                if (userBehivourData.code == 1) {
                    if (userBehivourData.action.equals("1")) {
                        likeImageView.setImageResource(R.drawable.like)
                        isFavourite = 0
                    } else {
                        likeImageView.setImageResource(R.drawable.unlike)
                        isFavourite = 1
                    }

                } else {
                    Toast.makeText(
                        this@ShortsVideoActivity,
                        "Something went wrong , please try again.",
                        Toast.LENGTH_SHORT
                    ).show()
                }


            }

            override fun onError(message: String?) {
                Toast.makeText(
                    this@ShortsVideoActivity,
                    "Something went wrong , please try again.",
                    Toast.LENGTH_SHORT
                ).show()
            }

        }).postRequest(
            "https://expo.multitvsolution.com/api/v6/user/behavior_post/token/15zh353kd4dese/device/web",
            "Like",
            params,
            header
        )
    }

    private fun folowApiRequest() {
        val header = HashMap<String, String>()
        val params = HashMap<String, String>()
        val userId = SharedPreference().getPreferenceString(this, "user_id")
        val userName = SharedPreference().getPreferenceString(this, "user_info")
        params["u_id"] = userId.toString()
        params["f_id"] = contentHomeList.get(mCurPos).userId.toString()
        params["st"] = "" + isFollow

        //var contentListUrl=authModel.masterUrls.

        CommonApiPresenterImpl(object : CommonApiListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onSuccess(response: String) {
                Log.e("Vikram:::", response)


                val jsonObj = JSONObject(response)

                if (jsonObj.optString("action").equals("0")) {
                    followButton.setText("Follow")
                    isFollow = 1
                } else {
                    followButton.setText("UnFollow")
                    isFollow = 0
                }


            }

            override fun onError(message: String?) {
                Toast.makeText(
                    this@ShortsVideoActivity,
                    "Something went wrong , please try again.",
                    Toast.LENGTH_SHORT
                ).show()
            }

        }).postRequest(
            "https://expo.multitvsolution.com/api/v6/user/follow/token/15zh353kd4dese/device/web",
            "Follow",
            params,
            header
        )
    }

    private lateinit var followButton: TextView
    private lateinit var httpDataSourceFactory: HttpDataSource.Factory
    private lateinit var defaultDataSourceFactory: DefaultDataSourceFactory
    private lateinit var cacheDataSourceFactory: DataSource.Factory
    private val simpleCache: SimpleCache = ApplicationController.simpleCache

    private fun setVideoPlayer(position: Int) {
        releaseVideoPlayer()
        binding.loadMoreProgressbar.visibility = View.VISIBLE

        val findViewByPosition = layoutManager?.findViewByPosition(position)

        styledPlayerView = findViewByPosition?.findViewById(R.id.playerView) as StyledPlayerView
        videoImageView = findViewByPosition.findViewById(R.id.videoImageView) as ImageView

        videoPauseButton = findViewByPosition.findViewById(R.id.playButton) as ImageView

        videoPlayButton = findViewByPosition.findViewById(R.id.pauseButton) as ImageView

        val timeBar = findViewByPosition.findViewById(R.id.exo_progress) as DefaultTimeBar

        muteButton = findViewByPosition.findViewById(R.id.muteButton) as ImageView

        unmuteButton = findViewByPosition.findViewById(R.id.unmuteButton) as ImageView

        exo_position = findViewByPosition.findViewById(R.id.exo_position) as TextView

        followButton = findViewByPosition.findViewById(R.id.followButton) as TextView
        likeImageView = findViewByPosition.findViewById(R.id.likeImageView) as ImageView

        bookmarkImageView = findViewByPosition.findViewById(R.id.bookmarkImageView) as ImageView

        val backButton = findViewByPosition.findViewById(R.id.backButton) as ImageView

        backButton.setOnClickListener {
            finish()
        }

        likeImageView.setOnClickListener {
            val userData = SharedPreference().getPreferenceString(this, "user_id")

            if (!userData.isNullOrEmpty()) {
                likeApiRequest()
            } else {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }
        }

        bookmarkImageView.setOnClickListener {
            val userData = SharedPreference().getPreferenceString(this, "user_id")

            if (!userData.isNullOrEmpty()) {
                bookMarkAPiRequest()
            } else {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }
        }

        followButton.setOnClickListener {
            val userData = SharedPreference().getPreferenceString(this, "user_id")

            if (!userData.isNullOrEmpty()) {
                folowApiRequest()
            } else {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }
        }

        videoPlayButton.setOnClickListener {
            videoPlayButton.visibility = View.GONE
            videoPauseButton.visibility = View.VISIBLE
            mediaPlayer?.playWhenReady = false
        }

        videoPauseButton.setOnClickListener {
            videoPlayButton.visibility = View.VISIBLE
            videoPauseButton.visibility = View.GONE
            mediaPlayer?.playWhenReady = true
        }


        unmuteButton.setOnClickListener {
            mediaPlayer?.audioComponent?.volume = 0f
            muteButton.visibility = View.VISIBLE
            unmuteButton.visibility = View.GONE
        }


        muteButton.setOnClickListener {
            val maxVolume = audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC) ?: 100
            mediaPlayer?.audioComponent?.volume = 5f

            muteButton.visibility = View.GONE
            unmuteButton.visibility = View.VISIBLE
        }


        videoPlayButton.visibility = View.VISIBLE
        videoImageView.visibility = View.VISIBLE
        val imageUrl = contentHomeList[position].thumbs?.get(0)?.thumb?.large



        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(this@ShortsVideoActivity).load(imageUrl).error(R.color.black)
                .into(videoImageView)
        }


        music01 = findViewByPosition.findViewById(R.id.music_01) as ImageView
        music02 = findViewByPosition.findViewById(R.id.music_02) as ImageView
        ivSoundTrack_HomeFragLay =
            findViewByPosition.findViewById(R.id.ivSoundTrack_HomeFragLay) as ImageView

        callFirstAnimation()
        val rotate = RotateAnimation(
            0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f
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

        val mediaItem = getMediaItem(
            contentHomeList.get(position).url.toString()
        )
        httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setAllowCrossProtocolRedirects(true)

        defaultDataSourceFactory = DefaultDataSourceFactory(
            this, httpDataSourceFactory
        )

        cacheDataSourceFactory = CacheDataSource.Factory()
            .setCache(simpleCache)
            .setUpstreamDataSourceFactory(httpDataSourceFactory)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)

        val customLoadControl = getCustomLoadControl()
        mediaPlayer = getMediaPLayerInstance(customLoadControl, trackSelector!!, videoAdsUrl)
        mediaPlayer?.addListener(playerStateListener)
        styledPlayerView.player = mediaPlayer
        styledPlayerView.controllerHideOnTouch = true
        styledPlayerView.keepScreenOn = true
        styledPlayerView.useController = false
        styledPlayerView.setControllerHideDuringAds(true)


        val hlsMediaSource = HlsMediaSource.Factory(cacheDataSourceFactory)
            .createMediaSource(mediaItem)
        mediaPlayer?.setMediaSource(hlsMediaSource)
        mediaPlayer?.repeatMode = Player.REPEAT_MODE_ONE
        mediaPlayer?.prepare()
        mediaPlayer?.playWhenReady = true
        updatePlayPauseButton()

        startUpdates(timeBar)
        mCurPos = position

        val userData = SharedPreference().getPreferenceString(this, "user_id")

        if (!userData.isNullOrEmpty()) {
            userBehivourRequest()
        }


        val volume = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) as Int

        if (volume <= 1) {
            muteButton.visibility = View.VISIBLE
            unmuteButton.visibility = View.GONE
        } else {
            muteButton.visibility = View.GONE
            unmuteButton.visibility = View.VISIBLE
        }
    }


    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (event.keyCode === KeyEvent.KEYCODE_VOLUME_DOWN) {
            if (styledPlayerView.visibility == View.VISIBLE) {
                val volume = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) as Int
                if (volume <= 1) {
                    muteButton.visibility = View.VISIBLE
                    unmuteButton.visibility = View.GONE
                } else {
                    muteButton.visibility = View.GONE
                    unmuteButton.visibility = View.VISIBLE
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        if (event.keyCode === KeyEvent.KEYCODE_VOLUME_UP) {
            if (styledPlayerView.visibility == View.VISIBLE) {
                val volume = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) as Int
                if (volume <= 1) {
                    muteButton.visibility = View.VISIBLE
                    unmuteButton.visibility = View.GONE
                } else {
                    muteButton.visibility = View.GONE
                    unmuteButton.visibility = View.VISIBLE
                }
            }
        }

        return super.onKeyUp(keyCode, event)
    }

    private var ambientModeJob: Job? = null


    private fun startUpdates(timeBar: DefaultTimeBar) {
        stopUpdates()
        ambientModeJob = lifecycleScope.launch {
            while (true) {
                withContext(Dispatchers.Main) {
                    timeBar.setPosition(mediaPlayer?.currentPosition as Long)
                    exo_position.text = getAndDisplayDuration()
                    updateTimeBarProgress(timeBar)
                }
                delay(1000)
            }
        }
    }

    private fun getAndDisplayDuration(): String {
        val durationMs: Long = mediaPlayer?.currentPosition as Long
        val durationFormatted: String = formatDuration(durationMs)
        return durationFormatted
    }

    private fun formatDuration(durationMs: Long): String {
        val totalSeconds = (durationMs / 1000).toInt()
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }


    private fun stopUpdates() {
        ambientModeJob?.cancel()
        ambientModeJob = null
    }

    private fun updateTimeBarProgress(timeBar: DefaultTimeBar) {
        mediaPlayer?.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == ExoPlayer.STATE_READY) {
                    timeBar.setDuration(mediaPlayer!!.getDuration())
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (isPlaying) {
                    timeBar.setPosition(mediaPlayer!!.getCurrentPosition())
                }
            }
        })

        timeBar.addListener(object : OnScrubListener {
            override fun onScrubStart(timeBar: TimeBar, position: Long) {
                // Handle scrub start
            }

            override fun onScrubMove(timeBar: TimeBar, position: Long) {
                // Handle scrub move
                mediaPlayer?.seekTo(position)
            }

            override fun onScrubStop(timeBar: TimeBar, position: Long, canceled: Boolean) {
                // Handle scrub stop
                mediaPlayer?.seekTo(position)
            }
        })
    }

    private fun updatePlayPauseButton() {/* var requestPlayPauseFocus = false
         val playing = mediaPlayer != null && mediaPlayer!!.playWhenReady
         requestPlayPauseFocus =
             requestPlayPauseFocus or (playing && videoPlayButton.isFocused)
         videoPlayButton.visibility = if (playing) FrameLayout.GONE else FrameLayout.VISIBLE
         requestPlayPauseFocus =
             requestPlayPauseFocus or (!playing && videoPauseButton.isFocused)
         videoPauseButton.visibility = if (!playing) FrameLayout.GONE else FrameLayout.VISIBLE
         if (requestPlayPauseFocus) {
             requestPlayPauseFocus()
         }*/
    }


    private fun requestPlayPauseFocus() {
        val playing = mediaPlayer != null && mediaPlayer!!.playWhenReady
        if (!playing) videoPlayButton.requestFocus()
        else videoPauseButton.requestFocus()

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

            if (videoSize.width > videoSize.height) styledPlayerView.setResizeMode(
                AspectRatioFrameLayout.RESIZE_MODE_FIT
            )
            else styledPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL)


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

        val mediaItemBuilder = MediaItem.Builder().setUri(videoUrl)
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

        return DefaultLoadControl.Builder().setBufferDurationsMs(
            MIN_BUFFER_DURATION,
            MAX_BUFFER_DURATION,
            BUFFER_FOR_PLAYBACK,
            BUFFER_FOR_PLAYBACK_AFTER_RE_BUFFER
        ).setAllocator(DefaultAllocator(true, ALLOCATION_SIZE))
            .setBackBuffer(BACK_BUFFER_DURATION, false).setPrioritizeTimeOverSizeThresholds(true)
            .setTargetBufferBytes(C.LENGTH_UNSET).build()

    }

    private fun getMediaPLayerInstance(
        customLoadControl: LoadControl, trackSelector: TrackSelector, adsUrl: String?
    ): ExoPlayer {

        return ExoPlayer.Builder(this)
            .setMediaSourceFactory(DefaultMediaSourceFactory(cacheDataSourceFactory))
            .setLoadControl(customLoadControl)
            .setTrackSelector(trackSelector).setSeekForwardIncrementMs(seekForwardIncrementMs)
            .setSeekBackIncrementMs(seekBackIncrementMs).build()

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
        stopUpdates()
    }

    override fun onResume() {
        super.onResume()
        resumeVideoPlayer()
    }

    override fun onLoadMore() {

    }

    override fun shareVideo(shareUrl: String) {
        CommonUtils().shareIntent(shareUrl, this)
    }


}