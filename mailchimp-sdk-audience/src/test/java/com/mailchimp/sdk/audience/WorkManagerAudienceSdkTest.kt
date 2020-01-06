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

import android.app.Application
import android.content.res.Resources
import com.google.gson.Gson
import com.mailchimp.sdk.api.model.Contact
import com.mailchimp.sdk.api.model.ContactEvent
import com.mailchimp.sdk.api.model.Tag
import com.mailchimp.sdk.api.model.mergefields.Address
import com.mailchimp.sdk.audience.AudienceWorker.Companion.KEY_INPUT_CONTACT
import com.mailchimp.sdk.core.MailchimpSdkConfiguration
import com.mailchimp.sdk.core.work.WorkProcessor
import com.nhaarman.mockitokotlin2.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations.initMocks
import java.util.*

/**
 * Tests that the audience sdk work manager implementation generates the right work requests and submits it to
 * the work processor.
 *
 * We use argument captor instead of passing the work request in the whenever line (where any() currently is)
 * because it makes a cleaner assert failure message.
 */
class WorkManagerAudienceSdkTest {

    @Mock
    private lateinit var processor: WorkProcessor
    @Mock
    private lateinit var gson: Gson
    @Mock
    private lateinit var audienceContract: AudienceSdkContract
    @Mock
    private lateinit var audienceSdkConfiguration: MailchimpSdkConfiguration

    @Before
    fun setup() {
        initMocks(this)
    }

    @Test
    fun testCreateOrUpdateContract() {
        val sdk =
            WorkManagerAudienceSdk(processor, gson, audienceSdkConfiguration, audienceContract)

        whenever(processor.submitWork(any())).thenReturn(UUID.randomUUID() to mock())

        sdk.createOrUpdateContact(Contact.Builder("jabroni@test.com").build())

        argumentCaptor<AudienceWorkRequest>().apply {
            verify(processor).submitWork(capture())

            assertNotNull(firstValue)
            assertEquals("AudienceWorkRequest:jabroni@test.com", firstValue.getUniqueWorkName())
        }
    }

    @Test
    fun testAddTag() {
        val sdk =
            WorkManagerAudienceSdk(processor, gson, audienceSdkConfiguration, audienceContract)

        val emailAddress = "jabroni@test.com"
        val tagToAdd = "Jabroni"

        val expectedContact = Contact.Builder(emailAddress).addTag(tagToAdd)
        val expectedWorkRequest = AudienceWorkRequest(expectedContact.build(), gson)

        val generatedUUID = UUID.randomUUID()
        whenever(processor.submitWork(any())).thenReturn(generatedUUID to mock())

        val workId = sdk.addTag(emailAddress, tagToAdd)
        assertEquals(generatedUUID, workId)

        argumentCaptor<AudienceWorkRequest>().apply {
            verify(processor).submitWork(capture())
            assertEquals(expectedWorkRequest, firstValue)
        }
    }

    @Test
    fun testRemoveTag() {
        val sdk =
            WorkManagerAudienceSdk(processor, gson, audienceSdkConfiguration, audienceContract)

        val emailAddress = "jabroni@test.com"
        val tagToRemove = "Jabroni"

        val expectedContact = Contact.Builder(emailAddress).removeTag(tagToRemove)
        val expectedWorkRequest = AudienceWorkRequest(expectedContact.build(), gson)

        val generatedUUID = UUID.randomUUID()
        whenever(processor.submitWork(any())).thenReturn(generatedUUID to mock())

        val workId = sdk.removeTag(emailAddress, tagToRemove)
        assertEquals(generatedUUID, workId)

        argumentCaptor<AudienceWorkRequest>().apply {
            verify(processor).submitWork(capture())
            assertEquals(expectedWorkRequest, firstValue)
        }
    }

    @Test
    fun testAddTags() {
        val sdk =
            WorkManagerAudienceSdk(processor, gson, audienceSdkConfiguration, audienceContract)

        val emailAddress = "jabroni@test.com"
        val tagsToAdd = listOf("jabroni", "bologna", "mahoney", "zamboni")

        val expectedContact = Contact.Builder(emailAddress)
        tagsToAdd.forEach { expectedContact.addTag(it) }
        val expectedWorkRequest = AudienceWorkRequest(expectedContact.build(), gson)

        val generatedUUID = UUID.randomUUID()
        whenever(processor.submitWork(any())).thenReturn(generatedUUID to mock())

        val workId = sdk.addTags(emailAddress, tagsToAdd)
        assertEquals(generatedUUID, workId)

        argumentCaptor<AudienceWorkRequest>().apply {
            verify(processor).submitWork(capture())
            assertEquals(expectedWorkRequest, firstValue)
        }
    }

