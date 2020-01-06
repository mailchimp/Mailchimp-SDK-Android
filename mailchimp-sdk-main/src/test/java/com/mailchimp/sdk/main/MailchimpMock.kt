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

package com.mailchimp.sdk.main

import com.mailchimp.sdk.main.di.MailchimpInjector

class MailchimpMock(audienceInjector: MailchimpInjector) : Mailchimp(audienceInjector) {
    companion object {
        private const val AUDIENCE_SDK_SINGLETON_PARAM_NAME = "INSTANCE"

        /**
         * This uses reflection to set the mockAudienceSdk as the one returned by AudienceSdk.sharedInstance()
         */
        fun setMock(mockMailchimp: MailchimpMock) {
            val field = Mailchimp::class.java.getDeclaredField(
                AUDIENCE_SDK_SINGLETON_PARAM_NAME
            )
            field.isAccessible = true
            field.set(null, mockMailchimp)
        }
    }

    /**
     * Call this after construction to initialize the SDK
     */
    fun initializeMock() {
        setupSdk(true)
    }
}