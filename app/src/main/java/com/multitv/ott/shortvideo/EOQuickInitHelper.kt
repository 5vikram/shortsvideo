package com.multitv.ott.shortvideo

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.Log
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.FragmentActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.bytedance.creativex.mediaimport.repository.api.IMaterialItem
import com.bytedance.creativex.mediaimport.repository.api.isVideo
import com.volcengine.auth.api.EOAuthErrCode
import com.volcengine.auth.api.EOAuthorization
import com.volcengine.auth.core.EOAuthConfig
import com.volcengine.auth.core.EOAuthResData
import com.volcengine.ck.album.AlbumEntrance
import com.volcengine.ck.album.api.IAlbumFinish
import com.volcengine.ck.album.base.AlbumConfig
import com.volcengine.ck.logkit.DebugLogger
import com.volcengine.ck.logkit.LogKit
import com.volcengine.easyeditor.api.MediaType
import com.volcengine.effectone.EffectOneSdk
import com.volcengine.effectone.api.EffectOneConfigList
import com.volcengine.effectone.base.PhotoEditingMode
import com.volcengine.effectone.editorui.EditorMainActivity
import com.volcengine.effectone.editorui.audiofilter.AudioFilterUIConfig
import com.volcengine.effectone.editorui.draft.EditorDraftActivity
import com.volcengine.effectone.editorui.effect.EffectPanelUIConfig
import com.volcengine.effectone.editorui.sticker.infosticker.InfoStickerUIConfig
import com.volcengine.effectone.editorui.sticker.text.data.TextStickerUIConfig
import com.volcengine.effectone.editorui.transition.TransitionUIConfig
import com.volcengine.effectone.filter.FilterUIConfig
import com.volcengine.effectone.image.ImageLoader
import com.volcengine.effectone.image.ImageOption
import com.volcengine.effectone.image.ImageSource
import com.volcengine.effectone.music.base.EOBaseMusicUIConfig
import com.volcengine.effectone.permission.Scene
import com.volcengine.effectone.recordersdk.RecordMediaType
import com.volcengine.effectone.recorderui.EORecordActivity
import com.volcengine.effectone.recorderui.base.RecorderInitConfig
import com.volcengine.effectone.recorderui.beauty.BeautyUIConfig
import com.volcengine.effectone.recorderui.util.RecordUtils
import com.volcengine.effectone.resource.api.EOResourceConfig
import com.volcengine.effectone.resource.api.EOResourceManager
import com.volcengine.effectone.resource.api.EOResourcePanelKey
import com.volcengine.effectone.resource.impl.DefaultLocalResourceLoader
import com.volcengine.effectone.singleton.AppSingleton
import com.volcengine.effectone.sticker.data.EOBaseStickerUIConfig
import com.volcengine.effectone.utils.EOUtils
import com.volcengine.effectone.widget.EOToaster
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale


interface IQuickInit {
    fun initApplication(application: Application) {}
    fun prepareAndInit(callback: (Boolean, String) -> Unit) {}
    fun startRecorder(activity: FragmentActivity) {}
    fun startDraft(activity: ComponentActivity) {}
    fun startEditorFromAlbum(activity: FragmentActivity) {}
}

object EOQuickInitHelper : IQuickInit {
    var licenseFileName: String =
        "multitv1_test_20241029_20241129_com.multitv.ott.shortvideo_1.5.0_212.licbag"

    override fun initApplication(application: Application) {
        //绑定AppContext
        AppSingleton.bindInstance(application)
        //素材sdk初始化
        resourceInit()
        //初始化EffectOneSdk
        EffectOneSdk.run {
            imageLoader = DefaultImageLoader()
            logger = DebugLogger()
            modelPath = EOResourceManager.getModelRootPath()
            photoEditingMode = PhotoEditingMode.MODE_PICTURE
        }
    }

    override fun prepareAndInit(callback: (Boolean, String) -> Unit) {
        CoroutineScope(Dispatchers.Main).launch(Dispatchers.Main) {
            initConfigs()
            val authResult = withContext(Dispatchers.IO) {
                auth()
            }
            if (authResult.resCode != EOAuthErrCode.EO_AUTH_SUCCESS) {
                EOToaster.show(AppSingleton.instance, "eo_frontpage_auth_fail")
                callback(false, authResult.resCode.toString())
                Log.e("VIKRAM:::", authResult.resMsg)
            } else {
                callback(true, "auth success")
            }
        }
    }

    private fun resourceInit() {
        // 指定素材文件保存目录、素材使用语言
        val config = EOResourceConfig.Builder()
            .setResourceSavePath(EOUtils.pathUtil.internalResource())
            .setSysLanguage(Locale.getDefault().language)
            .build()
        // 素材sdk初始化
        EOResourceManager.init(config)
    }

