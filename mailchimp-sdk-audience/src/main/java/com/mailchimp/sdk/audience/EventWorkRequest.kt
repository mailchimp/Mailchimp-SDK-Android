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

import androidx.work.BackoffPolicy
import androidx.work.Data
import androidx.work.workDataOf
import com.google.gson.Gson
import com.mailchimp.sdk.api.model.ContactEvent
import com.mailchimp.sdk.core.work.SdkWorkRequest
import com.mailchimp.sdk.core.work.SdkWorker

class EventWorkRequest(private val event: ContactEvent, private val gson: Gson) :
    SdkWorkRequest() {

    // We want to send events immediately
    override val requireInternet: Boolean = false
    override val backoffPolicy = BackoffPolicy.LINEAR

    override fun sdkWorkerClass(): Class<out SdkWorker> {
        return EventWorker::class.java
    }

    override fun getUniqueWorkName(): String {
        return "EventWorkRequest:${event.emailAddress}"
    }

    override fun workParameters(): Data {
        val contactString = gson.toJson(event)
        return workDataOf(EventWorker.KEY_INPUT_EVENT_REQUEST to contactString)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EventWorkRequest

        if (event != other.event) return false

        return true
    }

    override fun hashCode(): Int {
        return event.hashCode()
    }
}