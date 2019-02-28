package com.tragicfruit.weatherapp.screens.alert.fragments.list

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tragicfruit.weatherapp.model.WeatherAlert
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import io.realm.kotlin.where

class AlertListAdapter(private val listener: AlertCell.Listener) : RecyclerView.Adapter<AlertViewHolder>() {

    private val alertList: RealmResults<WeatherAlert>

    init {
        alertList = Realm.getDefaultInstance()
            .where<WeatherAlert>()
            .sort("enabled", Sort.DESCENDING, "dateAdded", Sort.ASCENDING)
            .findAll()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlertViewHolder {
        return AlertViewHolder(AlertCell(parent.context, listener))
    }

    override fun onBindViewHolder(holder: AlertViewHolder, position: Int) {
        alertList[position]?.let { alert ->
            val cell = holder.itemView as? AlertCell
            cell?.setData(alert)
        }
    }

    override fun getItemCount() = alertList.count()

}

class AlertViewHolder(itemView: AlertCell) : RecyclerView.ViewHolder(itemView)