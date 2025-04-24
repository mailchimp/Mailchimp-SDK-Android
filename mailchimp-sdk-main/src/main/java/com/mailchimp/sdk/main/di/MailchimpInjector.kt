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

package com.mailchimp.sdk.main.di

import android.content.Context
import com.mailchimp.sdk.api.di.ApiDependencies
import com.mailchimp.sdk.api.di.ApiImplementation
import com.mailchimp.sdk.audience.di.AudienceDependencies
import com.mailchimp.sdk.audience.di.AudienceImplementation
import com.mailchimp.sdk.core.MailchimpSdkConfiguration
import com.mailchimp.sdk.core.di.CoreDependencies
import com.mailchimp.sdk.core.di.CoreImplementation

open class MailchimpInjector(private val context: Context, private val configuration: MailchimpSdkConfiguration) {

    open val audienceDependencies: AudienceDependencies by lazy {
        AudienceImplementation.initialize(coreDependencies, apiDependencies, configuration)
    }
    open val coreDependencies: CoreDependencies by lazy {
        CoreImplementation(context)
    }
    open val apiDependencies: ApiDependencies by lazy {
        ApiImplementation(
            configuration.sdkKey,
            configuration.baseUrl,
            configuration.okHttpEventListener,
            configuration.debugModeEnabled
        )
    }
}