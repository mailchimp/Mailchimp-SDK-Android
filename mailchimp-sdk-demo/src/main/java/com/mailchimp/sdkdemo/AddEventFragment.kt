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
import com.mailchimp.sdkdemo.databinding.FragmentAddEventBinding
import com.mailchimp.sdkdemo.ui.KeyValueLayout
import timber.log.Timber

class AddEventFragment : Fragment() {

    private val mailchimpSdk = Mailchimp.sharedInstance()
    private val propertiesCellList = mutableListOf<KeyValueLayout>()

    private var _binding: FragmentAddEventBinding? = null

    /**
     * This property is only valid between onCreateView and onDestroyView.
     */
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentAddEventBinding.inflate(inflater, container, false).let {
            _binding = it
            it.root
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnAddPair.setOnClickListener { appendPropertyCell() }
        binding.btnCreateEvent.setOnClickListener { addEvent() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun addEvent() {
        val email = binding.nflEmailFAE.value
        if (email.isBlank()) {
            Toast.makeText(requireContext(), "Please enter an Email", Toast.LENGTH_SHORT).show()
            return
        }
        val eventName = binding.nflEventNameFAE.value
        if (eventName.isBlank()) {
            Toast.makeText(requireContext(), "Please enter an Event Name", Toast.LENGTH_SHORT).show()
            return
        }


        val properties = mutableMapOf<String, String>()
        for (view in propertiesCellList) {
            val propertyName = view.key
            val propertyValue = view.value
            if (propertyName.isBlank() && propertyValue.isBlank()) {
                Timber.i("Ignoring empty property")
            } else if (propertyName.isBlank() || propertyValue.isBlank()) {
                Toast.makeText(requireContext(), "Property names and values cannot be empty", Toast.LENGTH_SHORT).show()
                return
            } else {
                properties[view.key] = view.value
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
                requireContext(),
                "Malformed request: Check logs to see a more detailed error",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val liveData = mailchimpSdk.getStatusByIdLiveData(eventId)
        liveData.observe(viewLifecycleOwner) {
            Toast.makeText(requireContext(), "Work: $it", Toast.LENGTH_SHORT).show()
        }

        clearAddedCells()
    }

    private fun appendPropertyCell() {
        val newLayout = KeyValueLayout(requireContext(), null)
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
        binding.llAddOnFieldsFAE.addView(newLayout)
    }

    private fun clearAddedCells() {
        propertiesCellList.clear()
        binding.llAddOnFieldsFAE.removeAllViews()
    }

    private fun <T : View> removeView(view: T, list: MutableList<T>) {
        binding.llAddOnFieldsFAE.removeView(view)
        list.remove(view)
    }
}
