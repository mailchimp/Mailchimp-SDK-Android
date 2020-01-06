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

package com.mailchimp.sdk.api.model.mergefields

import com.google.gson.annotations.SerializedName

/**
 * Represents the value of a MergeField that is of Address type.
 *
 * @property addressLineOne First line of the address (ex. 4040 Main St.) : Required
 * @property addressLineTwo Second line of the address (ex. Apt. 404) : Optional
 * @property city City (ex. Atlanta): Required
 * @property state State or Province (ex. Georgia): Optional
 * @property country [Country] (ex. [Country.USA]): Optional
 * @property zip Zip (ex. 30308): Required
 */
@Suppress("DataClassPrivateConstructor")
data class Address private constructor(
    @SerializedName("addr1")
    val addressLineOne: String?,
    @SerializedName("addr2")
    val addressLineTwo: String?,
    val city: String?,
    val state: String?,
    val country: Country?,
    val zip: String?
) : MergeFieldValue {

    /**
     * Builder for [Address]. See [Address] for a more completed description of the values.
     */
    class Builder(
        private val addressLineOne: String,
        private val city: String,
        private val zip: String
    ) {
        private var addressLineTwo: String? = null
        private var state: String? = null
        private var country: Country? = null

        /**
         * Set the second line of the address. This is optional.
         *
         * @param addressLineTwo The second line of the address
         */
        fun setAddressLineTwo(addressLineTwo: String) = apply { this.addressLineTwo = addressLineTwo }

        /**
         * Set the state of the address. This is optional.
         *
         * @param state the state
         */
        fun setState(state: String) = apply { this.state = state }

        /**
         * Set the country of the address. This is optional.
         *
         * @param country the [Country]
         */
        fun setCountry(country: Country) = apply { this.country = country }
        fun build(): Address {
            return Address(addressLineOne, addressLineTwo, city, state, country, zip)
        }
    }
}
