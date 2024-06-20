package com.example.footixappbachelorarbeit.viewModelLiveData

import org.osgeo.proj4j.CRSFactory
import org.osgeo.proj4j.CoordinateTransform
import org.osgeo.proj4j.CoordinateTransformFactory
import org.osgeo.proj4j.ProjCoordinate


class FieldCalculationUtil {
    companion object {
        private var transform: CoordinateTransform? = CoordinateTransformFactory().createTransform(
            CRSFactory().createFromName("EPSG:4326"),
            CRSFactory().createFromName("EPSG:32629")
        )

        data class MinMaxValues(
            val minLat: Double,
            val maxLat: Double,
            val minLong: Double,
            val maxLong: Double
        )

        fun transformCoordinates(longitude: Double, latitude: Double): ProjCoordinate {
            val destCoord = ProjCoordinate()
            transform?.transform(ProjCoordinate(longitude, latitude), destCoord)
            return destCoord
        }

        fun angleBetweenPoints(coord1: ProjCoordinate, coord2: ProjCoordinate): Double {
            val slope: Double = (coord2.y - coord1.y) / (coord2.x - coord1.x)
            return Math.atan(slope)
        }

        fun rotatePoint(x: Double, y: Double, angle: Double): ProjCoordinate {
            val cosTheta = Math.cos(angle)
            val sinTheta = Math.sin(angle)
            return ProjCoordinate(x * cosTheta - y * sinTheta, x * sinTheta + y * cosTheta)
        }

        fun getScaling(
            coordinates: List<ProjCoordinate>,
            minMaxValues: MinMaxValues,
            width: Int,
            height: Int
        ): Double {
            val scaleX: Double = width / (minMaxValues.maxLong - minMaxValues.minLong)
            val scaleY: Double = height / (minMaxValues.maxLat - minMaxValues.minLat)
            return Math.min(scaleX, scaleY)
        }

        fun getMinMaxValues(coordinates: List<ProjCoordinate>): MinMaxValues {
            val minLat: Double =
                coordinates.stream().map { coord -> coord.y }.mapToDouble { d -> d }
                    .min().getAsDouble()
            val maxLat: Double =
                coordinates.stream().map { coord -> coord.y }.mapToDouble { d -> d }
                    .max().getAsDouble()
            val minLong: Double =
                coordinates.stream().map { coord -> coord.x }.mapToDouble { d -> d }
                    .min().getAsDouble()
            val maxLong: Double =
                coordinates.stream().map { coord -> coord.x }.mapToDouble { d -> d }
                    .max().getAsDouble()
            return MinMaxValues(minLat, maxLat, minLong, maxLong)
        }
    }
}