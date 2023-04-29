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
import androidx.constraintlayout.widget.ConstraintLayout
import com.mailchimp.sdkdemo.databinding.LayoutMarketingPermissionFieldBinding

class MarketingPermissionLayout(context: Context, attrs: AttributeSet?) : ConstraintLayout(context, attrs) {

    private val binding = LayoutMarketingPermissionFieldBinding.inflate(LayoutInflater.from(context), this)

    val removeButton = binding.btnRemoveField

    val isPermissionGranted: Boolean
        get() {
            return binding.scValue.isChecked
        }

    val permission: String
        get() {
            return binding.tietKeyMPFL.text.toString().trim()
        }

    var removeButtonVisible = false
        set(value) {
            field = value
            binding.btnRemoveField.visibility = if (value) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
}
