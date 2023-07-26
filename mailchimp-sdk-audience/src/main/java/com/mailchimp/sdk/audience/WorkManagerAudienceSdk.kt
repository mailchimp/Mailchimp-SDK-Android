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
import com.mailchimp.sdk.api.model.ContactEvent
import com.mailchimp.sdk.api.model.mergefields.Address
import com.mailchimp.sdk.core.MailchimpSdkConfiguration
import com.mailchimp.sdk.core.work.WorkProcessor
import com.mailchimp.sdk.core.work.WorkStatusProvider
import timber.log.Timber
import java.util.UUID

class WorkManagerAudienceSdk(
    private val workProcessor: WorkProcessor,
    private val gson: Gson,
    private val sdkConfiguration: MailchimpSdkConfiguration,
    workStatusProvider: WorkStatusProvider
) : AudienceSdkContract, WorkStatusProvider by workStatusProvider {
    override fun createOrUpdateContact(contact: Contact): UUID {
        return if (sdkConfiguration.autoTaggingEnabled) {
            val autoTaggedContact = applyAutoTags(contact)
            submitUpdateRequest(autoTaggedContact)
        } else {
            submitUpdateRequest(contact)
        }
    }

    override fun addTag(email: String, tag: String): UUID {
        val contact = Contact.Builder(email).addTag(tag).build()
        return submitUpdateRequest(contact)
    }

    override fun removeTag(email: String, tag: String): UUID {
        val contact = Contact.Builder(email).removeTag(tag).build()
        return submitUpdateRequest(contact)
    }

    override fun addTags(email: String, tags: List<String>): UUID {
        val contactBuilder = Contact.Builder(email)
        tags.forEach { contactBuilder.addTag(it) }
        return submitUpdateRequest(contactBuilder.build())
    }

    override fun removeTags(email: String, tags: List<String>): UUID {
        val contactBuilder = Contact.Builder(email)
        tags.forEach { contactBuilder.removeTag(it) }
        return submitUpdateRequest(contactBuilder.build())
    }

    override fun setMergeField(
        email: String,
        mergeFieldName: String,
        mergeFieldValue: String
    ): UUID {
        val contact = Contact.Builder(email).setMergeField(mergeFieldName, mergeFieldValue).build()
        return submitUpdateRequest(contact)
    }

    override fun setMergeField(
        email: String,
        mergeFieldName: String,
        mergeFieldValue: Address
    ): UUID {
        val contact = Contact.Builder(email).setMergeField(mergeFieldName, mergeFieldValue).build()
        return submitUpdateRequest(contact)
    }

    override fun setMarketingPermission(email: String, id: String, granted: Boolean): UUID {
        val contact = Contact.Builder(email).setMarketingPermission(id, granted).build()
        return submitUpdateRequest(contact)
    }

    override fun addContactEvent(
        email: String,
        eventName: String,
        contactEventProperties: Map<String, String>?
    ): UUID? {
        if (eventName.length > ContactEvent.MAX_EVENT_NAME_LENGTH) {
            Timber.e(
                "Event name(%s) must no longer than %s characters",
                eventName,
                ContactEvent.MAX_EVENT_NAME_LENGTH
            )
            return null
        }
        var invalidString: String? = null
        contactEventProperties?.forEach {
            if (!ContactEvent.PROPERTIES_KEY_REGEX_PATTERN.toRegex().matches(it.key)) {
                invalidString = it.key
            }
        }
        if (invalidString != null) {
            Timber.e(
                "Invalid Event Property Key (%s): Keys are restricted to A-z and underscores",
                invalidString
            )
            return null
        }

        val contactEventRequest = ContactEvent(email, eventName, contactEventProperties)
        val workRequest = EventWorkRequest(contactEventRequest, gson)
        return workProcessor.submitWork(workRequest).first
    }

    private fun submitUpdateRequest(contact: Contact): UUID {
        val workRequest = AudienceWorkRequest(contact, gson)
        return workProcessor.submitWork(workRequest).first
    }

    private fun applyAutoTags(contact: Contact): Contact {
        val builder = Contact.Builder(contact)
        builder.addTag(Contact.ANDROID_TAG)

        if (sdkConfiguration.context.resources.getBoolean(com.mailchimp.mailchimp_sdk_core.R.bool.isTablet)) {
            builder.addTag(Contact.TABLET_TAG)
        } else {
            builder.addTag(Contact.PHONE_TAG)
        }
        return builder.build()
    }
}