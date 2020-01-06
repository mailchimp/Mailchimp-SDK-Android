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

package com.mailchimp.sdk.api

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class ApiAuthInterceptor(private val apiKey: String) : Interceptor {

    companion object {

        private const val AUTHORIZATION_PREFIX = "apikey "
        private const val AUTHORIZATION_HEADER_KEY = "Authorization"
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request =
            chain
                .request()
                .newBuilder()
                .header(AUTHORIZATION_HEADER_KEY, AUTHORIZATION_PREFIX + apiKey)
                .build()

        return chain.proceed(request)
    }
}