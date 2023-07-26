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

package com.mailchimp.sdkdemo.mockapi

import com.mailchimp.sdk.api.SdkWebService
import com.mailchimp.sdk.api.model.ApiContact
import com.mailchimp.sdk.api.model.ContactEvent
import com.mailchimp.sdk.api.model.ContactEventResponse
import com.mailchimp.sdk.api.model.UpdateContactResponse
import okhttp3.Request
import okio.Timeout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MockSdkWebService(private val audienceBackend: MockMailchimpAudienceBackend, private val mockGenericCallBackend: MockGenericCallBackend) : SdkWebService {

    companion object {
        const val ADD_CONTACT_EVENT_TAG = "add_contact_event"
    }

    override fun updateContact(contactRequest: ApiContact): Call<UpdateContactResponse> {
        return AudienceCall { audienceBackend.createOrUpdateContact(contactRequest) }
    }

    override fun addContactEvent(contactEvent: ContactEvent): Call<ContactEventResponse> {
        return AudienceCall {
            mockGenericCallBackend.addRequest(contactEvent, ADD_CONTACT_EVENT_TAG)
            ContactEventResponse()
        }
    }
}

class AudienceCall<T>(private val doWork: () -> T) : Call<T> {
    private var isCancelled = false
    private var isExecuted = false

    // Not run on background thread
    override fun enqueue(callback: Callback<T>) {
        isExecuted = true
        val response = Response.success(executeUpdate())
        callback.onResponse(this, response)
    }

    override fun isExecuted(): Boolean {
        return isExecuted
    }

    override fun clone(): Call<T> {
        return AudienceCall(doWork)
    }

    override fun isCanceled(): Boolean {
        return isCancelled
    }

    override fun cancel() {
        isCancelled = true
    }

    override fun execute(): Response<T> {
        isExecuted = true
        return Response.success(executeUpdate())
    }

    override fun request(): Request {
        return Request.Builder().build()
    }

    override fun timeout(): Timeout {
        return Timeout.NONE
    }

    private fun executeUpdate(): T {
        return doWork()
    }
}