    @Test
    fun testRemoveTags() {
        val sdk =
            WorkManagerAudienceSdk(processor, gson, audienceSdkConfiguration, audienceContract)

        val emailAddress = "jabroni@test.com"
        val tagsToRemove = listOf("jabroni", "bologna", "mahoney", "zamboni")

        val expectedContact = Contact.Builder(emailAddress)
        tagsToRemove.forEach { expectedContact.removeTag(it) }

        val expectedWorkRequest = AudienceWorkRequest(expectedContact.build(), gson)

        val generatedUUID = UUID.randomUUID()

        whenever(processor.submitWork(any())).thenReturn(generatedUUID to mock())

        val workId = sdk.removeTags(emailAddress, tagsToRemove)
        assertEquals(generatedUUID, workId)

        argumentCaptor<AudienceWorkRequest>().apply {
            verify(processor).submitWork(capture())
            assertEquals(expectedWorkRequest, firstValue)
        }
    }

    @Test
    fun testAddMarketingPermission() {
        val sdk =
            WorkManagerAudienceSdk(processor, gson, audienceSdkConfiguration, audienceContract)

        val emailAddress = "jabroni@test.com"

        val expectedContact = Contact.Builder(emailAddress)
        expectedContact.setMarketingPermission("ateqw5rweafsdq345trewapfsd", true)

        val expectedWorkRequest = AudienceWorkRequest(expectedContact.build(), gson)
        val generatedUUID = UUID.randomUUID()

        whenever(processor.submitWork(any())).thenReturn(generatedUUID to mock())

        val workId = sdk.setMarketingPermission(emailAddress, "ateqw5rweafsdq345trewapfsd", true)
        assertEquals(generatedUUID, workId)

        argumentCaptor<AudienceWorkRequest>().apply {
            verify(processor).submitWork(capture())
            assertEquals(expectedWorkRequest, firstValue)
        }
    }

    @Test
    fun testSetStringMergeField() {
        val sdk =
            WorkManagerAudienceSdk(processor, gson, audienceSdkConfiguration, audienceContract)

        val emailAddress = "jabroni@test.com"
        val mergeFieldName = "Knickname"
        val mergeFieldStringValue = "Jabroni"

        val expectedContact =
            Contact.Builder(emailAddress).setMergeField(mergeFieldName, mergeFieldStringValue)
        val expectedWorkRequest = AudienceWorkRequest(expectedContact.build(), gson)

        val generatedUUID = UUID.randomUUID()
        whenever(processor.submitWork(any())).thenReturn(generatedUUID to mock())

        val workId = sdk.setMergeField(emailAddress, mergeFieldName, mergeFieldStringValue)
        assertEquals(generatedUUID, workId)

        argumentCaptor<AudienceWorkRequest>().apply {
            verify(processor).submitWork(capture())
            assertEquals(expectedWorkRequest, firstValue)
        }
    }

    @Test
    fun testSetAddressMergeField() {
        val sdk =
            WorkManagerAudienceSdk(processor, gson, audienceSdkConfiguration, audienceContract)

        val emailAddress = "jabroni@test.com"
        val mergeFieldName = "Knickname"
        val mergeFieldAddressValue: Address = mock()

        val expectedContact =
            Contact.Builder(emailAddress).setMergeField(mergeFieldName, mergeFieldAddressValue)
        val expectedWorkRequest = AudienceWorkRequest(expectedContact.build(), gson)

        val generatedUUID = UUID.randomUUID()
        whenever(processor.submitWork(any())).thenReturn(generatedUUID to mock())

        val workIdwithAddress =
            sdk.setMergeField(emailAddress, mergeFieldName, mergeFieldAddressValue)
        assertEquals(generatedUUID, workIdwithAddress)

        argumentCaptor<AudienceWorkRequest>().apply {
            verify(processor).submitWork(capture())
            assertEquals(expectedWorkRequest, firstValue)
        }
    }

    @Test
    fun testApplyTabletAutoTag() {
        val contact = Contact.Builder("joannesong@gmail.com").build()
        val mockApp = mock<Application>()
        val resource = mock<Resources>()
        val androidTag = Tag(Contact.ANDROID_TAG, Contact.ACTIVE_TAG_STATUS)
        val tabletTag = Tag(Contact.TABLET_TAG, Contact.ACTIVE_TAG_STATUS)
        val phoneTag = Tag(Contact.PHONE_TAG, Contact.ACTIVE_TAG_STATUS)
        val generatedUUID = UUID.randomUUID()

        whenever(mockApp.applicationContext).thenReturn(mockApp)

        audienceSdkConfiguration =
            MailchimpSdkConfiguration.Builder(mockApp, "sdkKey-us1").isAutoTaggingEnabled(true)
                .build()

        val sdk =
            WorkManagerAudienceSdk(processor, gson, audienceSdkConfiguration, audienceContract)

        whenever(processor.submitWork(any())).thenReturn(generatedUUID to mock())
        whenever(mockApp.resources).thenReturn(resource)
        whenever(resource.getBoolean(R.bool.isTablet)).thenReturn(true)

        sdk.createOrUpdateContact(contact)

        argumentCaptor<AudienceWorkRequest>().apply {
            verify(processor).submitWork(capture())

            assertNotNull(firstValue)
            firstValue.workParameters().getString(KEY_INPUT_CONTACT)
        }
        argumentCaptor<Contact>().apply {
            verify(gson).toJson(capture())

            assertTrue(firstValue.tags!!.contains(androidTag))
            assertTrue(firstValue.tags!!.contains(tabletTag))
            assertFalse(firstValue.tags!!.contains(phoneTag))
        }
    }

