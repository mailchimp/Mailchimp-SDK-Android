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

import androidx.work.Data
import androidx.work.workDataOf
import com.google.gson.Gson
import com.mailchimp.sdk.api.model.Contact
import com.mailchimp.sdk.audience.AudienceWorker.Companion.KEY_INPUT_CONTACT
import com.mailchimp.sdk.core.work.SdkWorkRequest
import com.mailchimp.sdk.core.work.SdkWorker

class AudienceWorkRequest(private val contact: Contact, private val gson: Gson) : SdkWorkRequest() {

    override fun sdkWorkerClass(): Class<out SdkWorker> {
        return AudienceWorker::class.java
    }

    override fun getUniqueWorkName(): String {
        return "AudienceWorkRequest:${contact.emailAddress}"
    }

    override fun workParameters(): Data {
        val contactString = gson.toJson(contact)
        return workDataOf(KEY_INPUT_CONTACT to contactString)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AudienceWorkRequest

        if (contact != other.contact) return false

        return true
    }

    override fun hashCode(): Int {
        return contact.hashCode()
    }
}
