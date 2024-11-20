package com.volcengine.effectone.export.viewmode

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import com.volcengine.effectone.editorui.external.data.EOFps
import com.volcengine.effectone.editorui.external.data.EOOutputVideoSettings
import com.volcengine.effectone.editorui.external.data.EOResolution
import com.volcengine.effectone.viewmodel.BaseViewModel
import com.volcengine.effectone.viewmodel.EffectOneViewModelFactory

class EffectOneVideoEncodeViewModel(activity: FragmentActivity) : BaseViewModel(activity) {
    companion object {
        const val TAG = "EffectOneVideoEncodeViewModel"

        fun get(activity: FragmentActivity): EffectOneVideoEncodeViewModel {
            return EffectOneViewModelFactory.viewModelProvider(activity).get(EffectOneVideoEncodeViewModel::class.java)
        }
    }

    private val _defaultEoOutputVideoSettings by lazy { EOOutputVideoSettings() }
    private var selectResolutionFps: Pair<EOResolution, EOFps> =
        Pair(EOResolution.RES_720P, EOFps.FPS_30)

    private val _resolutionFps = MutableLiveData<Pair<EOResolution, EOFps>>()
    val resolutionFps: MutableLiveData<Pair<EOResolution, EOFps>> get() = _resolutionFps

    init {
        selectResolutionFps = defaultPair()
        saveResolutionFps()
    }

    fun setDefaultResolution(resolutionFps: Pair<EOResolution, EOFps>) {
        selectResolutionFps = resolutionFps
    }

    fun changeSelectResolution(resolution: EOResolution) {
        selectResolutionFps = resolution to selectResolutionFps.second
    }

    fun changeSelectFps(fps: EOFps) {
        selectResolutionFps = selectResolutionFps.first to fps
    }

    fun resetResolutionFps() {
        selectResolutionFps = defaultPair()
    }


    fun saveResolutionFps() {
        _resolutionFps.value = selectResolutionFps
    }

    private fun defaultPair() =
        _defaultEoOutputVideoSettings.outputRes to _defaultEoOutputVideoSettings.outputFps
}
