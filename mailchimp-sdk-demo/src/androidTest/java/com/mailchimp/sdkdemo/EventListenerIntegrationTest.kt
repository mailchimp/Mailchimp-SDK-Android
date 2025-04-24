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
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.WorkManagerTestInitHelper
import com.mailchimp.sdk.api.SslHelper
import com.mailchimp.sdk.api.di.ApiDependencies
import com.mailchimp.sdk.api.model.Contact
import com.mailchimp.sdk.api.model.ContactStatus
import com.mailchimp.sdk.audience.di.AudienceDependencies
import com.mailchimp.sdk.audience.di.AudienceImplementation
import com.mailchimp.sdk.core.MailchimpSdkConfiguration
import com.mailchimp.sdk.main.Mailchimp
import com.mailchimp.sdk.main.di.MailchimpInjector
import com.mailchimp.sdkdemo.mockapi.MockLocalApiImplementation
import com.mailchimp.sdkdemo.mockapi.MockMailchimp
import io.mockk.mockk
import io.mockk.verify
import okhttp3.Call
import okhttp3.EventListener
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test


class EventListenerIntegrationTest {

    private lateinit var workManager: WorkManager
    private lateinit var mailchimp: Mailchimp
    private lateinit var context: Context

    private val mockWebServer = MockWebServer()

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        TestUtils.setupWorkManager(context)
        workManager = WorkManager.getInstance(context)
        mockWebServer.useHttps(SslHelper.serverSslSocketFactory, false)
        mockWebServer.start()
    }

    private fun setupMockLocalAudienceSdk(eventListener: EventListener, responseJson: String) {
        mockWebServer.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                return MockResponse().setResponseCode(200).setBody(responseJson)
            }
        }
        val configuration =
            MailchimpSdkConfiguration.Builder(context.applicationContext, "sdkKey-us1")
                .build()
        val mockLocalApiImplementation =
            MockLocalApiImplementation(mockWebServer.url("/") .toString(), eventListener)
        val mockInjector = object : MailchimpInjector(context, configuration) {
            override val apiDependencies: ApiDependencies = mockLocalApiImplementation
            override val audienceDependencies: AudienceDependencies by lazy {
                AudienceImplementation.initialize(coreDependencies, apiDependencies, configuration, override = true)
            }
        }
        val mockAudienceSDK = MockMailchimp(mockInjector)
        MockMailchimp.setAudienceAsMock(mockAudienceSDK)
        mockAudienceSDK.initializeMock()
        mailchimp = mockAudienceSDK
    }

    @Test
    fun testListenToConnectStart() {
        val callback = mockk<() -> Unit>(relaxed = true)
        val eventListener = object : EventListener() {
            override fun secureConnectStart(call: Call) {
                callback()
            }
        }
        setupMockLocalAudienceSdk(eventListener, "{ \"emailAddress\": \"a@b.c\" }")
        val email = "test@email.com"
        val testStatus = ContactStatus.SUBSCRIBED
        val testContact = Contact.Builder(email).setContactStatus(testStatus).build()
        val uuid = mailchimp.createOrUpdateContact(testContact)
        WorkManagerTestInitHelper.getTestDriver(context)!!.setAllConstraintsMet(uuid)
        val result = workManager.getWorkInfoById(uuid).get()
        assertNotNull(result)
        assert(result!!.state == WorkInfo.State.SUCCEEDED)
        verify { callback() }
    }

    @After
    fun cleanUp() {
        mockWebServer.shutdown()
    }
}