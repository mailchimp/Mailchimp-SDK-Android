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
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.google.android.material.textfield.TextInputLayout
import com.mailchimp.sdkdemo.R
import kotlinx.android.synthetic.main.layout_named_field.view.*

class NamedFieldLayout(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    var label: String? = null
        set(value) {
            til_NFL.hint = value
            field = value
        }

    init {
        val inflater = LayoutInflater.from(context)
        inflater.inflate(R.layout.layout_named_field, this, true)
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        var typedArray: TypedArray? = null
        try {
            typedArray = context.theme.obtainStyledAttributes(attrs, R.styleable.NamedFieldLayout, 0, 0)
            label = typedArray.getString(R.styleable.NamedFieldLayout_label)
        } finally {
            typedArray?.recycle()
        }
    }

    val removeButton = btn_remove_field
    val value: String
        get() {
            return tiet_NFL.text.toString().trim()
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
