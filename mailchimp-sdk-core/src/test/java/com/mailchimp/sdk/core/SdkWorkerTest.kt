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
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.mailchimp.sdk.core.work.SdkWorker
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations.initMocks
import java.lang.Exception
import java.lang.IllegalStateException

class SdkWorkerTest {

    @Mock
    private lateinit var context: Context
    @Mock
    private lateinit var workParams: WorkerParameters

    @Before
    fun setup() {
        initMocks(this)
    }

    @Test
    fun testDoWorkSuccess() {
        val worker = TestSdkWorker(SdkWorker.Response.SUCCESS, context, workParams)

        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.success(), result)
    }

    @Test
    fun testDoWorkMappings() {
        testMapping(SdkWorker.Response.RETRY, ListenableWorker.Result.retry())
        testMapping(SdkWorker.Response.FAILURE_END_CHAIN, ListenableWorker.Result.failure())
        testMapping(SdkWorker.Response.FAILURE_CONTINUE_CHAIN, ListenableWorker.Result.success())
        testMapping(SdkWorker.Response.SUCCESS, ListenableWorker.Result.success())
    }

    @Test
    fun testDoWorkExceptionThrown() {
        val worker = TestSdkWorkerException(IllegalStateException("something went wrong"), context, workParams)

        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.retry(), result)
    }

    @Test
    fun testRetryLimitHit() {
        whenever(workParams.runAttemptCount).thenReturn(10)

        val worker = TestSdkWorker(SdkWorker.Response.RETRY, context, workParams)
        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.success(), result)
    }

    private fun testMapping(response: SdkWorker.Response, expectedResult: ListenableWorker.Result) {
        val worker = TestSdkWorker(response, context, workParams)
        val result = worker.doWork()
        assertEquals(expectedResult, result)
    }
}

class TestSdkWorker(private val response: Response, context: Context, workParams: WorkerParameters) : SdkWorker(context, workParams) {
    override fun performWork(): Response {
        return response
    }
}
class TestSdkWorkerException(private val exception: Exception, context: Context, workParams: WorkerParameters) : SdkWorker(context, workParams) {
    override fun performWork(): Response {
        throw exception
    }
}
