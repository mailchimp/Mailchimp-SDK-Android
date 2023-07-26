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

package com.mailchimp.sdk.audience.di

import com.mailchimp.sdk.api.di.ApiDependencies
import com.mailchimp.sdk.audience.AudienceSdkContract
import com.mailchimp.sdk.audience.WorkManagerAudienceSdk
import com.mailchimp.sdk.core.MailchimpSdkConfiguration
import com.mailchimp.sdk.core.di.CoreDependencies

interface AudienceDependencies {
    val audienceSdkContract: AudienceSdkContract
}

class AudienceImplementation private constructor(
    coreDependencies: CoreDependencies,
    apiDependencies: ApiDependencies,
    private val mailchimpSdkConfiguration: MailchimpSdkConfiguration
) : AudienceDependencies,
    CoreDependencies by coreDependencies,
    ApiDependencies by apiDependencies {

    override val audienceSdkContract by lazy {
        WorkManagerAudienceSdk(workProcessor, gson, mailchimpSdkConfiguration, workStatusProvider)
    }

    /*
     * This only exists because we can override the WorkManager Worker Factory because it might be overridden by implementors of the SDK.
     * This means that we can't inject dependencies into workers and instead must use a service locator. So AudienceImplementation is being
     * used in that way. It's not pretty but it works.
     */
    companion object {
        private var instance: AudienceImplementation? = null

        /* Don't use this unless you're sure you should be */
        fun initialize(
            coreDependencies: CoreDependencies,
            apiDependencies: ApiDependencies,
            mailchimpSdkConfiguration: MailchimpSdkConfiguration,
            override: Boolean = false
        ): AudienceImplementation {
            synchronized(this) {
                if (instance == null || override) {
                    instance = AudienceImplementation(coreDependencies, apiDependencies, mailchimpSdkConfiguration)
                }
                return instance!!
            }
        }

        /* Don't use this unless you're sure you should be */
        fun sharedInstance(): AudienceImplementation? {
            return instance
        }
    }
}
