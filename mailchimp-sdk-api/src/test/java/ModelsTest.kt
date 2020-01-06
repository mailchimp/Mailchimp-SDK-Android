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

import com.mailchimp.sdk.api.model.Contact
import com.mailchimp.sdk.api.model.mergefields.Address
import com.mailchimp.sdk.api.model.mergefields.Country
import org.junit.Assert.assertEquals
import org.junit.Test

class ModelsTest {

    @Test
    fun testContactAndAddressBuilder() {
        val address =
            Address.Builder("1234 Any St", "Atlanta", "30319")
                .setAddressLineTwo("C/O Test Tester")
                .setState("GA")
                .setCountry(Country.USA)
                .build()

        val contact =
            Contact.Builder("test@test.com")
                .setMergeField("custom", "merge")
                .setMergeField("custom_address", address)
                .build()

        assertEquals("test@test.com", contact.emailAddress)

        val actualAddress = contact.mergeFields!!.first { it.key == "custom_address" }.value as Address

        assertEquals("1234 Any St", actualAddress.addressLineOne)
        assertEquals("C/O Test Tester", actualAddress.addressLineTwo)
        assertEquals("Atlanta", actualAddress.city)
        assertEquals("GA", actualAddress.state)
        assertEquals("30319", actualAddress.zip)
        assertEquals(Country.USA, actualAddress.country)
    }
}