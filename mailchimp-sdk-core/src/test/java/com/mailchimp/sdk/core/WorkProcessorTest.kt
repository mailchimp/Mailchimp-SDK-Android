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
import androidx.lifecycle.LiveData
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.Operation
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.google.common.util.concurrent.ListenableFuture
import com.mailchimp.sdk.core.work.SdkWorkRequest
import com.mailchimp.sdk.core.work.SdkWorker
import com.mailchimp.sdk.core.work.WorkProcessor
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.MockitoAnnotations.openMocks
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.UUID
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit

class WorkProcessorTest {

    @Mock
    private lateinit var workManager: WorkManager

    @Before
    fun setup() {
        openMocks(this)
    }

    @Test
    fun testSubmitWorkWithPrecedingRunningTask() {
        val workId = UUID.randomUUID()
        val workInfo = WorkInfo(workId, WorkInfo.State.RUNNING, setOf(), workDataOf(), Data.EMPTY, 0, 0)
        val future = TestableListenableFuture(listOf(workInfo))

        whenever(workManager.getWorkInfosForUniqueWork(anyString())).thenReturn(future)

        val workProcessor = WorkProcessor(workManager)
        val workRequest = TestWorkRequest1("test@test.com")
        val expectedQueueName = workRequest.getPrecedingWorkNames()[0]

        val operation = mock<Operation>()

        // verifies the preceding work name of this request is used (not the unique work name)
        whenever(
            workManager.enqueueUniqueWork(
                eq(expectedQueueName),
                eq(ExistingWorkPolicy.APPEND),
                any<OneTimeWorkRequest>()
            )
        )
            .thenReturn(operation)

        val result = workProcessor.submitWork(workRequest)
        assertEquals(operation, result.second)
    }

    @Test
    fun testSubmitWorkWithPrecedingFinishedTask() {
        val workId = UUID.randomUUID()
        val workInfo = WorkInfo(workId, WorkInfo.State.SUCCEEDED, setOf(), workDataOf(), Data.EMPTY, 0, 0)
        val future = TestableListenableFuture(listOf(workInfo))

        whenever(workManager.getWorkInfosForUniqueWork(anyString())).thenReturn(future)

        val workProcessor = WorkProcessor(workManager)
        val workRequest = TestWorkRequest1("test@test.com")
        val expectedQueueName = workRequest.getUniqueWorkName()

        val operation = mock<Operation>()

        // verifies the unique work name of this request is used (not the preceding work name)
        whenever(
            workManager.enqueueUniqueWork(
                eq(expectedQueueName),
                eq(ExistingWorkPolicy.APPEND),
                any<OneTimeWorkRequest>()
            )
        )
            .thenReturn(operation)

        val result = workProcessor.submitWork(workRequest)
        assertEquals(operation, result.second)
    }

    @Test
    fun testSubmitWorkWithNoPrecedingWork() {
        val future = TestableListenableFuture(emptyList<WorkInfo>())

        whenever(workManager.getWorkInfosForUniqueWork(anyString())).thenReturn(future)

        val workProcessor = WorkProcessor(workManager)
        val workRequest = TestWorkRequest1("test@test.com")
        val expectedQueueName = workRequest.getUniqueWorkName()

        val operation = mock<Operation>()

        // verifies the unique work name of this request is used (not the preceding work name)
        whenever(
            workManager.enqueueUniqueWork(
                eq(expectedQueueName),
                eq(ExistingWorkPolicy.APPEND),
                any<OneTimeWorkRequest>()
            )
        )
            .thenReturn(operation)

        val result = workProcessor.submitWork(workRequest)
        assertEquals(operation, result.second)
    }

    @Test
    fun testGetWorkByNameLiveData() {
        val mockData = mock<LiveData<List<WorkInfo>>>()
        whenever(workManager.getWorkInfosForUniqueWorkLiveData("test")).thenReturn(mockData)

        val workProcessor = WorkProcessor(workManager)
        val result = workProcessor.getWorkByNameLiveData("test")

        assertEquals(mockData, result)
    }

    @Test
    fun testGetWorkByName() {
        val mockFuture = mock<ListenableFuture<List<WorkInfo>>>()
        val mockData = mock<List<WorkInfo>>()
        whenever(mockFuture.get()).thenReturn(mockData)
        whenever(workManager.getWorkInfosForUniqueWork("test")).thenReturn(mockFuture)

        val workProcessor = WorkProcessor(workManager)
        val result = workProcessor.getWorkByName("test")

        assertEquals(mockData, result)
    }

    @Test
    fun testGetWorkByIdLiveData() {
        val mockData = mock<LiveData<WorkInfo>>()
        val workId = UUID.randomUUID()

        whenever(workManager.getWorkInfoByIdLiveData(workId)).thenReturn(mockData)

        val workProcessor = WorkProcessor(workManager)
        val result = workProcessor.getWorkByIdLiveData(workId)

        assertEquals(mockData, result)
    }

    @Test
    fun testGetWorkById() {
        val mockFuture = mock<ListenableFuture<WorkInfo?>>()
        val mockData = mock<WorkInfo>()
        val workId = UUID.randomUUID()

        whenever(mockFuture.get()).thenReturn(mockData)
        whenever(workManager.getWorkInfoById(workId)).thenReturn(mockFuture)

        val workProcessor = WorkProcessor(workManager)
        val result = workProcessor.getWorkById(workId)

        assertEquals(mockData, result)
    }

    class TestableListenableFuture<T>(private val testData: T) : ListenableFuture<T> {
        override fun addListener(listener: Runnable, executor: Executor) {
        }

        override fun isDone(): Boolean {
            return true
        }

        override fun get(): T {
            return testData
        }

        override fun get(timeout: Long, unit: TimeUnit?): T {
            return testData
        }

        override fun cancel(mayInterruptIfRunning: Boolean): Boolean {
            return true
        }

        override fun isCancelled(): Boolean {
            return false
        }
    }

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
}