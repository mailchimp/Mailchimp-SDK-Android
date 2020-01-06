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
import com.mailchimp.sdk.api.model.ApiContact
import com.mailchimp.sdk.api.model.Contact
import com.mailchimp.sdk.audience.di.AudienceImplementation
import com.mailchimp.sdk.core.work.SdkWorker
import timber.log.Timber

class AudienceWorker(appContext: Context, workParams: WorkerParameters) : SdkWorker(appContext, workParams) {
    private val gson = AudienceImplementation.sharedInstance()!!.gson
    private val webService = AudienceImplementation.sharedInstance()!!.sdkWebService

    companion object {
        const val KEY_INPUT_CONTACT = "contact"
        /*
         * We will not retry the request if we receive the following error codes.
         *  400: Malformed request.
         *  401: Authorization issue.
         */
        private val permanentFailureCodes = listOf(400, 401)
    }

    override fun performWork(): Response {
        val contact = gson.fromJson(inputData.getString(KEY_INPUT_CONTACT), Contact::class.java)
        val call = webService.updateContact(ApiContact.fromContact(contact))
        val response = call.execute()
        return if (response.isSuccessful) {
            Timber.d("Creating or updating user was successful for %s", contact.emailAddress)
            Response.SUCCESS
        } else if (permanentFailureCodes.contains(response.code())) {
            Timber.d("Creating or updating user failed for %s, will not retry", contact.emailAddress)
            Response.FAILURE_CONTINUE_CHAIN
        } else {
            Timber.d("Creating or updating user failed for %s, will retry if not attempt %s", contact.emailAddress, maxRetries)
            Response.RETRY
        }
    }
}
