package com.tragicfruit.kindweather.ui.home.settings

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.tragicfruit.kindweather.util.SharedPrefsHelper
import com.tragicfruit.kindweather.util.controllers.AlertController
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val alertController: AlertController,
    private val sharedPrefsHelper: SharedPrefsHelper
) : ViewModel() {

    private val _alertTime: MutableLiveData<AlertTime> = MutableLiveData(
        AlertTime(sharedPrefsHelper.getAlertHour(), sharedPrefsHelper.getAlertMinute())
    )
    val alertTime: LiveData<AlertTime> = _alertTime

    private val _useImperialUnits: MutableLiveData<Boolean> = MutableLiveData(
        sharedPrefsHelper.usesImperialUnits()
    )
    val useImperialUnits: LiveData<Boolean> = _useImperialUnits

    fun updateAlertTime(context: Context, hour: Int, minute: Int) {
        sharedPrefsHelper.setAlertHour(hour)
        sharedPrefsHelper.setAlertMinute(minute)
        _alertTime.value = AlertTime(hour, minute)

        // Restart alert service for new alert time
        alertController.scheduleDailyAlert(context)
    }

    fun updateUnits(useImperial: Boolean) {
        sharedPrefsHelper.setUsesImperialUnits(useImperial)
        _useImperialUnits.value = useImperial
    }

    companion object {
        const val DARK_SKY_URL = "https://darksky.net/poweredby/"
    }
}

data class AlertTime(
    val hour: Int,
    val minute: Int
)
