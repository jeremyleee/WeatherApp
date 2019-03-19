package com.tragicfruit.weatherapp.model

import com.tragicfruit.weatherapp.controllers.ForecastResponse
import io.realm.Realm
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.kotlin.createObject
import timber.log.Timber

/**
 * Forecast for a day
 */
open class ForecastPeriod : RealmObject() {

    var latitude = 0.0; private set
    var longitude = 0.0; private set

    var time: Long = 0; private set
    var summary = ""; private set
    var icon = ""; private set
    var data = RealmList<ForecastData>(); private set

    var fetchedTime: Long = 0; private set

    fun satisfiesParam(param: WeatherAlertParam): Boolean {
        val lowerBound = param.lowerBound
        val upperBound = param.upperBound

        val data = getDataForType(param.getType())

        // If data doesn't exist, treat it as zero
        val observedValue = data?.value ?: 0.0

        val satisfiesLowerBound = lowerBound == null || observedValue >= lowerBound
        val satisfiesUpperBound = upperBound == null || observedValue <= upperBound

        Timber.d("param:${param.getType().name}; observed:$observedValue; lowerbound:$lowerBound; upperbound:$upperBound; satisfies:${satisfiesLowerBound && satisfiesUpperBound}")

        return satisfiesLowerBound && satisfiesUpperBound
    }

    fun getHighTemp(): Double {
        val data = getDataForType(ForecastType.Temp_high) ?: return 0.0
        return data.value
    }

    private fun getDataForType(type: ForecastType) =
        data.where().equalTo("type", type.name).findFirst()

    companion object {

        fun fromResponse(responseData: ForecastResponse.Daily.DataPoint,
                         latitude: Double, longitude: Double, realm: Realm): ForecastPeriod {
            val forecastPeriod = realm.createObject<ForecastPeriod>()

            forecastPeriod.latitude = latitude
            forecastPeriod.longitude = longitude

            forecastPeriod.time = responseData.time
            forecastPeriod.summary = responseData.summary ?: forecastPeriod.summary
            forecastPeriod.icon = responseData.icon ?: forecastPeriod.icon

            when (responseData.precipType) {
                "rain" -> {
                    forecastPeriod.data.add(ForecastData.create(ForecastType.Rain_intensity, responseData.precipIntensity, realm))
                    forecastPeriod.data.add(ForecastData.create(ForecastType.Rain_probability, responseData.precipProbability, realm))
                }
                "snow" -> {
                    forecastPeriod.data.add(ForecastData.create(ForecastType.Snow_intensity, responseData.precipIntensity, realm))
                    forecastPeriod.data.add(ForecastData.create(ForecastType.Snow_probability, responseData.precipProbability, realm))
                }
                "sleet" -> {
                    // Do nothing for now
                }
            }

            forecastPeriod.data.add(ForecastData.create(ForecastType.Temp_high, responseData.temperatureHigh, realm))
            forecastPeriod.data.add(ForecastData.create(ForecastType.Temp_low, responseData.temperatureLow, realm))
            forecastPeriod.data.add(ForecastData.create(ForecastType.Humidity, responseData.humidity, realm))
            forecastPeriod.data.add(ForecastData.create(ForecastType.Wind_gust, responseData.windGust, realm))
            forecastPeriod.data.add(ForecastData.create(ForecastType.Uv_index, responseData.uvIndex?.toDouble(), realm))

            forecastPeriod.fetchedTime = System.currentTimeMillis()

            return forecastPeriod
        }

    }

}