package com.kourt.app.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * JUnit rule that swaps the Main dispatcher for a [TestDispatcher] for the duration of a test.
 *
 * Use [scheduler] to advance time / run pending coroutines from inside `runTest`.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainCoroutineRule(
    val scheduler: TestCoroutineScheduler = TestCoroutineScheduler(),
    val testDispatcher: TestDispatcher = StandardTestDispatcher(scheduler),
) : TestWatcher() {

    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
