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

package com.mailchimp.sdkdemo.mockapi

import com.mailchimp.sdk.api.di.ApiImplementation
import okhttp3.EventListener

class MockLocalApiImplementation(baseUrl: String, eventListener: EventListener) : ApiImplementation("irrelevant", baseUrl, eventListener, true)
