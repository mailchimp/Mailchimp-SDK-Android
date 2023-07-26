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
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.mailchimp.sdkdemo.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    /**
     * This property is only valid between onCreateView and onDestroyView.
     */
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentHomeBinding.inflate(inflater, container, false).let {
            _binding = it
            it.root
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewPagerFH.adapter = HomeFragmentPagerAdapter(this)
        TabLayoutMediator(binding.tabLayoutFH, binding.viewPagerFH) { tab, position ->
            tab.text = HomeFragmentPagerAdapter.HomeTabs.values()[position].tabName
        }.attach()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private class HomeFragmentPagerAdapter(fragment: Fragment) :
        FragmentStateAdapter(fragment) {

        enum class HomeTabs(val tabName: String) {
            ADD_CONTACT("Add Contact"),
            ADD_EVENT("Add Event")
        }

        override fun getItemCount(): Int {
            return HomeTabs.values().size
        }

        override fun createFragment(position: Int): Fragment {
            return when (HomeTabs.values()[position]) {
                HomeTabs.ADD_CONTACT -> AddContactFragment()
                HomeTabs.ADD_EVENT -> AddEventFragment()
            }
        }

    }
}