    private fun auth(): EOAuthResData {
        val authConfig = EOAuthConfig()
        // 根据文件名，拷贝离线鉴权文件到App私有目录下
        if (!getLicenseFile().exists()) {
            EOUtils.fileUtil.copyAssetFile(
                "${EOUtils.pathUtil.assetsLicensePath}/$licenseFileName",
                getLicenseFile().absolutePath
            )
        }
        authConfig.offlineLicensePath = getLicenseFile().absolutePath
        return EOAuthorization.makeAuthWithConfig(authConfig)
    }

    private fun getLicenseFile(): File {
        return File(EOUtils.pathUtil.internalLicense(), licenseFileName)
    }

    private fun initConfigs() {
        initEditorConfigs()
        initRecorderConfig()
        initPlugins()
//        //Fragment自定义
//        EffectOneConfigList.configure<InjectorConfig> {
//            it.hook<TextStickerInputFragment>(TtsTextStickerInputFragment::class.java)
//            it.hook<TextSticker>(TtsTextSticker::class.java)
//            it.hook<StickerDurationPanelFragment>(TtsStickerDurationPanelFragment::class.java)
//        }
    }

    //用户hook插件代码
    private fun initPlugins() {

    }

    override fun startDraft(activity: ComponentActivity) {
        EditorDraftActivity.startDraftActivity(activity)
    }

    override fun startEditorFromAlbum(activity: FragmentActivity) {
        EOUtils.permission.checkPermissions(activity, Scene.ALBUM, {
            val albumConfig = AlbumConfig(
                allEnable = true,
                imageEnable = true,
                videoEnable = true,
                maxSelectCount = EffectOneSdk.albumMaxSelectedCount,
                finishClazz = StartEditorFinishImpl::class.java
            )
            AlbumEntrance.startChooseMedia(
                activity, 1001, albumConfig
            )
        }, {
            AlbumEntrance.showAlbumPermissionTips(activity)
        })
    }

    fun initEditorConfigs() {
        //编辑和拍摄通用面板配置
        //滤镜面板
        EffectOneConfigList.configure(FilterUIConfig()) {
            it.resourceLoader = DefaultLocalResourceLoader.instance
        }
        //人脸特效面板
        EffectOneConfigList.configure(EOBaseStickerUIConfig()) {
            it.resourceLoader = DefaultLocalResourceLoader.instance
        }
        //音乐面板
        EffectOneConfigList.configure(EOBaseMusicUIConfig()) {
            it.resourceLoader = DefaultLocalResourceLoader.instance
        }

        //编辑专用面板配置
        //信息贴纸面板
        EffectOneConfigList.configure(InfoStickerUIConfig()) {
            it.resourceLoader = DefaultLocalResourceLoader.instance
        }
        //文字面板
        EffectOneConfigList.configure(TextStickerUIConfig()) {
            it.resourceLoader = DefaultLocalResourceLoader.instance
        }
        //视频特效面板
        EffectOneConfigList.configure(EffectPanelUIConfig()) {
            it.resourceLoader = DefaultLocalResourceLoader.instance
        }
        //音频特效面板
        EffectOneConfigList.configure(AudioFilterUIConfig()) {
            it.resourceLoader = DefaultLocalResourceLoader.instance
        }
        //转场面板
        EffectOneConfigList.configure(TransitionUIConfig()) {
            it.resourceLoader = DefaultLocalResourceLoader.instance
        }
    }

    //启动拍摄页
    override fun startRecorder(activity: FragmentActivity) {
        EOUtils.permission.checkPermissions(activity, Scene.RECORDER, {
            EORecordActivity.startRecord(activity)
        }, {
            RecordUtils.showRecordPermissionTips(activity, it)
        })
    }

    fun initRecorderConfig() {
        //拍摄模块整体配置
        EffectOneConfigList.configure(RecorderInitConfig()) {
            it.finishAction = { activity, list, musicItem, ext ->
                val options = ActivityOptionsCompat.makeCustomAnimation(activity, 0, 0)
                EditorMainActivity.startEditorActivityFromRecord(
                    activity,
                    list.map { resourceItem ->
                        com.volcengine.easyeditor.api.MediaItem(
                            path = resourceItem.path,
                            type = if (resourceItem.type == RecordMediaType.VIDEO) MediaType.VIDEO else MediaType.IMAGE,
                            duration = resourceItem.duration,
                            speed = resourceItem.speed
                        )
                    } as ArrayList<com.volcengine.easyeditor.api.MediaItem>,
                    options.toBundle(),
                    musicItem,
                    ext)
            }
            it.albumConfig = AlbumConfig(
                allEnable = true,
                imageEnable = true,
                videoEnable = true,
                maxSelectCount = EffectOneSdk.albumMaxSelectedCount,
                //maxDuration Unit:ms
                maxDuration = 60 * 60 * 1000L,
                finishClazz = StartEditorFinishImpl::class.java
            )
        }

        //拍摄专用面板配置
        //美颜面板
        EffectOneConfigList.configure(BeautyUIConfig()) {
            it.resourceLoader = DefaultLocalResourceLoader.instance
            it.defaultBeautyAction = {
                EOResourceManager.syncLoadConfigByPanelKey(
                    EOResourcePanelKey.RECORDER_BEAUTY.value,
                    true
                ).tabs
            }
        }

        initResourceData()
    }

