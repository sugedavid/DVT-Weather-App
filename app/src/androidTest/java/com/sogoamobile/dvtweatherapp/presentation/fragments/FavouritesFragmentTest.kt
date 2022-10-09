package com.sogoamobile.dvtweatherapp.presentation.fragments

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.launchActivity
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sogoamobile.dvtweatherapp.R
import com.sogoamobile.dvtweatherapp.data.location.LocationDao
import com.sogoamobile.dvtweatherapp.data.location.LocationDatabase
import com.sogoamobile.dvtweatherapp.data.location.LocationTable
import com.sogoamobile.dvtweatherapp.presentation.MainActivity
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class FavouritesFragmentTest {
    private lateinit var locationDao: LocationDao
    private lateinit var database: LocationDatabase
    private lateinit var locationTable: LocationTable
    private lateinit var emptyLocationTable: LocationTable

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context, LocationDatabase::class.java
        ).build()
        locationDao = database.locationDao()

        // fill data to db
        locationTable = LocationTable(
            id = 0,
            cityName = "Nairobi",
            description = "description",
            refreshTime = 1665348664414,
            temperature = 12,
            temperatureMin = 11,
            temperatureMax = 13,
            isFavourite = true,
            latitude = "0.0",
            longitude = "0.0",
        )

    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        database.close()
    }

    @Test
    fun launchFragmentAndVerifyUI() {
        locationDao.addLocation(locationTable)
        // use launchInContainer to launch the fragment with UI
        launchFragmentInContainer<FavouritesFragment>()

        onView(withText(R.string.favourites)).check(matches(isDisplayed()))

        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            onView(withText("Nairobi")).check(matches(isDisplayed()))
        }, 5000)

    }

    @Test
    fun shouldNavigateToHomeFragment() {
        locationDao.addLocation(locationTable)
        launchActivity<MainActivity>()

        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({

            // navigate to home fragment
            val favItemView = withText("Nairobi")
            onView(favItemView)
                .check(matches(isDisplayed()))
            onView(favItemView)
                .perform(click())

            // assert
            val drawerView = ViewMatchers.withId(R.id.imgDrawerMenu)
            onView(drawerView)
                .check(matches(isDisplayed()))

        }, 5000)
    }

    @Test
    fun shouldNavigateToMapsFragment() {
        locationDao.addLocation(locationTable)
        launchActivity<MainActivity>()

        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({

            // navigate to map fragment
            val mapView = ViewMatchers.withId(R.id.imgMapMenu)
            onView(mapView)
                .check(matches(isDisplayed()))
            onView(mapView)
                .perform(click())

            // assert
            val backView = ViewMatchers.withId(R.id.imgBackBtn)
            onView(backView)
                .check(matches(isDisplayed()))

        }, 5000)
    }

    @Test
    fun shouldRenderEmptyState() {
        launchActivity<MainActivity>()

        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({

            // assert
            val emptyStateView = ViewMatchers.withId(R.id.txtNoFavourites)
            onView(emptyStateView)
                .check(matches(isDisplayed()))

        }, 5000)
    }
}