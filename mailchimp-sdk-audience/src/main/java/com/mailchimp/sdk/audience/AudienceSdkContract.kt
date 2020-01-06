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

import androidx.annotation.Size
import com.mailchimp.sdk.api.model.Contact
import com.mailchimp.sdk.api.model.ContactEvent
import com.mailchimp.sdk.api.model.mergefields.Address
import com.mailchimp.sdk.core.work.WorkStatusProvider
import java.util.*

interface AudienceSdkContract : WorkStatusProvider {

    /**
     * Used to add a [Contact] to your Mailchimp Audience. If the contact already exists it will be
     * updated with the newly supplied information.
     *
     * @param contact The [Contact] to add or update
     * @return The [UUID] of the job, used for polling the status.
     */
    fun createOrUpdateContact(contact: Contact): UUID

    // Tags
    /**
     * Adds a tag to a given user.
     *
     * Note: If the user does not exist it will be created.
     * Note: If the tag does not exist is will be created.
     *
     * @param email The user's email address
     * @param tag The name of the tag to apply to the user.
     * @return The [UUID] of the job, used for polling the status.
     */
    fun addTag(email: String, tag: String): UUID

    /**
     * Adds multiple tags a given user.
     *
     * Note: If the user does not exist it will be created.
     * Note: If any of the tags do not exist they will be created.
     *
     * @param email The user's email address
     * @param tags A list of tag names to apply to the user
     * @return The [UUID] of the job, used for polling the status.
     */
    fun addTags(email: String, tags: List<String>): UUID

    /**
     * Removes a tag from a given user.
     *
     * @param email The user's email address
     * @param tag The name of the tag to be removed from the user.
     * @return The [UUID] of the job, used for polling the status.
     */
    fun removeTag(email: String, tag: String): UUID

    /**
     * Removes multiple tags from a given user.
     *
     * @param email The user's email address
     * @param tags A list of tag names to be removed from the user.
     * @return The [UUID] of the job, used for polling the status.
     */
    fun removeTags(email: String, tags: List<String>): UUID

    // Merge Fields
    /**
     * Sets the value of a merge field for a given user to the supplied value. If a value for the merge
     * field already exists it will be overwritten.
     *
     * @param email The user's email address
     * @param mergeFieldName The name of the merge field without VerticalBars (ex. FNAME)
     * @param mergeFieldValue The new value of the merge field.
     * @return The [UUID] of the job, used for polling the status.
     */
    fun setMergeField(email: String, mergeFieldName: String, mergeFieldValue: String): UUID

    /**
     * Sets the value of a merge field for a given user to the supplied value. If a value for the merge
     * field already exists it will be overwritten.
     *
     * @param email The user's email address
     * @param mergeFieldName The name of the merge field without VerticalBars (ex. FNAME)
     * @param mergeFieldValue The new value of the merge field.
     * @return The [UUID] of the job, used for polling the status.
     */
    fun setMergeField(email: String, mergeFieldName: String, mergeFieldValue: Address): UUID

    /**
     * Sets a marketing permission that the contact has agreed to.
     *
     * @param email The user's email address
     * @param id The marketing permission
     * @param granted whether the user has granted the given permission or not
     * @return The [UUID] of the job, used for polling the status.
     */
    fun setMarketingPermission(email: String, id: String, granted: Boolean): UUID

    /**
     * Adds an event for the given contact. Unlike creating or updating a contact, adding an event
     * will execute always execute immediately and will only be retried within the first 5 minutes.
     * This is to ensure that events are handled at the appropriate time on the backend.
     *
     * @param email The user's email address
     * @param eventName The name of the event, limited to 30 characters.
     * @param contactEventProperties Additional properties in the form of key values pairs. Keys are
     * restricted to A-z and underscores.
     * @return The [UUID] of the job, used for polling the status. Null if the inputs do not conform
     * to the above standard.
     */
    fun addContactEvent(
        email: String,
        @Size(ContactEvent.MAX_EVENT_NAME_LENGTH.toLong()) eventName: String,
        contactEventProperties: Map<String, String>? = null
    ): UUID?
}