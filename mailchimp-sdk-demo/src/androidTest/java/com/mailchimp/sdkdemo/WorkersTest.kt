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
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.impl.utils.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import com.mailchimp.sdk.core.MailchimpSdkConfiguration
import com.mailchimp.sdk.main.Mailchimp
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WorkersTest {

    private lateinit var workManager: WorkManager

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val fakeSdkKey = "irrelevant-us1"

        val config = MailchimpSdkConfiguration.Builder(context.applicationContext, fakeSdkKey).build()

        Mailchimp.initialize(config)

        val configuration =
            Configuration.Builder()
                .setMinimumLoggingLevel(Log.DEBUG)
                .setExecutor(SynchronousExecutor())
                .build()

        WorkManagerTestInitHelper.initializeTestWorkManager(context, configuration)
        workManager = WorkManager.getInstance(context)
    }

    @Test
    fun testCreateOrUpdateUser() {
        val createOrUpdateRequest =
            OneTimeWorkRequestBuilder<TestWorker>()
                .build()

        workManager.enqueue(createOrUpdateRequest).result.get()

        val workInfo = workManager.getWorkInfoById(createOrUpdateRequest.id).get()
        assertThat(workInfo.state, `is`(WorkInfo.State.SUCCEEDED))
    }
}