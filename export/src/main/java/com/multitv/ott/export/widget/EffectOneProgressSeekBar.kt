package com.volcengine.effectone.export.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatSeekBar
import com.volcengine.effectone.utils.SizeUtil
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class EffectOneProgressSeekBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatSeekBar(context, attrs, defStyleAttr) {

    private var rangeStart = 0
    private var rangEnd = 100
    private var suffixIndicator = ""
    private var prefixIndicator = ""

    private val textPaint by lazy {
        Paint().apply {
            isAntiAlias = true
            color = Color.WHITE
            textSize = SizeUtil.dp2pxF(14F)
        }
    }

    private val textBaseLineY by lazy { paddingTop.toFloat() }

    /**
     * 调用此方式仅仅影响Indicator显示的文案，不影响SeekBar的原有逻辑
     */
    fun setRange(start: Int, end: Int): EffectOneProgressSeekBar {
        rangeStart = start
        rangEnd = end
        return this
    }

    /**
     * 设置显示文案前缀缀 比如 "当前$progress"
     */
    fun setPrefixIndicator(preFixStr: String): EffectOneProgressSeekBar {
        prefixIndicator = preFixStr
        return this
    }

    /**
     * 设置显示文案后缀 比如 "$progress%"
     */
    fun setSuffixIndicator(suffixStr: String): EffectOneProgressSeekBar {
        suffixIndicator = suffixStr
        return this
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val text = "$prefixIndicator${getCurrentValue()}$suffixIndicator"
        val textWidth = textPaint.measureText(text)
        val progressRect = progressDrawable.bounds
        val paddingSL = max(paddingStart, paddingLeft)
        val currentProgressWidth = progressRect.width().times(progress).div(max.toFloat())

        val halfWidth = (textWidth / 2).toInt()
        //修正偏移
        val offset = max(halfWidth - paddingSL, 0)
        val x =
            min(currentProgressWidth - halfWidth + paddingSL - offset, currentProgressWidth)

        canvas.drawText(text, x, textBaseLineY, textPaint)
    }

    /**
     * 低版本7.0以下没有两个参数的方法
     * @link http://androidxref.com/6.0.1_r10/xref/frameworks/base/core/java/android/widget/ProgressBar.java
     */
    override fun setProgress(progress: Int) {
        if (getProgress() != progress) {
            super.setProgress(progress)
            invalidate()
        }
    }

    override fun setProgress(progress: Int, animate: Boolean) {
        if (getProgress() != progress) {
            super.setProgress(progress, animate)
            invalidate()
        }
    }

    private fun getCurrentValue(): Int {
        val value: Int = (progress * 1F / max * (rangEnd - rangeStart) + rangeStart).roundToInt()
        return min(max(value, rangeStart), rangEnd)
    }

}