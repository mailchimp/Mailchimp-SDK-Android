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

package com.mailchimp.sdkdemo.mockapi

import com.mailchimp.sdk.api.model.ApiContact
import com.mailchimp.sdk.api.model.UpdateContactResponse
import timber.log.Timber

class MockMailchimpAudienceBackend {

    private val contactMap = mutableMapOf<String, ApiContact>()
    private val lastCallMap = mutableMapOf<String, ApiContact>()

    fun createOrUpdateContact(contactUpdates: ApiContact): UpdateContactResponse {

        // Add to last call Map
        lastCallMap[contactUpdates.emailAddress] = contactUpdates

        // Add to Contact Map, Merge if necessary
        val currentContact: ApiContact? = contactMap[contactUpdates.emailAddress]
        if (currentContact != null) {
            Timber.i("Updating Contact For: ${contactUpdates.emailAddress} ")
            // Merge updates
            val updatedContact = mergeContacts(currentContact, contactUpdates)
            contactMap[contactUpdates.emailAddress] = updatedContact
            Timber.i("Updated Contact: $updatedContact")
        } else {
            // Adding new contact
            Timber.i("Creating New Contact For: ${contactUpdates.emailAddress}")
            contactMap[contactUpdates.emailAddress] = contactUpdates
            Timber.i("New Contact: $contactUpdates")
        }
        return UpdateContactResponse(contactUpdates.emailAddress)
    }

    private fun mergeContacts(contact: ApiContact, updates: ApiContact): ApiContact {
        val mergeField = mergeMaps(contact.mergeFields, updates.mergeFields)
        val tags = mergeLists(contact.tags, updates.tags) { it.name }
        val marketingPermission = mergeLists(contact.marketingPermissions, updates.marketingPermissions) { it.id }
        // Contact status is not updated because we currently do not support updating on the real api

        return contact.copy(contact.emailAddress, mergeField, tags, marketingPermission, contact.contactStatus)
    }

    private fun <T> mergeLists(contactList: List<T>?, updateList: List<T>?, keyGen: (T) -> String): List<T>? {
        if (contactList == null && updateList == null) {
            return null
        } else if (contactList == null) {
            return updateList
        } else if (updateList == null) {
            return contactList
        }
        val outList = mutableListOf<T>()
        outList.addAll(updateList)

        val updateKeys = updateList.map(keyGen)
        outList.addAll(contactList.filter { !updateKeys.contains(keyGen(it)) })

        return outList
    }

    private fun <K, V> mergeMaps(contactMap: Map<K, V>?, updateMap: Map<K, V>?): Map<K, V>? {
        if (contactMap == null && updateMap == null) {
            return null
        } else if (contactMap == null) {
            return updateMap
        } else if (updateMap == null) {
            return contactMap
        }

        val outMap = contactMap.toMutableMap()
        outMap.putAll(updateMap)
        return outMap
    }

    fun getContact(email: String): ApiContact? {
        return contactMap[email]
    }

    fun getLastCall(email: String): ApiContact? {
        return lastCallMap[email]
    }
}