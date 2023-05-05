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

package com.mailchimp.sdk.main

import android.app.Application
import android.content.Context
import com.mailchimp.sdk.core.MailchimpSdkConfiguration
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class MailchimpSdkBuilderTest {

    private val appContext: Context = mock<Application>()
    private val context = mock<Context>()

    @Test
    fun mailchimpSdkBuilderTest() {
        val sdk = "mysdk"
        whenever(context.applicationContext).thenReturn(appContext)
        val builder = MailchimpSdkConfiguration.Builder(context, sdk)

        try {
            builder.build()
        } catch (e: IllegalArgumentException) {
            assertEquals("The provided SDK key is invalid. It should contain 1 dash.", e.message)
            return
        }
        fail("We expected an illegalStateException, but did not receive one.")
    }

    @Test
    fun mailchimpSdkBuilderHasCorrectContextTest() {
        val sdk = "mysdk-us1"
        whenever(context.applicationContext).thenReturn(appContext)
        val builder = MailchimpSdkConfiguration.Builder(context, sdk)

        val myDebugMode = false
        val myAutoTagEnabled = true

        val config = builder.build()

        assertEquals("mysdk", config.sdkKey)
        assertEquals("us1", config.shard)
        assertEquals(appContext, config.context)
        assertEquals(myDebugMode, config.debugModeEnabled)
        assertEquals(myAutoTagEnabled, config.autoTaggingEnabled)
    }
}