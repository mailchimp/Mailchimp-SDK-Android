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

package com.mailchimp.sdk.core

import com.mailchimp.sdk.core.di.CoreImplementation
import org.junit.Assert.assertNotNull
import org.junit.Test

class CoreImplementationTest {

    @Test
    fun testCanConstructCore() {
        val coreImplementation = CoreImplementation()

        // we cannot test accessing the dependencies because work manager does not play nicely with mocks
        assertNotNull(coreImplementation)
    }
}