    @Test
    fun testApplyPhoneAutoTag() {
        val contact = Contact.Builder("joannesong@gmail.com").build()
        val mockApp = mock<Application>()
        val resource = mock<Resources>()
        val phoneTag = Tag(Contact.PHONE_TAG, Contact.ACTIVE_TAG_STATUS)
        val tabletTag = Tag(Contact.TABLET_TAG, Contact.ACTIVE_TAG_STATUS)
        val androidTag = Tag(Contact.ANDROID_TAG, Contact.ACTIVE_TAG_STATUS)
        val generatedUUID = UUID.randomUUID()

        whenever(mockApp.applicationContext).thenReturn(mockApp)

        audienceSdkConfiguration =
            MailchimpSdkConfiguration.Builder(mockApp, "sdkKey-us1").isAutoTaggingEnabled(true)
                .build()

        val sdk =
            WorkManagerAudienceSdk(processor, gson, audienceSdkConfiguration, audienceContract)

        whenever(processor.submitWork(any())).thenReturn(generatedUUID to mock())
        whenever(mockApp.resources).thenReturn(resource)
        whenever(resource.getBoolean(R.bool.isTablet)).thenReturn(false)

        sdk.createOrUpdateContact(contact)

        argumentCaptor<AudienceWorkRequest>().apply {
            verify(processor).submitWork(capture())

            assertNotNull(firstValue)
            firstValue.workParameters().getString(KEY_INPUT_CONTACT)
        }
        argumentCaptor<Contact>().apply {
            verify(gson).toJson(capture())

            assertTrue(firstValue.tags!!.contains(androidTag))
            assertTrue(firstValue.tags!!.contains(phoneTag))
            assertFalse(firstValue.tags!!.contains(tabletTag))
        }
    }

    @Test
    fun testEvent() {
        val testEmail = "test@email.com"
        val testEventName = "test_event"
        val testPropertyKey = "Valid_Key"
        val testPropertyValue = "Valid_Value"
        val generatedUUID = UUID.randomUUID()
        val mockApp = mock<Application>()
        whenever(mockApp.applicationContext).thenReturn(mockApp)
        whenever(processor.submitWork(any())).thenReturn(generatedUUID to mock())
        audienceSdkConfiguration =
            MailchimpSdkConfiguration.Builder(mockApp, "sdkKey-us1").isAutoTaggingEnabled(true)
                .build()

        val sdk =
            WorkManagerAudienceSdk(processor, gson, audienceSdkConfiguration, audienceContract)
        sdk.addContactEvent(
            testEmail,
            testEventName,
            mapOf(testPropertyKey to testPropertyValue)
        )

        argumentCaptor<EventWorkRequest>().apply {
            verify(processor).submitWork(capture())

            assertNotNull(firstValue)
            firstValue.workParameters().getString(KEY_INPUT_CONTACT)
        }
        argumentCaptor<ContactEvent>().apply {
            verify(gson).toJson(capture())
            assertEquals(testEmail, firstValue.emailAddress)
            assertEquals(testEventName, firstValue.eventName)
            assertEquals(testPropertyKey, firstValue.properties!!.keys.first())
            assertEquals(testPropertyValue, firstValue.properties!!.values.first())
        }
    }

    @Test
    fun testEventNameTooLong() {
        val testEmail = "irrelevant"
        val testEventName =
            "ThisEventNameIsLongerThanThirtyCharacters_klafdjklfdjklfdsjkfsdljkfldsjklfdsjklfdsjfsdkljfsdklj"
        val mockApp = mock<Application>()
        whenever(mockApp.applicationContext).thenReturn(mockApp)
        audienceSdkConfiguration =
            MailchimpSdkConfiguration.Builder(mockApp, "sdkKey-us1").isAutoTaggingEnabled(true)
                .build()

        val sdk =
            WorkManagerAudienceSdk(processor, gson, audienceSdkConfiguration, audienceContract)

        val result = sdk.addContactEvent(testEmail, testEventName)
        assertNull(result)
    }

    @Test
    fun testEventPropertiesContainsBadCharacters() {
        val testEmail = "irrelevant"
        val testEventName = "irrelevant"
        val testPropertyKey = "Key_With_Bad_Character_1"
        val testPropertyValue = "irrelevant"
        val mockApp = mock<Application>()
        whenever(mockApp.applicationContext).thenReturn(mockApp)
        audienceSdkConfiguration =
            MailchimpSdkConfiguration.Builder(mockApp, "sdkKey-us1").isAutoTaggingEnabled(true)
                .build()

        val sdk =
            WorkManagerAudienceSdk(processor, gson, audienceSdkConfiguration, audienceContract)

        val result = sdk.addContactEvent(
            testEmail,
            testEventName,
            mapOf(testPropertyKey to testPropertyValue)
        )
        assertNull(result)
    }
}