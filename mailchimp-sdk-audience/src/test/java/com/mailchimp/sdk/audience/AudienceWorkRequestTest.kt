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

import com.google.gson.Gson
import com.mailchimp.sdk.api.model.Contact
import com.mailchimp.sdk.audience.AudienceWorker.Companion.KEY_INPUT_CONTACT
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class AudienceWorkRequestTest {

    @Test
    fun testSdkWorkerClass() {
        val contact = Contact.Builder("test@test.com").build()
        val mockGson = mock<Gson>()
        val workRequest = AudienceWorkRequest(contact, mockGson)

        assertEquals("com.mailchimp.sdk.audience.AudienceWorker", workRequest.sdkWorkerClass().canonicalName)
    }

    @Test
    fun testCreateWorkRequestWithContact() {
        val contact = Contact.Builder("test@test.com").build()
        val mockGson = mock<Gson>()
        val json = "I_AM_JSON!"

        whenever(mockGson.toJson(contact)).thenReturn(json)

        val workRequest = AudienceWorkRequest(contact, mockGson)

        val data = workRequest.workParameters()

        assertEquals("AudienceWorkRequest:test@test.com", workRequest.getUniqueWorkName())

        val contactData = data.getString(KEY_INPUT_CONTACT)
        assertEquals(json, contactData)
    }

    @Test
    fun testHashCode() {
        val contact = Contact.Builder("test@test.com").build()
        val mockGson = mock<Gson>()
        val workRequest = AudienceWorkRequest(contact, mockGson)

        assertEquals(contact.hashCode(), workRequest.hashCode())
    }
}