    // 加载默认资源
    private fun initResourceData() {
        CoroutineScope(Dispatchers.IO).launch {
            EOResourceManager.loadDefaultResources()
        }
    }

}

fun ImageOption.toGlideRequestOption(): RequestOptions {
    var requestOptions = RequestOptions()
    if (skipDiskCached) {
        requestOptions = requestOptions.diskCacheStrategy(DiskCacheStrategy.NONE)
    }
    requestOptions = requestOptions.skipMemoryCache(false)
    if (this.width != 0 && this.height != 0) {
        requestOptions = requestOptions.override(this.width, this.height)
    }
    if (this.placeHolder != 0) {
        requestOptions = requestOptions.placeholder(this.placeHolder)
    }
    when (this.scaleType) {
        ImageView.ScaleType.CENTER_CROP -> {
            requestOptions = requestOptions.centerCrop()
        }

        ImageView.ScaleType.CENTER_INSIDE -> {
            requestOptions = requestOptions.centerInside()
        }

        ImageView.ScaleType.FIT_CENTER -> {
            requestOptions = requestOptions.fitCenter()
        }

        else -> {}
    }
    when (this.format) {
        Bitmap.Config.ARGB_8888 -> {
            requestOptions = requestOptions.format(DecodeFormat.PREFER_ARGB_8888)
        }

        Bitmap.Config.RGB_565 -> {
            requestOptions = requestOptions.format(DecodeFormat.PREFER_RGB_565)
        }

        else -> {
            //do nothing
        }
    }
    return requestOptions
}

class DefaultImageLoader : ImageLoader {
    companion object {
        private const val TAG = "ImageLoader"
    }

    private fun buildRequest(
        option: ImageOption?,
        source: ImageSource<*>,
        context: Context
    ): RequestBuilder<out Any>? {
        val sourceValue = source.getValue() ?: return null

        var requestBuilder = Glide.with(context)
            .`as`(option?.transcodeType ?: Drawable::class.java)
            .load(sourceValue)

        option?.let {
            requestBuilder = requestBuilder.apply(it.toGlideRequestOption())
        }
        bindListener(requestBuilder, option)
        return requestBuilder
    }

    override fun <T> loadImageView(
        imageView: ImageView,
        imageSource: ImageSource<T>,
        option: ImageOption?
    ) {
        buildRequest(option, imageSource, imageView.context)?.into(imageView)
    }


    override fun <T> loadBitmapSync(
        context: Context,
        imageSource: ImageSource<T>,
        option: ImageOption
    ): Bitmap? {
        option.transcodeType = Bitmap::class.java
        return buildRequest(option, imageSource, AppSingleton.instance)
            ?.submit(option.width, option.height)
            ?.get() as? Bitmap
    }


    @SuppressLint("DiscouragedApi")
    private fun setLocalImage(imageView: ImageView, builtInIcon: String, option: ImageOption?) {
        val assetsIcon = "file:///android_asset/Resource_icons/$builtInIcon"
        LogKit.d(TAG, "assets icon is $assetsIcon")
        loadImageView(imageView, assetsIcon, option)
    }

    private fun getIconName(builtInIcon: String): String {
        val lastDotIndex = builtInIcon.lastIndexOf(".")
        return if (lastDotIndex != -1) {
            builtInIcon.substring(0, lastDotIndex)
        } else {
            ""
        }
    }


    private fun <TranscodeType> bindListener(
        requestBuilder: RequestBuilder<TranscodeType>,
        option: ImageOption?
    ) {
        option?.listener?.let {
            requestBuilder.listener(object : RequestListener<TranscodeType> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<TranscodeType>?,
                    isFirstResource: Boolean
                ): Boolean {
                    return it.onLoadFailed(e)
                }

                override fun onResourceReady(
                    resource: TranscodeType,
                    model: Any?,
                    target: Target<TranscodeType>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    return it.onResourceReady(resource)
                }
            })
        }
    }
}


class StartEditorFinishImpl : IAlbumFinish {
    override suspend fun finishAction(
        activity: Activity,
        mediaList: List<IMaterialItem>,
        albumConfig: AlbumConfig
    ) {
        EditorMainActivity.startEditorActivityFromAlbum(activity, mediaList.map {
            com.volcengine.easyeditor.api.MediaItem(
                path = it.path,
                type = if (it.isVideo()) MediaType.VIDEO else MediaType.IMAGE,
                duration = if (it.isVideo()) it.duration else 3000
            )
        } as ArrayList<com.volcengine.easyeditor.api.MediaItem>)

        //关闭相册页
        if (!activity.isFinishing) {
            activity.finish()
        }
    }
}

