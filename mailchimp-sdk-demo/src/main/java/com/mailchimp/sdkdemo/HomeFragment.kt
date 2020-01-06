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
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment : Fragment() {

    private lateinit var pagerAdapter: HomeFragmentPagerAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pagerAdapter = HomeFragmentPagerAdapter(fragmentManager!!)
        view_pager_FH.adapter = pagerAdapter
        tab_layout_FH.setupWithViewPager(view_pager_FH)

    }

}

class HomeFragmentPagerAdapter(fragmentManager: FragmentManager) :
    FragmentStatePagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    override fun getItem(position: Int): Fragment {
        val tab = HomeTabs.values()[position]
        return when (tab) {
            HomeTabs.ADD_CONTACT -> AddContactFragment()
            HomeTabs.ADD_EVENT -> AddEventFragment()
        }
    }

    override fun getCount(): Int {
        return HomeTabs.values().size
    }

    override fun getPageTitle(position: Int): CharSequence {
        return HomeTabs.values()[position].tabName
    }

}

enum class HomeTabs(val tabName: String) {
    ADD_CONTACT("Add Contact"),
    ADD_EVENT("Add Event")
}