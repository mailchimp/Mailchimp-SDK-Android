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

package com.mailchimp.sdkdemo

import android.content.Context
import androidx.work.WorkerParameters
import com.mailchimp.sdk.core.work.SdkWorkRequest
import com.mailchimp.sdk.core.work.SdkWorker

class TestWorker(appContext: Context, workParams: WorkerParameters) : SdkWorker(appContext, workParams) {

    override fun performWork(): Response {
        return Response.SUCCESS
    }
}

class TestWorkRequest1(private val email: String) : SdkWorkRequest() {

    companion object {
        fun getUniqueName(email: String): String {
            return "TestWorkRequest1:$email"
        }
    }

    override fun sdkWorkerClass(): Class<out SdkWorker> {
        return TestWorker::class.java
    }

    override fun getUniqueWorkName(): String {
        return getUniqueName(email)
    }

    override fun getPrecedingWorkNames(): List<String> {
        return listOf(TestWorkRequest2.getUniqueName(email))
    }
}

class TestWorkRequest2(private val email: String) : SdkWorkRequest() {

    companion object {
        fun getUniqueName(email: String): String {
            return "TestWorkRequest2:$email"
        }
    }

    override fun sdkWorkerClass(): Class<out SdkWorker> {
        return TestWorker::class.java
    }

    override fun getUniqueWorkName(): String {
        return getUniqueName(email)
    }

    override fun getPrecedingWorkNames(): List<String> {
        return listOf(TestWorkRequest1.getUniqueName(email))
    }
}
