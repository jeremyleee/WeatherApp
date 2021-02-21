package com.tragicfruit.kindweather.utils.controllers

import android.content.Context
import android.content.Intent
import androidx.core.app.JobIntentService
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationResult
import com.tragicfruit.kindweather.R
import com.tragicfruit.kindweather.data.AlertRepository
import com.tragicfruit.kindweather.data.ForecastRepository
import com.tragicfruit.kindweather.data.NotificationRepository
import com.tragicfruit.kindweather.data.model.ForecastDataType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class AlertService : JobIntentService() {

    @Inject lateinit var alertRepository: AlertRepository
    @Inject lateinit var notificationRepository: NotificationRepository
    @Inject lateinit var forecastRepository: ForecastRepository
    @Inject lateinit var notificationController: NotificationController

    override fun onHandleWork(intent: Intent) {
        if (LocationResult.hasResult(intent)) {
            val location = LocationResult.extractResult(intent).lastLocation

            Timber.d("Fetching forecast")
            CoroutineScope(Dispatchers.IO).launch {
                val success = forecastRepository.fetchForecast(location.latitude, location.longitude)
                if (!success) {
                    // TODO: handle retry
                }

                displayWeatherAlert()
            }
        }
    }

    private suspend fun displayWeatherAlert() {
        forecastRepository.findForecastForToday()?.let { forecast ->
            // Highest priority alert
            val showAlert = alertRepository.findAlertMatchingForecast(forecast)

            val alertType = showAlert?.alertType
            val message = getString(alertType?.title ?: R.string.feed_entry_default)
            val color = ContextCompat.getColor(this, alertType?.color ?: R.color.alert_no_notification)

            // Create model object for feed
            val notification = notificationRepository.createNotification(
                description = message,
                color = color,
                forecastIcon = forecast.icon,
                rawTempHigh = forecastRepository.findDataPointForType(forecast, ForecastDataType.TempHigh)?.rawValue,
                rawTempLow = forecastRepository.findDataPointForType(forecast, ForecastDataType.TempLow)?.rawValue,
                rawPrecipProbability = forecastRepository.findDataPointForType(forecast, ForecastDataType.PrecipProbability)?.rawValue,
                latitude = forecast.latitude,
                longitude = forecast.longitude
            )
            if (showAlert != null) {
                Timber.i("Showing push notification: $message")
                notificationController.notifyWeatherAlert(this, notification)
            }
        }
    }

    companion object {
        private const val JOB_ID = 1000

        fun enqueueWork(context: Context, work: Intent) {
            enqueueWork(context, AlertService::class.java, JOB_ID, work)
        }
    }
}