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
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.mailchimp.sdk.api.di.ApiImplementation
import com.mailchimp.sdk.audience.di.AudienceDependencies
import com.mailchimp.sdk.audience.di.AudienceImplementation
import com.mailchimp.sdk.core.MailchimpSdkConfiguration
import com.mailchimp.sdk.main.Mailchimp
import com.mailchimp.sdk.main.di.MailchimpInjector
import com.mailchimp.sdkdemo.databinding.FragmentSetupBinding
import com.mailchimp.sdkdemo.mockapi.MockApiImplementation
import com.mailchimp.sdkdemo.mockapi.MockMailchimp
import java.util.Locale

class SetupFragment : Fragment() {

    private var _binding: FragmentSetupBinding? = null

    /**
     * This property is only valid between onCreateView and onDestroyView.
     */
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return FragmentSetupBinding.inflate(inflater, container, false).let {
            _binding = it
            it.root
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.cbxAutotag.isChecked = true
        binding.cbxDebug.isChecked = true
        binding.etSdkKey.text = Editable.Factory().newEditable(BuildConfig.MAILCHIMP_SDK_DEMO_KEY)
        binding.btnStart.setOnClickListener { start() }
        binding.btnStartMock.setOnClickListener { startMock() }
    }

    private fun start() {
        val sdkKey = binding.etSdkKey.text.toString().lowercase(Locale.getDefault())
        if (sdkKey.isBlank()) {
            Toast.makeText(requireContext(), getString(R.string.invalid_sdk_key_msg), Toast.LENGTH_SHORT).show()
        } else {
            // Initialize SDK
            // Normally this would be done elsewhere. Typically App Start.
            val configuration =
                MailchimpSdkConfiguration.Builder(requireContext(), sdkKey)
                    .isDebugModeEnabled(binding.cbxDebug.isChecked)
                    .isAutoTaggingEnabled(binding.cbxAutotag.isChecked)
                    .build()
            Mailchimp.initialize(requireContext(), configuration)

            goToHomeFragment()
        }
    }

    private fun startMock() {
        val configuration =
            MailchimpSdkConfiguration.Builder(requireContext(), "sdkkey-us1")
                .isDebugModeEnabled(binding.cbxDebug.isChecked)
                .isAutoTaggingEnabled(binding.cbxAutotag.isChecked)
                .build()
        val apiDependencies = MockApiImplementation()
        val mockInjector = object : MailchimpInjector(requireContext(), configuration) {
            override val audienceDependencies: AudienceDependencies by lazy {
                AudienceImplementation.initialize(
                    coreDependencies,
                    apiDependencies,
                    configuration,
                    override = true
                )
            }
            override val apiDependencies: ApiImplementation = apiDependencies
        }
        val mock = MockMailchimp(mockInjector)
        mock.initializeMock()
        MockMailchimp.setAudienceAsMock(mock)

        goToHomeFragment()
    }

    private fun goToHomeFragment() {
        val action = SetupFragmentDirections.actionSetupFragmentToHomeFragment()
        findNavController().navigate(action)
    }
}