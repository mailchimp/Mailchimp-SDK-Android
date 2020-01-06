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

import com.mailchimp.sdk.api.model.ApiContact
import com.mailchimp.sdk.api.model.Contact
import com.mailchimp.sdk.api.model.ContactStatus
import com.mailchimp.sdk.api.model.mergefields.Address
import com.mailchimp.sdk.api.model.mergefields.Country
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ContactTests {

    @Test
    fun testContactBuilderClonesProperly() {
        val contactOne =
            Contact.Builder("test@test.com")
                .addTag("Jabroni")
                .setMergeField("MergeFieldOne", "MergeFieldOneValue")
                .build()

        val contactTwo =
            Contact.Builder(contactOne)
                .build()

        assertEquals(contactOne, contactTwo)
    }

    @Test
    fun testContactToApiContact() {
        val address =
            Address.Builder("1234 Any St", "Atlanta", "GA")
                .setState("GA")
                .setCountry(Country.USA)
                .setAddressLineTwo("UNIT 23")
                .build()
        val contact =
            Contact.Builder("test@test.com")
                .setMarketingPermission("id_one", true)
                .setMergeField("FNAME", "Mahoney")
                .setMergeField("LNAME", "bologna")
                .setMergeField("ADDRESS", address)
                .setContactStatus(ContactStatus.SUBSCRIBED)
                .build()

        val apiContact = ApiContact.fromContact(contact)

        val apiAddress = apiContact.mergeFields!!["ADDRESS"]
        assertNotNull(apiAddress)
        assertEquals(address, apiAddress)

        val firstName = apiContact.mergeFields!!["FNAME"]
        assertNotNull(firstName)
        assertEquals("Mahoney", firstName)

        val lastName = apiContact.mergeFields!!["LNAME"]
        assertNotNull(lastName)
        assertEquals("bologna", lastName)

        assertEquals(apiContact.contactStatus, ContactStatus.SUBSCRIBED)

        assertEquals(apiContact.marketingPermissions!![0].id, "id_one")
        assertEquals(apiContact.marketingPermissions!![0].enabled, true)
        assertEquals(apiContact.emailAddress, "test@test.com")
    }
}