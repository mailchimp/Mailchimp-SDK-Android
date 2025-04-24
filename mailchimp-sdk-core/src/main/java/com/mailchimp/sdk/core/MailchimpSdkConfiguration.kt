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

import android.content.Context
import okhttp3.EventListener

/**
 * The MailchimpSdkConfiguration class configures the [Mailchimp] settings.
 * See [MailchimpSdkConfiguration.Builder] for a more in depth description.
 */
class MailchimpSdkConfiguration private constructor(
    val sdkKey: String,
    val baseUrl: String,
    val context: Context,
    val debugModeEnabled: Boolean = false,
    val autoTaggingEnabled: Boolean = true,
    val okHttpEventListener: EventListener? = null
) {

    /**
     * [MailchimpSdkConfiguration] Builder. Constructor parameters are required values. All other values
     * are optional.
     *
     * @property context Application [Context] is required for our Job Runner. If activity context
     * is passed in, we will pull the application context from it.
     * @property sdkKey The SDK key which can be generated on the mailchimp website //TODO update this
     */
    class Builder(context: Context, private val sdkKey: String) {

        companion object {
            private const val BASE_SDK_URL_FORMAT = "https://%s.api.mailchimp.com/clientapi/1.0/"
        }

        private var isDebugModeEnabled: Boolean = false
        private var isAutoTaggingEnabled: Boolean = true
        private var okHttpEventListener: EventListener? = null
        private val context: Context = context.applicationContext

        /**
         * Enables debug mode functionality including additional logging.
         *
         * Note: This is disabled by default.
         *
         * @param enabled Boolean indicating whether to enable or disable debug functionality
         */
        fun isDebugModeEnabled(enabled: Boolean) = apply { this.isDebugModeEnabled = enabled }

        /**
         * When enabled, Created or Updated users will be automatically tagged with relevant data.
         *
         * Note: This is enabled by default.
         *
         * @param enabled Boolean indicating whether to enable or disable auto tagging
         */
        fun isAutoTaggingEnabled(enabled: Boolean) = apply { this.isAutoTaggingEnabled = enabled }

        /**
         * Receives all analytics events from OkHttp client.
         *
         * @param eventListener EventListener that receives metric events to monitor the
         * quantity, size, and duration HTTP calls.  Events are invoked on the thread that triggered
         * the event (source: okhttp3.EventListener.canceled() documentation).
         */
        fun okHttpEventListener(eventListener: EventListener) = apply { this.okHttpEventListener = eventListener }

        fun build(): MailchimpSdkConfiguration {

            // SDK key format is "key-shard"
            val keyAndShard = this.sdkKey.split("-")
            if (keyAndShard.size != 2) {
                throw IllegalArgumentException("The provided SDK key is invalid. It should contain 1 dash.")
            }

            val key = keyAndShard.first()
            val shard = String.format(BASE_SDK_URL_FORMAT, keyAndShard.last())

            return MailchimpSdkConfiguration(
                key,
                shard,
                this.context,
                isDebugModeEnabled,
                isAutoTaggingEnabled,
                okHttpEventListener
            )
        }
    }
}