package com.sogoamobile.dvtweatherapp

import com.sogoamobile.dvtweatherapp.common.Common
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.*

class CommonUnitTest {

    private val longDate:Long = 1212580300

    @Test
    fun convertUnixToDateIsCorrect() {
        assertEquals(Common().convertUnixToDate(longDate), "Wed, 4 Jun, 2008")
    }

    @Test
    fun convertUnixToDayIsCorrect() {
        assertEquals(Common().convertUnixToDay(longDate), "Wednesday")
    }

    @Test
    fun convertUnixToHourIsCorrect() {
        assertEquals(Common().convertUnixToHour(longDate), "Wed, 2:51 pm")
    }

    @Test
    fun changeFavouriteImageIsCorrect() {
        assertEquals(Common().changeFavouriteImage(true), R.drawable.ic_heart_white)
        assertEquals(Common().changeFavouriteImage(false), R.drawable.ic_heart_outline)
    }
}