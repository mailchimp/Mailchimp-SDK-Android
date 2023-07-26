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

package com.mailchimp.sdkdemo

import android.content.Context
import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.Configuration
import androidx.work.WorkManager
import androidx.work.impl.utils.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import com.mailchimp.sdk.api.di.ApiImplementation
import com.mailchimp.sdk.api.model.ApiContact
import com.mailchimp.sdk.api.model.Contact
import com.mailchimp.sdk.api.model.ContactEvent
import com.mailchimp.sdk.api.model.ContactStatus
import com.mailchimp.sdk.api.model.mergefields.Address
import com.mailchimp.sdk.api.model.mergefields.Country
import com.mailchimp.sdk.api.model.mergefields.StringMergeFieldValue
import com.mailchimp.sdk.audience.di.AudienceDependencies
import com.mailchimp.sdk.audience.di.AudienceImplementation
import com.mailchimp.sdk.core.MailchimpSdkConfiguration
import com.mailchimp.sdk.core.work.WorkStatus
import com.mailchimp.sdk.main.Mailchimp
import com.mailchimp.sdk.main.di.MailchimpInjector
import com.mailchimp.sdkdemo.mockapi.MockApiImplementation
import com.mailchimp.sdkdemo.mockapi.MockGenericCallBackend
import com.mailchimp.sdkdemo.mockapi.MockMailchimp
import com.mailchimp.sdkdemo.mockapi.MockMailchimpAudienceBackend
import com.mailchimp.sdkdemo.mockapi.MockSdkWebService
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.UUID

class PublicAudienceSdkIntegrationTest {

