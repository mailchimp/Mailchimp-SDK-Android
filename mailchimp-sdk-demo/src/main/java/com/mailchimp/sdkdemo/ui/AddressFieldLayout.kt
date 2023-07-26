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

package com.mailchimp.sdkdemo.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import androidx.constraintlayout.widget.ConstraintLayout
import com.mailchimp.sdk.api.model.mergefields.Address
import com.mailchimp.sdk.api.model.mergefields.Country
import com.mailchimp.sdkdemo.R
import com.mailchimp.sdkdemo.databinding.LayoutAddressFieldBinding

class AddressFieldLayout(context: Context, attrs: AttributeSet?) : ConstraintLayout(context, attrs) {


    private val binding: LayoutAddressFieldBinding

    init {
        val inflater = LayoutInflater.from(context)
        binding = LayoutAddressFieldBinding.inflate(inflater, this)

        val adapter = ArrayAdapter<Country>(context, R.layout.country_spinner_item)
        adapter.setDropDownViewResource(R.layout.spinner_drop_down)
        adapter.addAll(Country.values().toList())
        binding.spnrCountry.adapter = adapter
    }

    val removeButton = binding.btnRemoveField

    val key: String
        get() {
            return binding.tietKeyAFL.text.toString().trim()
        }

    val lineOne: String
        get() {
            return binding.tietLineOneAFL.text.toString().trim()
        }

    val lineTwo: String
        get() {
            return binding.tietLineTwoAFL.text.toString().trim()
        }

    val city: String
        get() {
            return binding.tietCityAFL.text.toString().trim()
        }

    val state: String
        get() {
            return binding.tietStateAFL.text.toString().trim()
        }

    val country: Country
        get() {
            return binding.spnrCountry.selectedItem as Country
        }

    val zip: String
        get() {
            return binding.tietZipAFL.text.toString().trim()
        }

    val address: Address?
        get() {
            if (lineOne.isEmpty() || city.isEmpty() || zip.isEmpty()) {
                return null
            }

            val builder = Address.Builder(lineOne, city, zip)
            var fieldSet = false
            fieldSet = setAddressField(lineTwo) { builder.setAddressLineTwo(it) } || fieldSet
            fieldSet = setAddressField(state) { builder.setState(it) } || fieldSet
            builder.setCountry(country)
            return if (!fieldSet) null else builder.build()
        }

    var removeButtonVisible = false
        set(value) {
            field = value
            if (value) {
                binding.btnRemoveField.visibility = View.VISIBLE
            } else {
                binding.btnRemoveField.visibility = View.GONE
            }
        }

    private fun setAddressField(value: String, setter: (String) -> Address.Builder): Boolean {
        return if (value.isNotBlank()) {
            setter(value)
            true
        } else {
            false
        }
    }
}
