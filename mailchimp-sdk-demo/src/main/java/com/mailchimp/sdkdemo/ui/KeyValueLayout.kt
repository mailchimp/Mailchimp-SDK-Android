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
import com.mailchimp.sdkdemo.databinding.LayoutKeyValueBinding

class KeyValueLayout(context: Context, attrs: AttributeSet?) : ConstraintLayout(context, attrs) {

    private val binding = LayoutKeyValueBinding.inflate(LayoutInflater.from(context), this)

    var label: String? = null
        set(value) {
            if (!value.isNullOrEmpty()) {
                binding.tvLabelKVL.text = value
                binding.tvLabelKVL.visibility = View.VISIBLE
            } else {
                binding.tvLabelKVL.visibility = View.GONE
            }
            field = value
        }

    val removeButton = binding.btnRemoveField

    val value: String
        get() {
            return binding.tietValueKVL.text.toString().trim()
        }

    val key: String
        get() {
            return binding.tietKeyKVL.text.toString().trim()
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

    fun setValueHint(hint: String) {
        binding.tilValueKVL.hint = hint
    }

    fun setKeyHint(hint: String) {
        binding.tilKeyKVL.hint = hint
    }
}
