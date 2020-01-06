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

package com.mailchimp.sdk.api.model

import com.google.gson.annotations.SerializedName

data class ContactEvent(
    @SerializedName("email_address")
    val emailAddress: String,
    @SerializedName("event_name")
    val eventName: String,
    val properties: Map<String, String>?
) {
    companion object {
        const val MAX_EVENT_NAME_LENGTH = 30
        const val PROPERTIES_KEY_REGEX_PATTERN = "^[a-zA-Z_]*\$"
    }
}

class ContactEventResponse
