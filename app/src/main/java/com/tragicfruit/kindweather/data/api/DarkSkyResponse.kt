package com.tragicfruit.kindweather.data.api

import com.tragicfruit.kindweather.data.model.ForecastDataPoint
import com.tragicfruit.kindweather.data.model.ForecastDataType
import com.tragicfruit.kindweather.data.model.ForecastIcon
import com.tragicfruit.kindweather.data.model.ForecastPeriod
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.util.*

@Serializable(with = ForecastSerializer::class)
data class Forecast(
    val periods: List<ForecastPeriod>,
    val dataPoints: List<ForecastDataPoint>
)

class ForecastSerializer : KSerializer<Forecast> {
    override val descriptor = ForecastResponse.serializer().descriptor

    override fun deserialize(decoder: Decoder): Forecast {
        val response = ForecastResponse.serializer().deserialize(decoder)

        val periods = mutableListOf<ForecastPeriod>()
        val dataPoints = mutableListOf<ForecastDataPoint?>()
        response.daily.data.forEach { dataPoint ->
            val forecastId = UUID.randomUUID().toString()

            val icon = ForecastIcon.values().find {
                dataPoint.icon?.replace("-", "").equals(it.name, ignoreCase = true)
            } ?: ForecastIcon.Unknown

            periods += ForecastPeriod(
                id = forecastId,
                latitude = response.latitude,
                longitude = response.longitude,
                reportedTime = dataPoint.time,
                summary = dataPoint.summary,
                icon = icon,
                fetchedTime = System.currentTimeMillis()
            )

            dataPoints += createData(forecastId, ForecastDataType.TempHigh, dataPoint.temperatureHigh)
            dataPoints += createData(forecastId, ForecastDataType.TempLow, dataPoint.temperatureLow)
            dataPoints += createData(forecastId, ForecastDataType.PrecipIntensity, dataPoint.precipIntensity)
            dataPoints += createData(forecastId, ForecastDataType.PrecipProbability, dataPoint.precipProbability)
            dataPoints += createData(forecastId, ForecastDataType.Humidity, dataPoint.humidity)
            dataPoints += createData(forecastId, ForecastDataType.WindGust, dataPoint.windGust)
            dataPoints += createData(forecastId, ForecastDataType.UVIndex, dataPoint.uvIndex?.toDouble())
        }

        return Forecast(
            periods = periods,
            dataPoints = dataPoints.mapNotNull { it }
        )
    }

    private fun createData(forecastId: String, type: ForecastDataType, rawValue: Double?): ForecastDataPoint? {
        rawValue ?: return null
        return ForecastDataPoint(
            id = UUID.randomUUID().toString(),
            forecastId = forecastId,
            dataType = type,
            rawValue = rawValue,
            fetchedTime = System.currentTimeMillis()
        )
    }

    override fun serialize(encoder: Encoder, value: Forecast) = throw UnsupportedOperationException()
}

@Serializable
data class ForecastResponse(
    val latitude: Double,
    val longitude: Double,
    val timezone: String,
    val daily: Daily
) {

    @Serializable
    data class Daily(
        val summary: String?,
        val icon: String?,
        val data: List<DataPoint>
    ) {

        @Serializable
        data class DataPoint(
            val time: Long,
            val summary: String?,
            val icon: String?,
            val precipIntensity: Double?,
            val precipProbability: Double?,
            val precipType: String?,
            val temperatureHigh: Double?,
            val temperatureLow: Double?,
            val dewPoint: Double?,
            val humidity: Double?,
            val pressure: Double?,
            val windSpeed: Double?,
            val windGust: Double?,
            val cloudCover: Double?,
            val uvIndex: Int?,
            val visibility: Double?,
            val ozone: Double?
        )
    }
}
