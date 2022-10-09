package com.sogoamobile.dvtweatherapp.presentation

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.launchActivity
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sogoamobile.dvtweatherapp.data.location.LocationDao
import com.sogoamobile.dvtweatherapp.data.location.LocationDatabase
import com.sogoamobile.dvtweatherapp.data.location.LocationTable
import com.sogoamobile.dvtweatherapp.data.locationForecast.LocationForecastDao
import com.sogoamobile.dvtweatherapp.data.locationForecast.LocationForecastTable
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import com.sogoamobile.dvtweatherapp.R
import org.junit.Assert.assertEquals

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    private lateinit var locationDao: LocationDao
    private lateinit var locationForecastDao: LocationForecastDao
    private lateinit var database: LocationDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context, LocationDatabase::class.java
        ).build()
        locationDao = database.locationDao()
        locationForecastDao = database.locationForecastDao()

        // fill data to db
        val locationTable = LocationTable(
            id = 0,
            cityName = "Nairobi",
            description = "description",
            refreshTime = 1665348664414,
            temperature = 12,
            temperatureMin = 11,
            temperatureMax = 13,
            isFavourite = false,
            latitude = "0.0",
            longitude = "0.0",
        )

        val locationForecastTable = LocationForecastTable(
            id = 0,
            day = 1665348664414,
            main = "Rain",
            temperature = 12
        )

        locationDao.addLocation(locationTable)
        locationForecastDao.addLocationForecast(locationForecastTable)

    }

    @Test
    fun shouldLaunchMainActivity() {
        launchActivity<MainActivity>()

        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            onView(ViewMatchers.withId(R.id.imgDrawerMenu))
                .check(matches(isDisplayed()))
            onView(ViewMatchers.withId(R.id.imgSearchTab))
                .check(matches(isDisplayed()))
            onView(ViewMatchers.withId(R.id.imgWeatherFav))
                .check(matches(isDisplayed()))
            onView(ViewMatchers.withId(R.id.imgRefresh))
                .check(matches(isDisplayed()))
            onView(ViewMatchers.withText("Nairobi"))
            .check(matches(isDisplayed()))
        }, 5000)
    }

    @Test
    fun shouldNavigateToFavouriteFragment() {
        launchActivity<MainActivity>()

        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            // open drawer
            val drawerView = ViewMatchers.withId(R.id.imgDrawerMenu)
            onView(drawerView)
                .check(matches(isDisplayed()))
            onView(drawerView)
                .perform(click())

            // navigate to favourites fragment
            val navFavouriteView = ViewMatchers.withId(R.id.nav_favourites)
            onView(navFavouriteView)
                .check(matches(isDisplayed()))
            onView(navFavouriteView)
                .perform(click())
            onView(ViewMatchers.withText(R.string.favourites)).check(matches(isDisplayed()))

        }, 5000)
    }

    @Test
    fun shouldFavouriteLocationFragment() {
        launchActivity<MainActivity>()

        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            // favourite location
            val favouriteView = ViewMatchers.withId(R.id.img_favorite)
            onView(favouriteView)
                .check(matches(isDisplayed()))
            onView(favouriteView)
                .perform(click())

            assertEquals(locationDao.getLocation(0).value?.isFavourite, true)

        }, 5000)
    }

    @Test
    fun shouldSearchForALocationFragment() {
        launchActivity<MainActivity>()

        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            // favourite location
            val searchView = ViewMatchers.withId(R.id.imgSearchTab)
            onView(searchView)
                .check(matches(isDisplayed()))
            onView(searchView)
                .perform(click())

        }, 5000)
    }

}