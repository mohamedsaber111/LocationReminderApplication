package com.udacity.project4

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@ExperimentalCoroutinesApi
//add MainCoroutineRule() because we use ViewModelScope and we use coroutine
class MainCoroutineRule(val dispatcher: TestCoroutineDispatcher = TestCoroutineDispatcher()):

    TestWatcher(),
    TestCoroutineScope by TestCoroutineScope(dispatcher) {

    override fun starting(description: Description?) {
        super.starting(description)
        //swap dispatchers to Dispatchers setMain before every test run
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description?) {
        super.finished(description)
        //reset coroutine when finish test
        cleanupTestCoroutines()
        Dispatchers.resetMain()
    }
}