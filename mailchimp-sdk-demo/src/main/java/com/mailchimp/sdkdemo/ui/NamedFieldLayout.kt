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
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.withStyledAttributes
import com.mailchimp.sdkdemo.R
import com.mailchimp.sdkdemo.databinding.LayoutNamedFieldBinding

class NamedFieldLayout(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    private val binding = LayoutNamedFieldBinding.inflate(LayoutInflater.from(context), this)

    init {
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        context.withStyledAttributes(attrs, R.styleable.NamedFieldLayout) {
            binding.tilNFL.hint = getString(R.styleable.NamedFieldLayout_label)
        }
    }

    val removeButton = binding.btnRemoveField

    val value: String
        get() {
            return binding.tietNFL.text.toString().trim()
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

    var label: String?
        get() = binding.tilNFL.hint?.toString()
        set(value) {
            binding.tilNFL.hint = value
        }
}
