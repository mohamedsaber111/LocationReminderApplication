package com.udacity.project4.utils

import androidx.test.espresso.idling.CountingIdlingResource

//EspressoIdlingResource used in actual code not testCode to track whether long running tasks are still working
//it is going to be singleton class contain EspressoIdlingResource on it
object EspressoIdlingResource {

    private const val RESOURCE = "GLOBAL"

    @JvmField
    //CountingIdlingResource() allow you to increment and decrement count
    //such that when counter greater than zero app consider working when counter less than zero app is idle
    val countingIdlingResource = CountingIdlingResource(RESOURCE)

    // so when app start doing some work we are increment the counter, when work finish you will decrement counter
    fun increment() {
        countingIdlingResource.increment()
    }

    fun decrement() {
        if (!countingIdlingResource.isIdleNow) {
            countingIdlingResource.decrement()
        }
    }
}

//wrapEspressoIdlingResource start by incrementing count run
inline fun <T> wrapEspressoIdlingResource(function: () -> T): T {
    // Espresso does not work well with coroutines yet. See
    // https://github.com/Kotlin/kotlinx.coroutines/issues/982
    EspressoIdlingResource.increment() // Set app as busy.
    return try {
        // whatever code it's wrapped around
        function()
    } finally {
        //make sure to always decrement count
        EspressoIdlingResource.decrement() // Set app as idle.
    }
}