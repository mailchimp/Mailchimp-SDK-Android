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

import android.content.Context
import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.Configuration
import androidx.work.impl.utils.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import com.mailchimp.sdk.core.MailchimpSdkConfiguration
import com.mailchimp.sdk.main.Mailchimp
import org.junit.Before
import org.junit.Test

class AudienceSdkInitializeTests {

    private var context: Context = InstrumentationRegistry.getInstrumentation().targetContext

    @Before
    fun setup() {
        val configuration =
            Configuration.Builder()
                .setMinimumLoggingLevel(Log.DEBUG)
                .setExecutor(SynchronousExecutor())
                .build()

        WorkManagerTestInitHelper.initializeTestWorkManager(context, configuration)
    }

    @Test
    fun testInitializeWithConfiguration() {
        val config =
            MailchimpSdkConfiguration.Builder(context.applicationContext, "I_AM_SDK_KEY_TRUST_ME-us1")
                .isDebugModeEnabled(true)
                .isAutoTaggingEnabled(true)
                .build()

        Mailchimp.initialize(config)
    }
}