    private lateinit var mailchimp: Mailchimp
    private lateinit var mockAudienceBackend: MockMailchimpAudienceBackend
    private lateinit var mockGenericCallBackend: MockGenericCallBackend
    private lateinit var workManager: WorkManager
    private lateinit var context: Context

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        setupWorkManager(context)
        setupMockAudienceSdk(context)
    }

    private fun setupWorkManager(context: Context) {
        val config =
            Configuration.Builder()
                .setMinimumLoggingLevel(Log.DEBUG)
                .setExecutor(SynchronousExecutor())
                .build()
        WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
        workManager = WorkManager.getInstance(context)
        workManager.pruneWork()
    }

    private fun setupMockAudienceSdk(context: Context, autotagging: Boolean = false, debugMode: Boolean = false) {
        val sdkKey = "FakeSdkKey-us1"
        val configuration =
            MailchimpSdkConfiguration.Builder(context.applicationContext, sdkKey)
                .isAutoTaggingEnabled(autotagging)
                .isDebugModeEnabled(debugMode)
                .build()
        val mockApiImplementation = MockApiImplementation()
        mockAudienceBackend = mockApiImplementation.audienceBackend
        mockGenericCallBackend = mockApiImplementation.mockGenericCallBackend
        val mockInjector = object : MailchimpInjector(configuration) {
            override val apiDependencies: ApiImplementation = mockApiImplementation
            override val audienceDependencies: AudienceDependencies by lazy {
                AudienceImplementation.initialize(coreDependencies, apiDependencies, configuration, override = true)
            }
        }
        val mockAudienceSDK = MockMailchimp(mockInjector)
        MockMailchimp.setAudienceAsMock(mockAudienceSDK)
        mockAudienceSDK.initializeMock()
        mailchimp = mockAudienceSDK
    }

    @Test
    fun testSettingContactStatus() {
        val email = "test@email.com"
        val testStatus = ContactStatus.SUBSCRIBED
        val testContact = Contact.Builder(email).setContactStatus(testStatus).build()

        val id = mailchimp.createOrUpdateContact(testContact)
        ensureComplete(id)

        val contact = mockAudienceBackend.getLastCall(email)
        assertWorkInFinishedState(id)
        assertNotNull(contact)
        assertContactEqualsApi(testContact, contact!!)
        assertEquals(email, contact.emailAddress)
        assertEquals(testStatus, contact.contactStatus)
    }

    @Test
    fun testSettingContactStatusNull() {
        val email = "test@email.com"
        val testStatus: ContactStatus? = null
        val testContact = Contact.Builder(email).setContactStatus(testStatus).build()

        val id = mailchimp.createOrUpdateContact(testContact)
        ensureComplete(id)

        val contact = mockAudienceBackend.getLastCall(email)
        assertWorkInFinishedState(id)
        assertNotNull(contact)
        assertContactEqualsApi(testContact, contact!!)
        assertEquals(email, contact.emailAddress)
        assertEquals(testStatus, contact.contactStatus)
    }

    @Test
    fun testAddingTag() {
        val email = "test@email.com"
        val tag1 = "TAG1"

        val id = mailchimp.addTag(email, tag1)
        ensureComplete(id)

        val contact = mockAudienceBackend.getLastCall(email)
        assertWorkInFinishedState(id)
        assertNotNull(contact)
        assertEquals(email, contact!!.emailAddress)
        assertContactContainsTag(contact, tag1, true)
    }

    @Test
    fun testAddingTags() {
        val email = "test@email.com"
        val tag1 = "TAG1"
        val tag2 = "TAG2"

        val id = mailchimp.addTags(email, listOf(tag1, tag2))
        ensureComplete(id)

        val contact = mockAudienceBackend.getLastCall(email)
        assertWorkInFinishedState(id)
        assertNotNull(contact)
        assertEquals(email, contact!!.emailAddress)
        assertContactContainsTag(contact, tag1, true)
        assertContactContainsTag(contact, tag2, true)
    }

    @Test
    fun testRemovingTag() {
        val email = "test@email.com"
        val tag1 = "TAG1"

        val id = mailchimp.removeTag(email, tag1)
        ensureComplete(id)

        val contact = mockAudienceBackend.getLastCall(email)
        assertWorkInFinishedState(id)
        assertNotNull(contact)
        assertEquals(email, contact!!.emailAddress)
        assertContactContainsTag(contact, tag1, false)
    }

    @Test
    fun testRemovingTags() {
        val email = "test@email.com"
        val tag1 = "TAG1"
        val tag2 = "TAG2"

        val id = mailchimp.removeTags(email, listOf(tag1, tag2))
        ensureComplete(id)

        val contact = mockAudienceBackend.getLastCall(email)
        assertWorkInFinishedState(id)
        assertNotNull(contact)
        assertEquals(email, contact!!.emailAddress)
        assertContactContainsTag(contact, tag1, false)
        assertContactContainsTag(contact, tag2, false)
    }

    @Test
    fun testSetMarketingPermission() {
        val email = "test@email.com"

        val id = mailchimp.setMarketingPermission(email, "consent_to_email", true)
        ensureComplete(id)

        val contact = mockAudienceBackend.getLastCall(email)
        assertWorkInFinishedState(id)
        assertNotNull(contact)
        assertEquals(email, contact!!.emailAddress)
        assertContactContainsPermission(contact, "consent_to_email", true)
    }

    @Test
    fun testSetMergeFieldString() {
        val email = "test@email.com"
        val mergeFieldName = "MERGE_FIELD_NAME"
        val mergeFieldValue = "MERGE_FIELD_VALUE"

        val id = mailchimp.setMergeField(email, mergeFieldName, mergeFieldValue)
        ensureComplete(id)

        val contact = mockAudienceBackend.getLastCall(email)
        assertWorkInFinishedState(id)
        assertNotNull(contact)
        assertEquals(email, contact!!.emailAddress)
        assertContactMergeField(contact, mergeFieldName, mergeFieldValue)
    }

    @Test
    fun testSetMergeFieldAddress() {
        val email = "test@email.com"
        val mergeFieldName = "MERGE_FIELD_NAME"
        val addressLine1 = "Address 1"
        val addressLine2 = "Address 2"
        val city = "ATL"
        val state = "GA"
        val country = Country.USA
        val zip = "30308"
        val testAddress =
            Address.Builder(addressLine1, city, zip)
                .setAddressLineTwo(addressLine2)
                .setState(state)
                .setCountry(country)
                .build()

        val id = mailchimp.setMergeField(email, mergeFieldName, testAddress)
        ensureComplete(id)

        val contact = mockAudienceBackend.getLastCall(email)
        assertWorkInFinishedState(id)
        assertNotNull(contact)
        assertEquals(email, contact!!.emailAddress)

        val actualAddress = getAddressFromContact(contact, mergeFieldName)!!
        assertNotNull(actualAddress)
        assertEquals(addressLine1, actualAddress.addressLineOne)
        assertEquals(addressLine2, actualAddress.addressLineTwo)
        assertEquals(city, actualAddress.city)
        assertEquals(state, actualAddress.state)
        assertEquals(country, actualAddress.country)
        assertEquals(zip, actualAddress.zip)
    }

    @Test
    fun testCreatingContact() {
        val email = "test@email.com"
        val testContact = Contact.Builder(email).build()

        val id = mailchimp.createOrUpdateContact(testContact)
        ensureComplete(id)

        val contact = mockAudienceBackend.getLastCall(email)
        assertWorkInFinishedState(id)
        assertNotNull(contact)
        assertEquals(email, contact!!.emailAddress)
    }

    @Test
    fun testAutoTagging() {
        // We left autotagging off on the other tests because of potential headaches. So for this test we cheat and
        // turn it on.
        setupMockAudienceSdk(context, autotagging = true)
        val email = "test@email.com"
        val testContact = Contact.Builder(email).build()

        val id = mailchimp.createOrUpdateContact(testContact)
        ensureComplete(id)

        val contact = mockAudienceBackend.getLastCall(email)
        assertWorkInFinishedState(id)
        assertNotNull(contact)
        assertEquals(email, contact!!.emailAddress)
        assertContactContainsTag(contact, Contact.ANDROID_TAG, true)
        if (isTablet()) {
            assertContactContainsTag(contact, Contact.TABLET_TAG, true)
        } else {
            assertContactContainsTag(contact, Contact.PHONE_TAG, true)
        }
    }

    @Test
    fun testOverrideMergeField() {
        val email = "test@email.com"
        val mergeFieldName = "MERGE_FIELD"
        val mergeFieldVal1 = "VAL1"
        val mergeFieldVal2 = "VAL2"

        val testContact =
            Contact.Builder(email)
                .setMergeField(mergeFieldName, mergeFieldVal1)
                .setMergeField(mergeFieldName, mergeFieldVal2)
                .build()
        val id = mailchimp.createOrUpdateContact(testContact)
        ensureComplete(id)

        assertWorkInFinishedState(id)
        val contact = mockAudienceBackend.getContact(email)
        assertNotNull(contact)
        assertEquals(email, contact!!.emailAddress)
        assertContactMergeField(contact, mergeFieldName, mergeFieldVal2)
    }

    @Test
    fun testOverrideTag() {
        val email = "test@email.com"
        val tag = "TAG"

        val testContact =
            Contact.Builder(email)
                .addTag(tag)
                .removeTag(tag)
                .build()
        val id = mailchimp.createOrUpdateContact(testContact)
        ensureComplete(id)

        assertWorkInFinishedState(id)
        val contact = mockAudienceBackend.getContact(email)
        assertNotNull(contact)
        assertEquals(email, contact!!.emailAddress)
        assertContactContainsTag(contact, tag, false)
    }

    @Test
    fun testCreatingComplexContact() {
        val email = "test@email.com"
        val tag1 = "Tag1"
        val tag2 = "Tag2"
        val mergeFieldTag1 = "MERGE_TAG_1"
        val mergeFieldValue1 = "MERGE_VALUE_1"
        val mergeFieldTag2 = "MERGE_TAG_2"
        val mergeFieldValue2 = "MERGE_VALUE_2"

        val addressLine1 = "Address 1"
        val addressLine2 = "Address 2"
        val city = "ATL"
        val state = "GA"
        val country = Country.USA
        val zip = "30308"
        val addressMergeTag = "Address_Merge_Tag"
        val testAddress =
            Address.Builder(addressLine1, city, zip)
                .setAddressLineTwo(addressLine2)
                .setState(state)
                .setCountry(country)
                .build()

        val testContact =
            Contact.Builder(email)
                .addTag(tag1)
                .removeTag(tag2)
                .setMergeField(mergeFieldTag1, mergeFieldValue1)
                .setMergeField(mergeFieldTag2, mergeFieldValue2)
                .setMergeField(addressMergeTag, testAddress)
                .build()

        val id = mailchimp.createOrUpdateContact(testContact)
        ensureComplete(id)

        val actualContact = mockAudienceBackend.getContact(email)
        assertWorkInFinishedState(id)
        assertNotNull(actualContact)
        assertContactEqualsApi(testContact, actualContact!!)
        assertEquals(email, actualContact.emailAddress)

        assertContactContainsTag(actualContact, tag1, true)
        assertContactContainsTag(actualContact, tag2, false)

        assertContactMergeField(actualContact, mergeFieldTag1, mergeFieldValue1)
        assertContactMergeField(actualContact, mergeFieldTag2, mergeFieldValue2)

        val actualAddress = getAddressFromContact(actualContact, addressMergeTag)!!
        assertNotNull(actualAddress)
        assertEquals(addressLine1, actualAddress.addressLineOne)
        assertEquals(addressLine2, actualAddress.addressLineTwo)
        assertEquals(city, actualAddress.city)
        assertEquals(state, actualAddress.state)
        assertEquals(country, actualAddress.country)
        assertEquals(zip, actualAddress.zip)
    }

    @Test
    fun testSendingEvent() {
        val email = "test@email.com"
        val event = "test_event"

        val id = mailchimp.addContactEvent(email, event)
        ensureComplete(id!!)

        val actualEventRequest =
            mockGenericCallBackend.getRequests(MockSdkWebService.ADD_CONTACT_EVENT_TAG).last() as ContactEvent
        assertEquals(event, actualEventRequest.eventName)
        assertEquals(email, actualEventRequest.emailAddress)
        assertNull(actualEventRequest.properties)
    }

    @Test
    fun testSendingEventWithProperties() {
        val email = "test@email.com"
        val event = "test_event"
        val properties = mapOf("test_prop" to "val1", "test_prop" to "val2")

        val id = mailchimp.addContactEvent(email, event, properties)
        ensureComplete(id!!)

        val actualEventRequest =
            mockGenericCallBackend.getRequests(MockSdkWebService.ADD_CONTACT_EVENT_TAG).last() as ContactEvent

        assertEquals(event, actualEventRequest.eventName)
        assertEquals(email, actualEventRequest.emailAddress)
        assertEquals(properties, actualEventRequest.properties)
    }

    private fun ensureComplete(id: UUID) {
        try {
            WorkManagerTestInitHelper.getTestDriver(context)?.setAllConstraintsMet(id)
        } catch (e: Exception) {
            // ignored
        }
    }

    private fun isTablet(): Boolean {
        return context.resources.getBoolean(com.mailchimp.mailchimp_sdk_core.R.bool.isTablet)
    }

    private fun getAddressFromContact(contact: ApiContact, mergeFieldName: String): Address? {
        return contact.mergeFields?.get(mergeFieldName) as? Address
    }

    private fun assertContactContainsTag(contact: ApiContact, tag: String, active: Boolean) {
        val tagMap = contact.tags?.associateBy { it.name }
        assertTrue(tagMap?.contains(tag) ?: false)
        val status = if (active) Contact.ACTIVE_TAG_STATUS else Contact.INACTIVE_TAG_STATUS
        assertEquals(status, tagMap?.get(tag)?.status)
    }

    private fun assertContactMergeField(contact: ApiContact, mergeFieldName: String, expectedValue: String) {
        val mergeFieldMap = contact.mergeFields
        assertTrue(mergeFieldMap?.contains(mergeFieldName) ?: false)
        val value = (mergeFieldMap?.get(mergeFieldName) as String)
        assertEquals(expectedValue, value)
    }

    private fun assertWorkInFinishedState(id: UUID) {
        val status = mailchimp.getStatusById(id)
        MatcherAssert.assertThat(WorkStatus.FINISHED, `is`(status))
    }

    private fun assertContactContainsPermission(contact: ApiContact, id: String, enabled: Boolean) {
        val permissionMap = contact.marketingPermissions?.associateBy { it.id }
        assertTrue(permissionMap?.contains(id) ?: false)
        assertEquals(enabled, permissionMap?.get(id)?.enabled)
    }

    private fun assertContactEqualsApi(contact: Contact, apiContact: ApiContact) {
        assertEquals(contact.emailAddress, apiContact.emailAddress)
        assertEquals(contact.contactStatus, apiContact.contactStatus)
        assertEquals(contact.marketingPermissions, apiContact.marketingPermissions)
        assertEquals(contact.tags, apiContact.tags)
        if (contact.mergeFields?.isNotEmpty() == true) {
            for (mergeField in contact.mergeFields!!) {
                if (mergeField.value is StringMergeFieldValue) {
                    assertContactMergeField(
                        apiContact,
                        mergeField.key,
                        (mergeField.value as StringMergeFieldValue).value
                    )
                } else if (mergeField.value is Address) {
                    assertEquals(mergeField.value, apiContact.mergeFields!![mergeField.key] as Address)
                }
            }
        }
    }
}
