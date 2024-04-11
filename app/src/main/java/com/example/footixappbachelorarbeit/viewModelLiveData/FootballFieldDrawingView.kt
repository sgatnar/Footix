package com.example.footixappbachelorarbeit.viewModelLiveData

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View

class FootballFieldDrawingView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val paint = Paint().apply {
        color = Color.WHITE
        strokeWidth = 5f
        style = Paint.Style.STROKE
    }

    private val path = Path()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        drawField(canvas)
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
}
