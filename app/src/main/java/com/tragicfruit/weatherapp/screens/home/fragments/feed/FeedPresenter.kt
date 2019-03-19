package com.tragicfruit.weatherapp.screens.home.fragments.feed

import com.tragicfruit.weatherapp.model.WeatherNotification
import io.realm.Realm
import io.realm.RealmChangeListener
import io.realm.RealmResults
import io.realm.Sort
import io.realm.kotlin.where

class FeedPresenter(override var view: FeedContract.View) : FeedContract.Presenter,
    RealmChangeListener<RealmResults<WeatherNotification>> {

    private lateinit var feedData: RealmResults<WeatherNotification>

    override fun init() {
        feedData = Realm.getDefaultInstance()
            .where<WeatherNotification>()
            .sort("createdAt", Sort.DESCENDING)
            .findAll()

        view.initView(feedData)
    }

    override fun resume() {
        view.refreshFeed()
        feedData.addChangeListener(this)
    }

    override fun pause() {
        feedData.removeChangeListener(this)
    }

    override fun onChange(t: RealmResults<WeatherNotification>) {
        view.refreshFeed()
    }

}