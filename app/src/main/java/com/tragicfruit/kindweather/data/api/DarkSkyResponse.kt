package com.tragicfruit.kindweather.data.api

import com.tragicfruit.kindweather.data.model.ForecastData
import com.tragicfruit.kindweather.data.model.ForecastPeriod
import com.tragicfruit.kindweather.data.model.ForecastType
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.util.*

@Serializable(with = ForecastSerializer::class)
data class Forecast(
    val data: List<ForecastPeriod>
)

class ForecastSerializer : KSerializer<Forecast> {
    override val descriptor = ForecastResponse.serializer().descriptor

    override fun deserialize(decoder: Decoder): Forecast {
        val response = ForecastResponse.serializer().deserialize(decoder)

        return Forecast(
            response.daily.data.map {
                ForecastPeriod().apply {
                    id = UUID.randomUUID().toString()
                    latitude = response.latitude
                    longitude = response.longitude
                    time = it.time
                    summary = it.summary ?: summary
                    icon = it.icon ?: icon
                    data.add(createData(ForecastType.TempHigh, it.temperatureHigh))
                    data.add(createData(ForecastType.TempLow, it.temperatureLow))
                    data.add(createData(ForecastType.PrecipIntensity, it.precipIntensity))
                    data.add(createData(ForecastType.PrecipProbability, it.precipProbability))
                    data.add(createData(ForecastType.Humidity, it.humidity))
                    data.add(createData(ForecastType.WindGust, it.windGust))
                    data.add(createData(ForecastType.UVIndex, it.uvIndex?.toDouble()))
                    fetchedTime = System.currentTimeMillis()
                }
            }
        )
    }

    private fun createData(type: ForecastType, rawValue: Double?): ForecastData? {
        rawValue ?: return null
        return ForecastData().apply {
            this.type = type.name
            this.rawValue = rawValue
            this.fetchedTime = System.currentTimeMillis()
        }
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
