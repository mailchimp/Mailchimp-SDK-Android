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

import android.content.Context
import androidx.work.WorkerParameters
import com.mailchimp.sdk.api.model.ContactEvent
import com.mailchimp.sdk.audience.di.AudienceImplementation
import com.mailchimp.sdk.core.work.SdkWorker
import timber.log.Timber

class EventWorker(appContext: Context, workParams: WorkerParameters) :
    SdkWorker(appContext, workParams) {
    override val maxRetries: Int = 5
    private val gson = AudienceImplementation.sharedInstance()!!.gson
    private val webService = AudienceImplementation.sharedInstance()!!.sdkWebService

    companion object {
        const val KEY_INPUT_EVENT_REQUEST = "key_input_event_request"
    }

    override fun performWork(): Response {
        val eventRequest = gson.fromJson(
            inputData.getString(KEY_INPUT_EVENT_REQUEST),
            ContactEvent::class.java
        )
        val call = webService.addContactEvent(eventRequest)
        val response = call.execute()
        return if (response.isSuccessful) {
            Timber.d(
                "Successfully sent event %s for %s",
                eventRequest.eventName,
                eventRequest.emailAddress
            )
            Response.SUCCESS
        } else {
            Timber.d(
                "Failed in sending even %s for %s",
                eventRequest.eventName,
                eventRequest.emailAddress
            )
            Response.RETRY
        }
    }
}