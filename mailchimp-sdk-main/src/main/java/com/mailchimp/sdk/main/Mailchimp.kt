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

import android.annotation.SuppressLint
import android.content.Context
import com.mailchimp.sdk.audience.AudienceSdkContract
import com.mailchimp.sdk.core.MailchimpSdkConfiguration
import com.mailchimp.sdk.main.di.MailchimpInjector
import timber.log.Timber

/**
 * The Mailchimp Class manages all contact updates to your Mailchimp audience. This class operates
 * as a singleton. To start an instance call the [Mailchimp.initialize] method with the desired
 * configuration. To retrieve an instance of the Audience SDK call the [Mailchimp.sharedInstance]
 * method.
 */
open class Mailchimp protected constructor(
    private val injector: MailchimpInjector
) : AudienceSdkContract by injector.audienceDependencies.audienceSdkContract {

    companion object {

        @SuppressLint("StaticFieldLeak")
        private var INSTANCE: Mailchimp? = null

        /**
         * Initializes the audience SDK. If the SDK has already been initialized this method will
         * return the shared instance.
         *
         * Note: Once initialized you may call [Mailchimp.sharedInstance] to retrieve the shared
         * instance of the [Mailchimp].
         *
         * @param configuration The [MailchimpSdkConfiguration] to use for the SDK
         * @return The newly created instance of the [Mailchimp]
         */
        @JvmStatic
        fun initialize(context: Context, configuration: MailchimpSdkConfiguration): Mailchimp {
            if (INSTANCE == null) {
                INSTANCE =
                    Mailchimp(MailchimpInjector(context, configuration))
                INSTANCE!!.setupSdk(configuration.debugModeEnabled)
            }
            return INSTANCE!!
        }

        /**
         * Returns the shared instance of the [Mailchimp]
         *
         * Note: The SDK must have already been initialized using the [Mailchimp.initialize] method.
         *
         * @return The shared instance of the [Mailchimp]
         */
        @JvmStatic
        fun sharedInstance(): Mailchimp {
            if (INSTANCE == null) {
                throw java.lang.IllegalStateException("You must call initialize first")
            }
            return INSTANCE!!
        }
    }

    protected fun setupSdk(debugModeEnabled: Boolean) {
        if (debugModeEnabled && Timber.treeCount == 0) {
            Timber.plant(Timber.DebugTree())
        }
        Timber.d("SDK initialized")
    }
}
