package com.volcengine.effectone.export.base

import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner

interface IBaseUIHelper : LifecycleObserver {
    val activity: FragmentActivity
    val owner: LifecycleOwner
    fun initView(rootViewGroup: ViewGroup)
}