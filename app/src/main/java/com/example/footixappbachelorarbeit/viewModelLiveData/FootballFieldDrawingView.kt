package com.example.footixappbachelorarbeit.viewModelLiveData

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import com.example.footixappbachelorarbeit.R


class FootballFieldDrawingView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    val EARTH_RADIUS = 6378137.0 // Radius of the Earth in meters

    val RADIUS_MAJOR = 6378137.0
    val RADIUS_MINOR = 6356752.3142

    var centerX = 0f // Mittelpunkt für X
    var centerY = 0f // Mittelpunkt für Y
    var left = 0f // Left boundary
    var right = 0f // Right boundary
    var top = 0f // Top boundary
    var bottom = 0f // Bottom boundary
    var counter = 1

    var point1 = azimutalProjection(8.9698102, 49.0966608, 0.0, 0.0)//Point2D(getMercatorXAxis(49.096662), getMercatorYAxis(8.969794))
    var point2 = azimutalProjection(8.9704159, 49.0961792, 0.0, 0.0)//Point2D(getMercatorXAxis(49.0961786), getMercatorYAxis(8.9704049))
    var point3 = azimutalProjection(8.9715593, 49.0967875, 0.0, 0.0)//Point2D(getMercatorXAxis(49.0967883), getMercatorYAxis(8.9715579))
    var point4 = azimutalProjection(8.9709535, 49.0972751, 0.0, 0.0)//Point2D(getMercatorXAxis(49.0972706), getMercatorYAxis(8.9709473))
    data class Point2D(val x: Double, val y: Double)

    private val paint = Paint().apply {
        color = Color.WHITE
        strokeWidth = 5f
        style = Paint.Style.STROKE
    }

    private lateinit var gnssStartPointOne: GNSSPositionCoords
    private lateinit var gnssStartPointTwo: GNSSPositionCoords

    private val path = Path()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        initField()

        drawFieldInit(canvas)

        mapToRelativeCoordinates()

        drawPosition(canvas)

        /*when (counter) {
            1 -> drawPositionOneLeft(canvas)
            2 -> drawPositionTwoRight(canvas)
        }*/
    }

    private fun initField() {
        centerX = width / 2f // width = 1216
        centerY = height / 2f // height = 1855
        left = (centerX - width * 0.48f) // 583,68
        right = (centerX + width * 0.48f)
        top = (centerY - height * 0.48f)
        bottom = (centerY + height * 0.48f)
    }

    private fun drawFieldInit(canvas: Canvas?) {

        // Entire football field
        canvas?.drawRect(left, top, right, bottom, paint)

        // Horizontal middle line
        canvas?.drawLine(left, centerY, right, centerY, paint)

        // Circle in the middle
        val radius = width * 0.22f
        canvas?.drawCircle(centerX, centerY, radius, paint)

        // Rectangles
        val rectLeft1 = centerX - width / 4
        val rectRight1 = centerX + width / 4
        val rectTop1 = (centerY + height * 0.3f)
        val rectBottom1 = bottom//centerY - height

        val rectLeft2 = centerX - width / 4
        val rectRight2 = centerX + width / 4
        val rectTop2 = top
        val rectBottom2 = (centerY - height * 0.3f)

        canvas?.drawRect(
            rectLeft1, rectTop1,
            rectRight1, rectBottom1, paint
        )

        canvas?.drawRect(
            rectLeft2, rectTop2,
            rectRight2, rectBottom2, paint
        )

        canvas?.drawPath(path, paint)
    }

    /*private fun drawPositionOneLeft(canvas: Canvas?) {
        val centerX = left // width * 0.98f //width / 2f
        val centerY = height / 2f
        val pointRadius = 24f

        val pointPaint = Paint().apply {
            color = ContextCompat.getColor(context, R.color.yellow_footix_light)
            style = Paint.Style.FILL_AND_STROKE
        }

        canvas?.drawCircle(centerX, centerY, pointRadius, pointPaint)
    }*/

    private fun drawPosition(canvas: Canvas?) {
        val minX = 8.9697940
        val maxX = 8.9715579
        val minY = 49.0961884
        val maxY = 49.0972982

        val point = convertToCanvasCoordinates(8.9712794, 49.0972982, minX, maxX, minY, maxY, canvas!!.height, canvas.width)

        val pointRadius = 24f

        val pointStyle = Paint().apply {
            color = ContextCompat.getColor(context, R.color.yellow_footix_light)
            style = Paint.Style.FILL_AND_STROKE
        }
        canvas?.drawCircle(point.x.toFloat(), point.y.toFloat(), pointRadius, pointStyle)
    }

   /* private fun drawPositionTwoRight(canvas: Canvas?) {
        val minX = 8.9704049
        val maxX = 8.9709261
        val minY = 49.0966620
        val maxY = 49.0967883

        val point = convertToCanvasCoordinates(8.9715579, 49.0967883, minX, maxX, minY, maxY, canvas!!.height, canvas.width)


        val pointRadius = 24f

        val pointPaint2 = Paint().apply {
            color = ContextCompat.getColor(context, R.color.yellow_footix_light)
            style = Paint.Style.FILL_AND_STROKE
        }
        canvas?.drawCircle(point.x.toFloat(), point.y.toFloat(), pointRadius, pointPaint2)
    }*/

    fun convertToCanvasCoordinates(longitude: Double, latitude: Double, minX: Double, maxX: Double, minY: Double, maxY: Double, canvasHeight: Int, canvasWidth: Int): Point2D {
        // Calculate the scaling factors based on the bounding box dimensions
        val scaleX = canvasWidth / (maxX - minX)
        val scaleY = canvasHeight / (maxY - minY)

        // Convert longitude and latitude to canvas coordinates within the bounding box
        val x = (longitude - minX) * scaleX

        val y = Math.abs((maxY - latitude) * scaleY)
        return Point2D(x, y)
    }

    fun mapToRelativeCoordinates() {
        var referencePoint = point1;

        point2 = Point2D(point2.x - referencePoint.x, point2.y - referencePoint.y)
        point3 = Point2D(point3.x - referencePoint.x, point3.y - referencePoint.y)
        point4 = Point2D(point4.x - referencePoint.x, point4.y - referencePoint.y)
        point1 = Point2D(0.0,0.0)
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
                var punkt1_long = 8.9709420
                var punkt1_lat = 49.0965378
                var point1 = Point2D(getMercatorXAxis(punkt1_lat), getMercatorYAxis(punkt1_long))

                var punkt2_long = 8.9704415
                var punkt2_lat = 49.0969584
                var point2 = Point2D(getMercatorXAxis(punkt2_lat), getMercatorYAxis(punkt2_long))

                // 4 Eckpunkte rausfinden und hard codieren als globale
                // bestimmten welche Koordinate Urpsrung ist (alle relativ dazu)
                // wie viel pixel entsprechen einer längeneinheit (Koordinatensystem anpassen)
                // af zeichnung mappen

                calculateEuclideanDistance(point1.x, point1.y, point2.x, point2.y)
            }
            counter++
        }
        invalidate()
    }

