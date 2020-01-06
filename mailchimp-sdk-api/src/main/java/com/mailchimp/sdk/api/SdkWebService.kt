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

package com.mailchimp.sdk.api

import com.mailchimp.sdk.api.model.ApiContact
import com.mailchimp.sdk.api.model.ContactEvent
import com.mailchimp.sdk.api.model.ContactEventResponse
import com.mailchimp.sdk.api.model.UpdateContactResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface SdkWebService {

    @POST("contacts")
    fun updateContact(@Body contactRequest: ApiContact): Call<UpdateContactResponse>

    @POST("contacts/events")
    fun addContactEvent(@Body contactEvent: ContactEvent): Call<ContactEventResponse>
}