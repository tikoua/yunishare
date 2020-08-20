package com.uneed.yuni

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.View

/**
 *   created by dcl
 *   on 2020/7/7 5:00 PM
 */
class YuniDownloadProgressView : View {
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context) : super(context)

    private val log: (String) -> Unit = {
        Log.d("ProgressView", it)
    }

    private var progress = 0

    companion object {
        private const val DefaultWidth = 40
        private const val DefaultHeight = 40
    }

    private var realWidth: Int = 0
    private var realHeight: Int = 0

    private val textPaint by lazy {
        Paint().apply {
            style = Paint.Style.FILL
            color = Color.parseColor("#ffffff")
            isAntiAlias = true
        }
    }
    private val circleProgressPaint by lazy {
        Paint().apply {
            style = Paint.Style.STROKE
            color = Color.parseColor("#ffffff")
            isAntiAlias = true
            strokeWidth = 10F
            strokeCap = Paint.Cap.ROUND
        }
    }
    private val backPaint by lazy {
        Paint().apply {
            style = Paint.Style.FILL
            color = Color.parseColor("#24a0ff")
            isAntiAlias = true
        }
    }
    private val shadowPaint by lazy {
        Paint().apply {
            style = Paint.Style.FILL
            color = Color.parseColor("#1724a0ff")
            isAntiAlias = true
        }
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val heightMode = MeasureSpec.getMode(widthMeasureSpec)
        val maxWidth = when (widthMode) {
            MeasureSpec.AT_MOST -> widthSize.coerceAtMost(DefaultWidth)
            MeasureSpec.EXACTLY -> widthSize
            else -> widthSize.coerceAtMost(DefaultWidth)
        }
        val maxHeight = when (heightMode) {
            MeasureSpec.AT_MOST -> heightSize.coerceAtMost(DefaultHeight)
            MeasureSpec.EXACTLY -> heightSize
            else -> heightSize.coerceAtMost(DefaultHeight)
        }
        val size = maxHeight.coerceAtMost(maxWidth)

        realWidth = size
        realHeight = size
        setMeasuredDimension(realWidth, realHeight)
        log("onMeasure  size: $size  widthSize: $widthSize  heightSize: $heightSize")
    }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val size = w.coerceAtMost(h)
        realWidth = size
        realHeight = size
        log("onSizeChanged  size: $size  widthSize: $w  heightSize: $h")
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawCircle(realWidth / 2F, realHeight / 2F, realHeight / 2F, shadowPaint)
        canvas.drawCircle(realWidth / 2F, realHeight / 2F, realHeight / 2F - 10, backPaint)
        canvas.drawArc(
            15F,
            15F,
            realWidth - 15F,
            realHeight - 15F,
            -90F,
            progress / 100F * 360,
            false,
            circleProgressPaint
        )
    }

    fun setProgress(progress: Int) {
        this.progress = progress
        postInvalidate()
    }
}