/*    fun getMercatorXAxis(input: Double): Double {
        return Math.toRadians(input) * EARTH_RADIUS
    }


    fun getMercatorYAxis(input: Double): Double {
        return Math.log(Math.tan(Math.PI / 4 + Math.toRadians(input) / 2)) * EARTH_RADIUS
    }*/

    fun getMercatorYAxis(input: Double): Double {
        var input = input
        input = Math.min(Math.max(input, -89.5), 89.5)
        val earthDimensionalRateNormalized = 1.0 - Math.pow(RADIUS_MINOR / RADIUS_MAJOR, 2.0)
        var inputOnEarthProj = Math.sqrt(earthDimensionalRateNormalized) *
                Math.sin(Math.toRadians(input))
        inputOnEarthProj = Math.pow(
            (1.0 - inputOnEarthProj) / (1.0 + inputOnEarthProj),
            0.5 * Math.sqrt(earthDimensionalRateNormalized)
        )
        val inputOnEarthProjNormalized =
            Math.tan(0.5 * (Math.PI * 0.5 - Math.toRadians(input))) / inputOnEarthProj
        return -1 * RADIUS_MAJOR * Math.log(inputOnEarthProjNormalized)
    }

    fun getMercatorXAxis(input: Double): Double {
        return RADIUS_MAJOR * Math.toRadians(input)
    }

     fun azimutalProjection( longitude: Double,  latitude: Double, centerX: Double, centerY: Double): Point2D {
         var lonRadians = Math.toRadians(longitude);
         var latRadians = Math.toRadians(latitude);

         // Berechne die Projektion
         var x = Math.cos(latRadians) * Math.sin(lonRadians - Math.toRadians(centerX));
         var y = Math.sin(latRadians) * Math.cos(Math.toRadians(centerY))
         - Math.cos(latRadians) * Math.sin(Math.toRadians(centerY)) * Math.cos(lonRadians - Math.toRadians(centerX));


         return Point2D(x, y);
     }

    fun calculateRefMiddlePoint(
        startPointOne: GNSSPositionCoords,
        startPointTwo: GNSSPositionCoords
    ) {
        val lat1 = startPointOne.latitude
        val long1 = startPointOne.longitude
        //Log.e("calculateRefMiddlePoint", "POS 1 $long1 and $lat1")
        val lat2 = startPointTwo.latitude
        val long2 = startPointTwo.longitude
        //Log.e("calculateRefMiddlePoint", "POS 2 $long2 and $lat2")

        val midLat = (lat1 + lat2) / 2.0
        val midLon = (long1 + long2) / 2.0

        Log.e("MIDDLEPOINT", "midLat & midLon $midLat and $midLon")

        //transformCoordinatesToMiddlePoint()

        //var distancebetween = distanceBetweenCoordinates(lat1, long1, lat2, long2)
        //Log.d("Distance", "Distance calculated: $distancebetween meters")
    }

    fun calculateEuclideanDistance(x1: Double, y1: Double, x2: Double, y2: Double): Double {
        return Math.sqrt((y2 - y1) * (y2 - y1) + (x2 - x1) * (x2 - x1))
    }
}