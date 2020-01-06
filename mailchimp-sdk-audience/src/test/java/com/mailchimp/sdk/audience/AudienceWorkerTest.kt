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

package com.mailchimp.sdk.audience

import android.app.Application
import androidx.work.Data
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.mailchimp.sdk.api.SdkWebService
import com.mailchimp.sdk.api.di.ApiDependencies
import com.mailchimp.sdk.api.model.Contact
import com.mailchimp.sdk.api.model.UpdateContactResponse
import com.mailchimp.sdk.audience.di.AudienceImplementation
import com.mailchimp.sdk.core.MailchimpSdkConfiguration
import com.mailchimp.sdk.core.di.CoreDependencies
import com.mailchimp.sdk.core.work.SdkWorker
import com.nhaarman.mockitokotlin2.*
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import retrofit2.Call
import retrofit2.Response

class AudienceWorkerTest {
    private val apiDependencies = mock<ApiDependencies>()
    private val coreDependencies = mock<CoreDependencies>()
    private val mailchimpConfiguration = mock<MailchimpSdkConfiguration>()
    lateinit var gson: Gson
    lateinit var webService: SdkWebService

    @Before
    fun setup() {
        gson = mock()
        whenever(apiDependencies.gson).thenReturn(gson)
        webService = mock()
        whenever(apiDependencies.sdkWebService).thenReturn(webService)
        AudienceImplementation.initialize(
            coreDependencies,
            apiDependencies,
            mailchimpConfiguration,
            true
        )
    }

    @Test
    fun performWorkTest() {
        val context = mock<Application>()
        val workParameter = mock<WorkerParameters>()

        val contact = Contact.Builder("test@jabroni.com").build()
        whenever(gson.fromJson(anyOrNull<String>(), eq(Contact::class.java))).thenReturn(contact)

        val call = mock<Call<UpdateContactResponse>>()
        whenever(webService.updateContact(any())).thenReturn(call)

        val inputData = mock<Data>()
        whenever(workParameter.inputData).thenReturn(inputData)

        val response = mock<Response<UpdateContactResponse>>()
        whenever(call.execute()).thenReturn(response)

        val audienceWorker = AudienceWorker(context, workParameter)

        whenever(response.isSuccessful).thenReturn(true)
        val actualResult = audienceWorker.performWork()

        assertEquals(SdkWorker.Response.SUCCESS, actualResult)
    }

    @Test
    fun performWorkTestFailedResponse() {
        val context = mock<Application>()
        val workParameter = mock<WorkerParameters>()

        val contact = Contact.Builder("test@jabroni.com").build()
        whenever(gson.fromJson(anyOrNull<String>(), eq(Contact::class.java))).thenReturn(contact)

        val call = mock<Call<UpdateContactResponse>>()
        whenever(webService.updateContact(any())).thenReturn(call)

        val inputData = mock<Data>()
        whenever(workParameter.inputData).thenReturn(inputData)

        val response = mock<Response<UpdateContactResponse>>()
        whenever(call.execute()).thenReturn(response)

        val audienceWorker = AudienceWorker(context, workParameter)

        whenever(response.isSuccessful).thenReturn(false)
        val actualResult = audienceWorker.performWork()

        assertEquals(SdkWorker.Response.RETRY, actualResult)
    }
}