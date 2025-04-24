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
import com.mailchimp.sdk.api.model.mergefields.Address
import com.mailchimp.sdk.api.model.mergefields.MergeField
import com.mailchimp.sdk.api.model.mergefields.MergeFieldValue
import com.mailchimp.sdk.api.model.mergefields.StringMergeFieldValue

/**
 * A Contact represents one member of your Audience. This is used for creating or updating a
 * contact. See [Contact.Builder] for more detailed instructions for constructing a contact.
 */
@ConsistentCopyVisibility
data class Contact private constructor(
    /**
     * The contacts email address.
     */
    val emailAddress: String,
    /**
     * A list of the merge fields to be applied/updated on the contact.
     */
    val mergeFields: List<MergeField>? = null,
    /**
     * A list of tags to be applied or removed on the contact.
     */
    val tags: List<Tag>? = null,
    /**
     * A list of marketing permissions to be set on the contact.
     */
    val marketingPermissions: List<MarketingPermission>? = null,
    /**
     * The contact status to be set on the contact. This will only take effect on newly created
     * contacts.
     */
    val contactStatus: ContactStatus? = null
) {

    companion object {
        const val ACTIVE_TAG_STATUS = "active"
        const val INACTIVE_TAG_STATUS = "inactive"

        /**
         * Tag marking the user as an Android User.
         */
        const val ANDROID_TAG = "Android"

        /**
         * Tag marking the user as a Phone user.
         */
        const val PHONE_TAG = "Phone"

        /**
         * Tag marking the user as a Tablet user.
         */
        const val TABLET_TAG = "Tablet"
    }

    /**
     * Builder for a [Contact]
     *
     * emailAddress is the only required field and is used to identify a contact.
     */
    class Builder {

        /**
         * Creates a [Contact.Builder] object.
         *
         * @param emailAddress the email address identifying the contact.
         */
        constructor(emailAddress: String) {
            this.email = emailAddress
        }

        /**
         * Creates a [Contact.Builder] taking in an existing contact as a base.
         *
         * @param contact the contact to use as the base contact.
         */
        constructor(contact: Contact) {
            this.email = contact.emailAddress
            this.mergeFields = (contact.mergeFields ?: emptyList()).associateBy { it.key }.toMutableMap()
            this.tags = (contact.tags ?: emptyList()).associateBy { it.name }.toMutableMap()
            this.contactStatus = contact.contactStatus

            this.marketingPermissions = (contact.marketingPermissions ?: emptyList())
                .associateBy { it.id }.toMutableMap()
        }

        private val email: String
        private var mergeFields: MutableMap<String, MergeField> = mutableMapOf()
        private var tags: MutableMap<String, Tag> = mutableMapOf()
        private var marketingPermissions: MutableMap<String, MarketingPermission> = mutableMapOf()
        private var contactStatus: ContactStatus? = null

        /**
         * Sets the merge field value on the contact.
         *
         * @param key The key for the merge field without vertical bars (ex. FNAME)
         * @param value The new value for the merge field
         */
        fun setMergeField(key: String, value: String) =
            apply { this.mergeFields[key] = MergeField(key, StringMergeFieldValue(value)) }

        /**
         * Sets the merge field value on the contact.
         *
         * @param key The key for the merge field without vertical bars (ex. FNAME)
         * @param value The new value for the merge field
         */
        fun setMergeField(key: String, value: Address) = apply { this.mergeFields[key] = MergeField(key, value) }

        /**
         * Puts a request to add a tag to the contact on the contact object.
         *
         * @param name the name of the tag
         */
        fun addTag(name: String) = apply { this.tags[name] = Tag(name, ACTIVE_TAG_STATUS) }

        /**
         * Puts a request to remove a tag from the contact on the contact object.
         *
         * @param name the name of the tag
         */
        fun removeTag(name: String) = apply { this.tags[name] = Tag(name, INACTIVE_TAG_STATUS) }

        /**
         * Sets the Contact Status of the contact. The default value is [ContactStatus.TRANSACTIONAL].
         * This value is only used on initial creation of the object. If the contact already exists
         * this value will be ignored and will not update the ContactStatus of the Contact.
         *
         * @param contactStatus The contact status of the contact.
         */
        fun setContactStatus(contactStatus: ContactStatus?) = apply { this.contactStatus = contactStatus }

        /**
         * Sets the given marketing permission on the contact as either granted or not. This
         * indicates that the contact has agreed to receive the given type of marketing.
         *
         * @param id The id for the contact status
         * @param granted whether the user has agreed to receive marketing of this type or not.
         */
        fun setMarketingPermission(id: String, granted: Boolean) =
            apply { this.marketingPermissions[id] = MarketingPermission(id, granted) }

        private fun <T> mapCleaner(map: Map<String, T>): List<T>? {
            val list = map.values
            return if (list.isEmpty()) null else list.toList()
        }

        /**
         * Builds the [Contact] object.
         *
         * @return the contact object
         */
        fun build(): Contact {
            return Contact(
                email,
                mapCleaner(mergeFields),
                mapCleaner(tags),
                mapCleaner(marketingPermissions),
                contactStatus
            )
        }
    }
}

data class Tag(val name: String, val status: String)

data class MarketingPermission(
    @SerializedName("marketing_permission_id")
    val id: String,
    val enabled: Boolean
)

/**
 * Used to indicate the subscription status of the contact.
 */
enum class ContactStatus {
    /**
     * Indicates that a contact is subscribed to marketing campaigns.
     */
    @SerializedName("subscribed")
    SUBSCRIBED,

    /**
     * Indicated that a contact is not subscribed to marketing campaigns and will only receive
     * transactional information.
     */
    @SerializedName("transactional")
    TRANSACTIONAL
}

/**
 * This is the API representation of a contact. The schema does not allow simple deserialization of
 * a merge field because there is no simple way to determine the type of a merge field object in
 * its JSON form. That is why this class exists, in addition to decoupling the API schema from the SDK
 * public contract.
 */
data class ApiContact(
    @SerializedName("email_address")
    val emailAddress: String,
    @SerializedName("merge_fields")
    val mergeFields: Map<String, Any>? = null,
    val tags: List<Tag>? = null,
    @SerializedName("marketing_permissions")
    val marketingPermissions: List<MarketingPermission>? = null,
    @SerializedName("status")
    val contactStatus: ContactStatus? = null
) {
    companion object {
        fun fromContact(contact: Contact): ApiContact {
            return ApiContact(
                contact.emailAddress,
                translateMergeFields(contact.mergeFields),
                contact.tags,
                contact.marketingPermissions,
                contact.contactStatus
            )
        }

        private fun translateMergeFields(mergeFields: List<MergeField>?): Map<String, Any> {
            return (mergeFields ?: emptyList()).associateBy({ it.key }, { translateMergeValue(it.value) })
        }

        private fun translateMergeValue(value: MergeFieldValue): Any {
            return if (value.javaClass == StringMergeFieldValue::class.java) {
                (value as StringMergeFieldValue).value
            } else {
                value
            }
        }
    }
}
