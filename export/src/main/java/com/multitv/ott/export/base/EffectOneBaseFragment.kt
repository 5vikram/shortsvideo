package com.volcengine.effectone.export.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlin.coroutines.CoroutineContext

abstract class EffectOneBaseFragment: Fragment() {
    companion object {
        const val TAG = "EffectOneBaseFragment"
    }


    @get:LayoutRes
    abstract val layoutResourceId: Int
    lateinit var rootView: View
    private var defaultScope: CoroutineScope? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(layoutResourceId, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.rootView = view
        initView()
        initObserver()
        initData()
    }

    abstract fun getFragmentTag(): String
    open fun initView() {
        //ignore
    }

    open fun initObserver() {
        //ignore
    }

    open fun initData() {
        //ignore
    }


    override fun onDestroyView() {
        super.onDestroyView()
        runCatching { defaultScope?.cancel() }
    }


    fun requireDefaultScope(): CoroutineScope {
        if (defaultScope == null) {
            val job = SupervisorJob()
            defaultScope = object : CoroutineScope {
                override val coroutineContext: CoroutineContext
                    get() = job + Dispatchers.Default + CoroutineName(TAG)

            }
        }
        return defaultScope!!
    }
}