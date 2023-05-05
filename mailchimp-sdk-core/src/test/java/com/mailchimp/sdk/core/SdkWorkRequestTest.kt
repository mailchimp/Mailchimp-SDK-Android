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
import androidx.work.BackoffPolicy
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.mailchimp.sdk.core.work.SdkWorkRequest
import com.mailchimp.sdk.core.work.SdkWorker
import org.junit.Assert.assertEquals
import org.junit.Test

class SdkWorkRequestTest {

    /**
     * Test to make sure our work request behavior does not change without knowing, that is possibly a breaking change
     * to our public API that requires documentation. It can cause breakage because assumptions made about how
     * work requests operate will leak to the documentation.
     *
     * If you are changing the verification of this test, consider the impact to the public API and documentation.
     */
    @Test
    fun testSdkWorkRequestBuildRequest() {
        val workRequest = TestableSdkWorkRequest()
        val oneTimeRequest = workRequest.buildRequest()

        assertEquals(60000, oneTimeRequest.workSpec.backoffDelayDuration)
        assertEquals(NetworkType.CONNECTED, oneTimeRequest.workSpec.constraints.requiredNetworkType)
        assertEquals(BackoffPolicy.EXPONENTIAL, oneTimeRequest.workSpec.backoffPolicy)
        assertEquals(workRequest.workParameters(), oneTimeRequest.workSpec.input)
    }

    @Test
    fun testGetUniqueWorkName() {
        val workRequest = TestableSdkWorkRequest()

        assertEquals("SdkWorker", workRequest.getUniqueWorkName())
    }

    @Test
    fun testGetPrecedingWorkNames() {
        val workRequest = TestableSdkWorkRequest()

        assertEquals(emptyList<String>(), workRequest.getPrecedingWorkNames())
    }
}

class TestableSdkWorkRequest : SdkWorkRequest() {
    override fun sdkWorkerClass(): Class<out SdkWorker> {
        return MockWorker::class.java
    }

    override fun workParameters(): Data {
        return workDataOf("kosmo" to "kramer")
    }
}

/*
    Not testing this class....just here for testability.
 */
class MockWorker(context: Context, workerParameters: WorkerParameters) : SdkWorker(context, workerParameters) {
    override fun performWork(): Response {
        // does nothing
        return Response.SUCCESS
    }
}