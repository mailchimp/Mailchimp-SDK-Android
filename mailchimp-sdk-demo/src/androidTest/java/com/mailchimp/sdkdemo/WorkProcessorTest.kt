/*
 * Licensed under the Mailchimp Mobile SDK License Agreement (the "License");
 * you may not use this file except in compliance with the License. Unless
 * required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either or express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mailchimp.sdkdemo

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.Configuration
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.impl.utils.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import com.mailchimp.sdk.core.MailchimpSdkConfiguration
import com.mailchimp.sdk.core.work.WorkProcessor
import com.mailchimp.sdk.main.Mailchimp
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WorkProcessorTest {

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val fakeSdkKey = "sdkkey-us1"

        val config = MailchimpSdkConfiguration.Builder(context.applicationContext, fakeSdkKey).build()

        Mailchimp.initialize(context, config)

        val configuration =
            Configuration.Builder()
                .setMinimumLoggingLevel(Log.DEBUG)
                .setExecutor(SynchronousExecutor())
                .build()

        WorkManagerTestInitHelper.initializeTestWorkManager(context, configuration)
    }

    @Test
    fun testSubmitWork() {
        val work = TestWorkRequest1("test@test.com")

        val workProcessor = WorkProcessor(WorkManager.getInstance())
        workProcessor.submitWork(work).second.result.get()

        val workInfo = workProcessor.getWorkByName(work.getUniqueWorkName())
        assertThat(workInfo[0].state, `is`(WorkInfo.State.ENQUEUED))
    }

    @Test
    fun testSubmitDependentTasks() {
        val workRequestOne = TestWorkRequest1("test@test.com")
        val workRequestTwo = TestWorkRequest1("test@test.com")

        val workProcessor = WorkProcessor(WorkManager.getInstance())
        val operationOne = workProcessor.submitWork(workRequestOne)
        val operationTwo = workProcessor.submitWork(workRequestTwo)

        val workInfo = workProcessor.getWorkByName(workRequestOne.getUniqueWorkName())
        val workInfoOne = workInfo.first { it.id == operationOne.first }
        val workInfoTwo = workInfo.first { it.id == operationTwo.first }

        assertThat(workInfo.size, `is`(2))
        assertThat(workInfoOne.state, `is`(WorkInfo.State.ENQUEUED))
        assertThat(workInfoTwo.state, `is`(WorkInfo.State.BLOCKED))
    }

    @Test
    fun testSubmitIndependentTasks() {
        val workRequestOne = TestWorkRequest1("test@test.com")
        val workRequestTwo = TestWorkRequest1("test@mailchimp.com")

        val workProcessor = WorkProcessor(WorkManager.getInstance())
        workProcessor.submitWork(workRequestOne).second.result.get()
        workProcessor.submitWork(workRequestTwo).second.result.get()

        val workInfo = workProcessor.getWorkByName(workRequestOne.getUniqueWorkName())
        assertThat(workInfo.size, `is`(1))
        assertThat(workInfo[0].state, `is`(WorkInfo.State.ENQUEUED))

        val workInfoTwo = workProcessor.getWorkByName(workRequestTwo.getUniqueWorkName())
        assertThat(workInfoTwo.size, `is`(1))
        assertThat(workInfoTwo[0].state, `is`(WorkInfo.State.ENQUEUED))
    }

    @Test
    fun testSubmitSecondIndependentTaskThatMustHappenAfterFirst() {
        val userWorkRequest = TestWorkRequest1("test@test.com")
        val tagWorkRequest = TestWorkRequest2("test@test.com")

        val workProcessor = WorkProcessor(WorkManager.getInstance())
        val userWork = workProcessor.submitWork(userWorkRequest)
        val tagWork = workProcessor.submitWork(tagWorkRequest)

        userWork.second.result.get()
        tagWork.second.result.get()

        val workInfo = workProcessor.getWorkByName(userWorkRequest.getUniqueWorkName())
        assertThat(workInfo.size, `is`(2))

        val userWorkInfo = workInfo.first { info -> info.id == userWork.first }
        val tagWorkInfo = workInfo.first { info -> info.id == tagWork.first }

        assertThat(tagWorkInfo.state, `is`(WorkInfo.State.BLOCKED))
        assertThat(userWorkInfo.state, `is`(WorkInfo.State.ENQUEUED))
    }
}