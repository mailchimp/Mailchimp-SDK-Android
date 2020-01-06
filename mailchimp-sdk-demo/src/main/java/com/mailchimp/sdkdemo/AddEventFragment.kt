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

package com.mailchimp.sdkdemo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.mailchimp.sdk.main.Mailchimp
import com.mailchimp.sdkdemo.ui.KeyValueLayout
import kotlinx.android.synthetic.main.fragment_add_event.*
import timber.log.Timber

class AddEventFragment : Fragment() {

    private val mailchimpSdk = Mailchimp.sharedInstance()
    private val propertiesCellList = mutableListOf<KeyValueLayout>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_event, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btn_add_pair.setOnClickListener { appendPropertyCell() }
        btn_create_event.setOnClickListener { addEvent() }
    }

    private fun addEvent() {
        val email = nfl_email_FAE.value
        if (email.isBlank()) {
            Toast.makeText(context!!, "Please enter an Email", Toast.LENGTH_SHORT).show()
            return
        }
        val eventName = nfl_event_name_FAE.value
        if (eventName.isBlank()) {
            Toast.makeText(context!!, "Please enter an Event Name", Toast.LENGTH_SHORT).show()
            return
        }


        val properties = mutableMapOf<String, String>()
        for (view in propertiesCellList) {
            val propertyName = view.key
            val propertyValue = view.value
            if (propertyName.isBlank() && propertyValue.isBlank()) {
                Timber.i("Ignoring empty property")
            }
            else if (propertyName.isBlank() || propertyValue.isBlank()) {
                Toast.makeText(context!!, "Property names and values cannot be empty", Toast.LENGTH_SHORT).show()
                return
            } else {
                properties.put(view.key, view.value)
            }
        }

        // This check is to make sure a null value is okay
        val eventId = if (properties.isEmpty()) {
            mailchimpSdk.addContactEvent(email, eventName)
        } else {
            mailchimpSdk.addContactEvent(email, eventName, properties)
        }

        if (eventId == null) {
            Toast.makeText(
                context!!,
                "Malformed request: Check logs to see a more detailed error",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val liveData = mailchimpSdk.getStatusByIdLiveData(eventId)
        liveData.observe(
            this@AddEventFragment,
            androidx.lifecycle.Observer {
                Toast.makeText(context!!, "Work: $it", Toast.LENGTH_SHORT).show()
            })

        clearAddedCells()
    }

    private fun appendPropertyCell() {
        val newLayout = KeyValueLayout(context!!, null)
        newLayout.removeButtonVisible = true
        newLayout.setKeyHint(getString(R.string.key))
        newLayout.setValueHint(getString(R.string.value))
        newLayout.removeButton.setOnClickListener {
            removeView(
                newLayout,
                propertiesCellList
            )
        }
        propertiesCellList.add(newLayout)
        ll_add_on_fields_FAE.addView(newLayout)
    }

    private fun clearAddedCells() {
        propertiesCellList.clear()
        ll_add_on_fields_FAE.removeAllViews()
    }

    private fun <T : View> removeView(view: T, list: MutableList<T>) {
        ll_add_on_fields_FAE.removeView(view)
        list.remove(view)
    }
}