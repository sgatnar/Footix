package com.example.footixappbachelorarbeit.viewModelLiveData

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.os.Build
import android.util.AttributeSet
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.example.footixappbachelorarbeit.R
import org.osgeo.proj4j.ProjCoordinate


class FootballFieldDrawingView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    var counter = 1

    private lateinit var gnssStartPointOne: GNSSPositionCoords
    private lateinit var gnssStartPointTwo: GNSSPositionCoords

     var playerPosition: ProjCoordinate? = null


    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        drawFieldInit(canvas)

        drawPosition(canvas, playerPosition)

//        drawPosition(globalCanvas, projCoordinate)

        // Position Rendering

        /*when (counter) {
            1 -> drawPositionOneLeft(canvas)
            2 -> drawPositionTwoRight(canvas)
        }*/
    }

    private fun drawFieldInit(canvas: Canvas?) {
        val centerX = width / 2f
        val centerY = height / 2f
        val left = (centerX - width * 0.48f)
        val right = (centerX + width * 0.48f)
        val top = (centerY - height * 0.48f)
        val bottom = (centerY + height * 0.48f)

        // Entire football field
        canvas?.drawRect(left, top, right, bottom, paint)
        // Horizontal middle line
        canvas?.drawLine(left, centerY, right, centerY, paint)
        // Circle in the middle
        val radius = width * 0.22f
        canvas?.drawCircle(centerX, centerY, radius, paint)

        // Rectangles
        val rectLeft = centerX - width / 4
        val rectRight = centerX + width / 4
        val rectTop1 = (centerY + height * 0.3f)
        val rectBottom2 = (centerY - height * 0.3f)

        canvas?.drawRect(rectLeft, rectTop1, rectRight, bottom, paint)
        canvas?.drawRect(rectLeft, top, rectRight, rectBottom2, paint)
        canvas?.drawPath(Path(), paint)
    }

    private val paint = Paint().apply {
        color = Color.WHITE
        strokeWidth = 5f
        style = Paint.Style.STROKE
    }

    private fun drawPosition(canvas: Canvas?, projCoordinate: ProjCoordinate?) {
        if (projCoordinate == null) {
            return
        }

        val pointRadius = 24f

        val pointStyle = Paint().apply {
            color = ContextCompat.getColor(context, R.color.yellow_footix)
            style = Paint.Style.FILL_AND_STROKE
        }
        canvas?.drawCircle(
            projCoordinate.x.toFloat(),
            projCoordinate.y.toFloat(),
            pointRadius,
            pointStyle
        )
    }
}