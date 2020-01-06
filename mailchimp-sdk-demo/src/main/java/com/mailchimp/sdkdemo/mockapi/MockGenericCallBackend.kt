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

import timber.log.Timber

class MockGenericCallBackend {

    private val callMap = mutableMapOf<String, MutableList<Any>>()

    fun addRequest(request: Any, tag: String) {
        val requestList = callMap.getOrPut(tag) { mutableListOf() }
        requestList.add(request)
        Timber.d("Request List: %s", requestList)
    }

    fun getRequests(tag: String): List<Any> {
        return callMap[tag] ?: emptyList()
    }

    fun clearRequests(tag: String) {
        callMap.remove(tag)
    }
}