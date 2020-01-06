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
import androidx.work.WorkManager
import com.google.gson.Gson
import com.mailchimp.sdk.api.di.ApiDependencies
import com.mailchimp.sdk.core.MailchimpSdkConfiguration
import com.mailchimp.sdk.core.di.CoreDependencies
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertNotNull
import org.junit.Test

class MailchimpInitializeTest {

    @Test
    fun testMailchimpSdkWithMocks() {
        val mockApp = mock<Application>()
        val mockCoreDependencies = mock<CoreDependencies>()
        val mockApiDependencies = mock<ApiDependencies>()
        val gson = Gson()

        whenever(mockApiDependencies.gson).thenReturn(gson)
        whenever(mockApiDependencies.sdkWebService).thenReturn(mock())
        whenever(mockCoreDependencies.workProcessor).thenReturn(mock())
        whenever(mockCoreDependencies.workStatusProvider).thenReturn(mock())
        whenever(mockApp.applicationContext).thenReturn(mockApp)

        val sdkConfiguration = MailchimpSdkConfiguration.Builder(mockApp, "sdkKey-us1").build()
        val mock = MailchimpMockInjector(mockCoreDependencies, mockApiDependencies, sdkConfiguration)

        val mockSdk = MailchimpMock(mock)
        MailchimpMock.setMock(mockSdk)

        assertNotNull(Mailchimp.sharedInstance())
    }
}