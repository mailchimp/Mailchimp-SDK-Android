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
import com.mailchimp.sdkdemo.R
import kotlinx.android.synthetic.main.layout_marketing_permission_field.view.*

class MarketingPermissionLayout(context: Context, attrs: AttributeSet?) : ConstraintLayout(context, attrs) {
    init {
        val inflater = LayoutInflater.from(context)
        inflater.inflate(R.layout.layout_marketing_permission_field, this, true)
    }

    val removeButton = btn_remove_field
    val isPermissionGranted: Boolean
        get() {
            return sc_value.isChecked
        }
    val permission: String
        get() {
            return tiet_key_MPFL.text.toString().trim()
        }
    var removeButtonVisible = false
        set(value) {
            field = value
            if (value) {
                btn_remove_field.visibility = View.VISIBLE
            } else {
                btn_remove_field.visibility = View.GONE
            }
        }
}
