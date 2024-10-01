package com.multitv.ott.shortvideo.utils

class SystemVolumeEvent {
    private var listener: SystemVolumeEventListener? = null

    interface SystemVolumeEventListener {
        fun onKeyDownEvent()

        fun onKeyUpEvent()
    }

    fun setSystemVolumeEventListener(listener: SystemVolumeEventListener?) {
        this.listener = listener
    }


    fun onKeyDownEvent() {
        if (listener != null) {
            listener!!.onKeyDownEvent()
        }
    }

    fun onKeyUpEvent() {
        if (listener != null) {
            listener!!.onKeyUpEvent()
        }
    }

    companion object {
        private var mInstance: SystemVolumeEvent? = null
        val instance: SystemVolumeEvent?
            get() {
                if (mInstance == null) {
                    mInstance = SystemVolumeEvent()
                }
                return mInstance
            }
    }
}
