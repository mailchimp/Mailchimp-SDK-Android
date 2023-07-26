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
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.mailchimp.sdk.api.model.Contact
import com.mailchimp.sdk.api.model.ContactStatus
import com.mailchimp.sdk.main.Mailchimp
import com.mailchimp.sdkdemo.databinding.FragmentAddContactBinding
import com.mailchimp.sdkdemo.ui.AddressFieldLayout
import com.mailchimp.sdkdemo.ui.KeyValueLayout
import com.mailchimp.sdkdemo.ui.MarketingPermissionLayout
import com.mailchimp.sdkdemo.ui.NamedFieldLayout

class AddContactFragment : Fragment() {

    private var _binding: FragmentAddContactBinding? = null
    private val binding get() = _binding!!

    private val audienceSdk = Mailchimp.sharedInstance()
    private val addTagsCellList = mutableListOf<NamedFieldLayout>()
    private val removeTagsCellList = mutableListOf<NamedFieldLayout>()
    private val stringMergeFieldCellList = mutableListOf<KeyValueLayout>()
    private val addressFieldCellList = mutableListOf<AddressFieldLayout>()
    private val marketingPermissionCellList = mutableListOf<MarketingPermissionLayout>()
    private val noContactStatus = object {
        override fun toString(): String {
            return "DON'T SEND CONTACT STATUS"
        } // Placeholder object that is a stand in for a null value
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    requireActivity().finish()
                }
            })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddContactBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // This is gross but ¯\_(ツ)_/¯ since it's the demo app
        val adapter = ArrayAdapter<Any>(requireContext(), R.layout.spinner_demo_app)
        adapter.setDropDownViewResource(R.layout.spinner_drop_down)
        ContactStatus.values().toList().forEach { adapter.add(it) }
        adapter.add(noContactStatus)
        binding.spnrContactStatus.adapter = adapter

        binding.btnCreateContact.setOnClickListener { callCreateOrUpdateEndpoint() }
        binding.btnAddTag.setOnClickListener { appendAddTagCell() }
        binding.btnRemoveTag.setOnClickListener { appendRemoveTagCell() }
        binding.btnAddMergeField.setOnClickListener { appendMergeFieldCell() }
        binding.btnAddAddress.setOnClickListener { appendAddressCell() }
        binding.btnAddMarketingPermission.setOnClickListener { appendMarketingPermissionCell() }
    }


    private fun callCreateOrUpdateEndpoint() {
        val email = binding.nflEmail.value
        if (email.isBlank()) {
            Toast.makeText(requireContext(), "Please enter an Email", Toast.LENGTH_SHORT).show()
            return
        }
        val contactBuilder = Contact.Builder(email)

        val spnrOption = binding.spnrContactStatus.selectedItem
        if (spnrOption != noContactStatus) {
            contactBuilder.setContactStatus(spnrOption as ContactStatus)
        }

        for (view in addTagsCellList) {
            if (view.value.isNotBlank()) {
                contactBuilder.addTag(view.value)
            }
        }

        for (view in removeTagsCellList) {
            if (view.value.isNotBlank()) {
                contactBuilder.removeTag(view.value)
            }
        }

        for (view in stringMergeFieldCellList) {
            if (view.value.isNotBlank() && view.key.isNotBlank()) {
                contactBuilder.setMergeField(view.key, view.value)
            }
        }

        for (view in addressFieldCellList) {
            val addr = view.address
            if (addr != null) {
                contactBuilder.setMergeField(view.key, addr)
            }
        }

        for (view in marketingPermissionCellList) {
            contactBuilder.setMarketingPermission(view.permission, view.isPermissionGranted)
        }

        val contact = contactBuilder.build()
        val uuid = audienceSdk.createOrUpdateContact(contact)

        val liveData = Mailchimp.sharedInstance().getStatusByIdLiveData(uuid)
        liveData.observe(
            viewLifecycleOwner,
            androidx.lifecycle.Observer {
                Toast.makeText(requireContext(), "Work: $it", Toast.LENGTH_SHORT).show()
            }
        )

        clearAddedCells()
    }

    private fun appendAddTagCell() {
        val newLayout = NamedFieldLayout(requireContext(), null)
        setViewMargins(newLayout)
        newLayout.label = getString(R.string.add_tag)
        newLayout.removeButtonVisible = true
        newLayout.removeButton.setOnClickListener { removeView(newLayout, addTagsCellList) }
        addTagsCellList.add(newLayout)
        binding.llAddOnFields.addView(newLayout)
    }

    private fun appendRemoveTagCell() {
        val newLayout = NamedFieldLayout(requireContext(), null)
        setViewMargins(newLayout)
        newLayout.label = getString(R.string.remove_tag)
        newLayout.removeButtonVisible = true
        newLayout.removeButton.setOnClickListener { removeView(newLayout, removeTagsCellList) }
        removeTagsCellList.add(newLayout)
        binding.llAddOnFields.addView(newLayout)
    }

    private fun appendMergeFieldCell() {
        val newLayout = KeyValueLayout(requireContext(), null)
        setViewMargins(newLayout)
        newLayout.label = getString(R.string.set_merge_field)
        newLayout.removeButtonVisible = true
        newLayout.setKeyHint(getString(R.string.key))
        newLayout.setValueHint(getString(R.string.value))
        newLayout.removeButton.setOnClickListener {
            removeView(
                newLayout,
                stringMergeFieldCellList
            )
        }
        stringMergeFieldCellList.add(newLayout)
        binding.llAddOnFields.addView(newLayout)
    }

    private fun appendAddressCell() {
        val newLayout = AddressFieldLayout(requireContext(), null)
        setViewMargins(newLayout)
        newLayout.removeButtonVisible = true
        newLayout.removeButton.setOnClickListener { removeView(newLayout, addressFieldCellList) }
        addressFieldCellList.add(newLayout)
        binding.llAddOnFields.addView(newLayout)
    }

    private fun appendMarketingPermissionCell() {
        val newLayout = MarketingPermissionLayout(requireContext(), null)
        setViewMargins(newLayout)
        newLayout.removeButtonVisible = true
        newLayout.removeButton.setOnClickListener {
            removeView(
                newLayout,
                marketingPermissionCellList
            )
        }
        marketingPermissionCellList.add(newLayout)
        binding.llAddOnFields.addView(newLayout)
    }

    private fun setViewMargins(view: View) {
        val eightDp = resources.getDimension(R.dimen.four_dp).toInt() * 2
        val lp = ViewGroup.MarginLayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        lp.setMargins(0, eightDp, 0, eightDp)
        view.layoutParams = lp
    }

    private fun clearAddedCells() {
        stringMergeFieldCellList.clear()
        addTagsCellList.clear()
        removeTagsCellList.clear()
        addressFieldCellList.clear()
        marketingPermissionCellList.clear()
        binding.llAddOnFields.removeAllViews()
    }

    private fun <T : View> removeView(view: T, list: MutableList<T>) {
        binding.llAddOnFields.removeView(view)
        list.remove(view)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}