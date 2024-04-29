package com.example.footixappbachelorarbeit.viewModelLiveData

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.os.Handler
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.example.footixappbachelorarbeit.R

class FootballFieldDrawingView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val paint = Paint().apply {
        color = Color.WHITE
        strokeWidth = 5f
        style = Paint.Style.STROKE
    }

    private val path = Path()

    private var secondPointVisible = false

    private val handler = Handler()

    private val secondPointRunnable = Runnable {
        secondPointVisible = true
        invalidate()
    }

    init {
        handler.postDelayed(secondPointRunnable, 5000) // Delay of 5 seconds (5000 milliseconds)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        drawField(canvas)

        drawPositionPoint(canvas)

        if (secondPointVisible) {
            drawSecondPoint(canvas)
        }
    }

    private fun drawField(canvas: Canvas?) {

        val centerX = width / 2
        val centerY = height / 2
        val fieldWidth = (width * 0.48f)
        val fieldHeigth = (height * 0.48f)

        val left = centerX - (fieldWidth)
        val right = centerX + (fieldWidth)
        val top = centerY - fieldHeigth
        val bottom = centerY + (fieldHeigth)

        // Entire football field
        canvas?.drawRect(left, top, right, bottom, paint)

        // Horizontal middle line
        canvas?.drawLine(left, centerY.toFloat(), right, centerY.toFloat(), paint)

        // Circle in the middle
        val radius = fieldHeigth / 4.5f
        canvas?.drawCircle(centerX.toFloat(), centerY.toFloat(), radius, paint)

        // Rectangles
        val rectLeft1 = centerX - fieldWidth / 2
        val rectRight1 = centerX + fieldWidth / 2
        val rectTop1 = centerY + fieldHeigth / 1.6f
        val rectBottom1 = centerY + fieldHeigth

        val rectLeft2 = centerX - fieldWidth / 2
        val rectRight2 = centerX + fieldWidth / 2
        val rectTop2 = centerY - fieldHeigth / 1.6f
        val rectBottom2 = centerY - fieldHeigth

        canvas?.drawRect(rectLeft1, rectTop1,
            rectRight1, rectBottom1, paint)

        canvas?.drawRect(rectLeft2, rectTop2,
            rectRight2, rectBottom2, paint)

        canvas?.drawPath(path, paint)
    }

    private fun drawPositionPoint(canvas: Canvas?) {
        val centerX = width / 2f
        val centerY = height / 2f
        val pointRadius = 16f

        val pointPaint = Paint().apply {
            color = ContextCompat.getColor(context, R.color.yellow_footix)
            style = Paint.Style.FILL_AND_STROKE
        }

        canvas?.drawCircle(centerX, centerY, pointRadius, pointPaint)
    }

    private fun drawSecondPoint(canvas: Canvas?) {
        val centerX = width / 2f
        val centerY = height / 2f

        // Calculate the coordinates of the second point relative to the middle point
        val xOffset = 50 // Example offset in the x-direction
        val yOffset = -50 // Example offset in the y-direction

        val secondPointX = centerX + xOffset
        val secondPointY = centerY + yOffset

        val pointRadius = 22f

        val pointPaint = Paint().apply {
            color = ContextCompat.getColor(context, R.color.yellow_footix)
            style = Paint.Style.FILL_AND_STROKE
        }

        canvas?.drawCircle(secondPointX, secondPointY, pointRadius, pointPaint)
    }

    fun updateField(){
        invalidate()
    }
}
