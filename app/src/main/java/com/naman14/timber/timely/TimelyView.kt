/*
* Copyright 2014 Adnan A M.
* Copyright 2015 Naman Dwivedi.

* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at

*   http://www.apache.org/licenses/LICENSE-2.0

* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.naman14.timber.timely

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.util.Property
import android.view.View
import com.naman14.timber.R
import com.naman14.timber.timely.animation.TimelyEvaluator
import com.naman14.timber.timely.model.NumberUtils

class TimelyView : View {
    private var mPaint: Paint? = null
    private var mPath: Path? = null
    private var controlPoints: Array<FloatArray>? = null
    private var textColor = 0

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.TimelyView)
        textColor = typedArray.getColor(R.styleable.TimelyView_text_color, Color.BLACK)
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    fun getControlPoints(): Array<FloatArray>? {
        return controlPoints
    }

    fun setControlPoints(controlPoints: Array<FloatArray>?) {
        this.controlPoints = controlPoints
        invalidate()
    }

    fun animate(start: Int, end: Int): ObjectAnimator {
        val startPoints = NumberUtils.getControlPointsFor(start)
        val endPoints = NumberUtils.getControlPointsFor(end)
        return ObjectAnimator.ofObject(this, CONTROL_POINTS_PROPERTY, TimelyEvaluator(), startPoints, endPoints)
    }

    fun animate(end: Int): ObjectAnimator {
        val startPoints = NumberUtils.getControlPointsFor(-1)
        val endPoints = NumberUtils.getControlPointsFor(end)
        return ObjectAnimator.ofObject(this, CONTROL_POINTS_PROPERTY, TimelyEvaluator(), startPoints, endPoints)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (controlPoints == null) return
        val length = controlPoints!!.size
        val height = measuredHeight
        val width = measuredWidth
        val minDimen = if (height > width) width.toFloat() else height.toFloat()
        mPath!!.reset()
        mPath!!.moveTo(minDimen * controlPoints!![0][0], minDimen * controlPoints!![0][1])
        var i = 1
        while (i < length) {
            mPath!!.cubicTo(minDimen * controlPoints!![i][0], minDimen * controlPoints!![i][1],
                    minDimen * controlPoints!![i + 1][0], minDimen * controlPoints!![i + 1][1],
                    minDimen * controlPoints!![i + 2][0], minDimen * controlPoints!![i + 2][1])
            i += 3
        }
        canvas.drawPath(mPath, mPaint)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        var width = measuredWidth
        var height = measuredHeight
        val widthWithoutPadding = width - paddingLeft - paddingRight
        val heigthWithoutPadding = height - paddingTop - paddingBottom
        val maxWidth = (heigthWithoutPadding * RATIO).toInt()
        val maxHeight = (widthWithoutPadding / RATIO).toInt()
        if (widthWithoutPadding > maxWidth) {
            width = maxWidth + paddingLeft + paddingRight
        } else {
            height = maxHeight + paddingTop + paddingBottom
        }
        setMeasuredDimension(width, height)
    }

    private fun init() {
        // A new paint with the style as stroke.
        mPaint = Paint()
        mPaint!!.isAntiAlias = true
        mPaint!!.color = textColor
        mPaint!!.strokeWidth = 5.0f
        mPaint!!.style = Paint.Style.STROKE
        mPath = Path()
    }

    companion object {
        private const val RATIO = 1f
        private val CONTROL_POINTS_PROPERTY: Property<TimelyView, Array<FloatArray>> = object : Property<TimelyView, Array<FloatArray>>(Array<FloatArray>::class.java, "controlPoints") {
            override fun get(`object`: TimelyView): Array<FloatArray>? {
                return `object`.getControlPoints()
            }

            override fun set(`object`: TimelyView, value: Array<FloatArray>?) {
                `object`.setControlPoints(value)
            }
        }
    }
}