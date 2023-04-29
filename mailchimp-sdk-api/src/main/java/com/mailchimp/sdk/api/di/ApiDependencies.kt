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

package com.mailchimp.sdk.api.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.mailchimp.sdk.api.ApiAuthInterceptor
import com.mailchimp.sdk.api.RetrofitBuilder
import com.mailchimp.sdk.api.SdkWebService
import com.mailchimp.sdk.api.gson.BasicGsonTypeDecoder
import com.mailchimp.sdk.api.gson.GsonInterfaceAdapter
import com.mailchimp.sdk.api.model.mergefields.Address
import com.mailchimp.sdk.api.model.mergefields.MergeFieldValue
import com.mailchimp.sdk.api.model.mergefields.StringMergeFieldValue
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

interface ApiDependencies {
    val sdkWebService: SdkWebService
    val gson: Gson
}

open class ApiImplementation(private val sdkKey: String, private val shard: String, private val isDebug: Boolean) :
    ApiDependencies {
    override val gson: Gson by lazy {
        GsonBuilder().registerTypeAdapter(
            MergeFieldValue::class.java,
            mergeFieldValueTypeAdapter
        ).create()
    }
    override val sdkWebService: SdkWebService by lazy { retrofit.create(SdkWebService::class.java) }

    // WebService Dependencies
    private val okHttpLoggingInterceptor by lazy {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        interceptor
    }
    private val apiAuthorizationInterceptor by Dependency { ApiAuthInterceptor(sdkKey) }
    private val okHttpClient by lazy {
        val builder = OkHttpClient.Builder()
        builder.addInterceptor(apiAuthorizationInterceptor)
        if (isDebug) {
            builder.addInterceptor(okHttpLoggingInterceptor)
        }
        builder.build()
    }

    private val gsonConverterFactory: GsonConverterFactory by lazy { GsonConverterFactory.create(gson) }
    private val retrofit: Retrofit by lazy {
        RetrofitBuilder().createInstance(
            shard,
            gsonConverterFactory,
            okHttpClient
        )
    }

    // Gson Dependencies
    private val mergeFieldValueTypeAdapter by Dependency { GsonInterfaceAdapter<MergeFieldValue>(mergeFieldTypeDecoder) }
    private val mergeFieldTypeDecoder by Dependency {
        BasicGsonTypeDecoder.Builder("type")
            .addMapping("string", StringMergeFieldValue::class.java)
            .addMapping("address", Address::class.java)
            .build()
    }
}

class Dependency<T>(private val getter: () -> T) : ReadOnlyProperty<Any, T> {
    override fun getValue(thisRef: Any, property: KProperty<*>): T {
        return getter()
    }
}