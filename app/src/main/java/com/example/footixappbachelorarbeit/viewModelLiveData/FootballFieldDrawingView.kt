package com.example.footixappbachelorarbeit.viewModelLiveData

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.example.footixappbachelorarbeit.R
import com.example.footixappbachelorarbeit.viewModelLiveData.FieldCalculationUtil.Companion.angleBetweenPoints
import com.example.footixappbachelorarbeit.viewModelLiveData.FieldCalculationUtil.Companion.getMinMaxValues
import com.example.footixappbachelorarbeit.viewModelLiveData.FieldCalculationUtil.Companion.getScaling
import com.example.footixappbachelorarbeit.viewModelLiveData.FieldCalculationUtil.Companion.rotatePoint
import com.example.footixappbachelorarbeit.viewModelLiveData.FieldCalculationUtil.Companion.transformCoordinates
import org.osgeo.proj4j.ProjCoordinate


class FootballFieldDrawingView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    var counter = 1

    private val paint = Paint().apply {
        color = Color.WHITE
        strokeWidth = 5f
        style = Paint.Style.STROKE
    }

    private lateinit var gnssStartPointOne: GNSSPositionCoords
    private lateinit var gnssStartPointTwo: GNSSPositionCoords

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        drawFieldInit(canvas)

        // Position Rendering
        drawPosition(canvas, calculateCurrentPosition())

        /*when (counter) {
            1 -> drawPositionOneLeft(canvas)
            2 -> drawPositionTwoRight(canvas)
        }*/
    }

    private fun calculateCurrentPosition(): ProjCoordinate {
        val latitudes = doubleArrayOf(49.0966610, 49.0961931, 49.0967969, 49.0972568)
        val longitudes = doubleArrayOf(8.9698321, 8.9704132, 8.9715452, 8.9709557)

        val currentLat = 49.0970071
        val currentlong = 8.9712296

        val initialCoords: ArrayList<ProjCoordinate> = ArrayList<ProjCoordinate>()

        for (i in longitudes.indices) {
            initialCoords.add(transformCoordinates(longitudes[i], latitudes[i]))
        }

        // herausfinden welches lange/kurze Seite ist und dementsprechend ausrichten (falsch gespiegelt?)..
        val angle: Double =
            angleBetweenPoints(initialCoords[0], initialCoords[1]) + Math.toRadians(180.0)


        val rotatedCoords: ArrayList<ProjCoordinate> = ArrayList();
        for (coord: ProjCoordinate in initialCoords) {
            rotatedCoords.add(rotatePoint(coord.x, coord.y, -angle))
        }

        val minMaxValues: FieldCalculationUtil.Companion.MinMaxValues =
            getMinMaxValues(rotatedCoords)

        val scale: Double = getScaling(rotatedCoords, minMaxValues, width, height)
        rotatedCoords.stream().forEach { c: ProjCoordinate ->
            c.setValue(
                (c.x - minMaxValues.minLong) * scale,
                (c.y - minMaxValues.minLat) * scale
            )
        }

        var projCoordinate = transformCoordinates(currentlong, currentLat)
        projCoordinate = rotatePoint(projCoordinate.x, projCoordinate.y, -angle)
        projCoordinate.setValue(
            (projCoordinate.x - minMaxValues.minLong) * scale,
            (projCoordinate.y - minMaxValues.minLat) * scale
        )
        return projCoordinate
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

    private fun drawPosition(canvas: Canvas?, projCoordinate: ProjCoordinate) {
        val pointRadius = 24f

        val pointStyle = Paint().apply {
            color = ContextCompat.getColor(context, R.color.yellow_footix_light)
            style = Paint.Style.FILL_AND_STROKE
        }
        canvas?.drawCircle(projCoordinate.x.toFloat(), projCoordinate.y.toFloat(), pointRadius, pointStyle)
    }

    fun updateField(longitude: Double, latitude: Double) {
        if (longitude == 0.0 || latitude == 0.0) {
            Log.e("updateField", "still waiting for lat and long $longitude and $latitude")
        } else {
            if (counter == 1) {
                var punkt1_long = 8.9698102//8.9709420
                var punkt1_lat = 49.0966608//49.0965378
                gnssStartPointOne = GNSSPositionCoords(punkt1_lat, punkt1_long)
                Log.e("updateField", "RECEIVED Start position ONE $longitude and $latitude")
            } else if (counter == 2) {
                var punkt2_long = 8.9704159//8.9704415
                var punkt2_lat = 49.0961792//49.0969584
                gnssStartPointTwo = GNSSPositionCoords(punkt2_lat, punkt2_long)
                Log.e("updateField", "RECEIVED Start position TWO $longitude and $latitude")

            } else if (counter == 3) {
                var punkt2_long = 8.9715593//8.9704415
                var punkt2_lat = 49.0967875//49.0969584
                gnssStartPointTwo = GNSSPositionCoords(punkt2_lat, punkt2_long)
                Log.e("updateField", "RECEIVED Start position TWO $longitude and $latitude")
            } else if (counter == 4) {
                var punkt2_long = 8.9709535//8.9704415
                var punkt2_lat = 49.0972751//49.0969584
                gnssStartPointTwo = GNSSPositionCoords(punkt2_lat, punkt2_long)
                Log.e("updateField", "RECEIVED Start position TWO $longitude and $latitude")
            } else {


            }
            counter++
        }
        invalidate()
    }

}