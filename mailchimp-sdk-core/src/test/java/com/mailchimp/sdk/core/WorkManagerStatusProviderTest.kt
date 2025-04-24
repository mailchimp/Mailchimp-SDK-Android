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

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.work.Data
import androidx.work.WorkInfo
import androidx.work.workDataOf
import com.mailchimp.sdk.core.work.WorkManagerStatusProvider
import com.mailchimp.sdk.core.work.WorkProcessor
import com.mailchimp.sdk.core.work.WorkStatus
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mock
import org.mockito.MockitoAnnotations.openMocks
import org.mockito.kotlin.whenever
import java.util.UUID

class WorkManagerStatusProviderTest {

    @Mock
    private lateinit var processor: WorkProcessor

    @Rule
    @JvmField
    val rule: TestRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        openMocks(this)
    }

    @Test
    fun testGetStatusById() {
        val workId = UUID.randomUUID()
        val workInfo = WorkInfo(workId, WorkInfo.State.SUCCEEDED, setOf(), workDataOf(), Data.EMPTY, 0, 0)
        whenever(processor.getWorkById(workId)).thenReturn(workInfo)

        val statusProvider = WorkManagerStatusProvider(processor)
        val workStatus = statusProvider.getStatusById(workId)

        assertEquals(WorkStatus.FINISHED, workStatus)
    }

    @Test
    fun testGetStatusByIdLiveData() {
        val workId = UUID.randomUUID()
        val workInfoLiveData = MutableLiveData<WorkInfo>()
        workInfoLiveData.value = WorkInfo(workId, WorkInfo.State.SUCCEEDED, setOf(), workDataOf(), Data.EMPTY, 0, 0)
        whenever(processor.getWorkByIdLiveData(workId)).thenReturn(workInfoLiveData)

        val statusProvider = WorkManagerStatusProvider(processor)
        val workStatus = statusProvider.getStatusByIdLiveData(workId)

        workStatus.observeForever {
            assertEquals(WorkStatus.FINISHED, it)
        }
    }

    @Test
    fun testMappingsMapToCorrectStatus() {
        testMapping(WorkInfo.State.ENQUEUED, WorkStatus.QUEUED)
        testMapping(WorkInfo.State.RUNNING, WorkStatus.RUNNING)
        testMapping(WorkInfo.State.SUCCEEDED, WorkStatus.FINISHED)
        testMapping(WorkInfo.State.FAILED, WorkStatus.FAILED)
        testMapping(WorkInfo.State.BLOCKED, WorkStatus.QUEUED)
        testMapping(WorkInfo.State.CANCELLED, WorkStatus.FAILED)
    }

    private fun testMapping(workInfoState: WorkInfo.State, expectedStatus: WorkStatus) {
        val workId = UUID.randomUUID()
        val workInfo = WorkInfo(workId, workInfoState, setOf(), workDataOf(), Data.EMPTY, 0, 0)
        whenever(processor.getWorkById(workId)).thenReturn(workInfo)

        val statusProvider = WorkManagerStatusProvider(processor)
        val workStatus = statusProvider.getStatusById(workId)

        assertEquals(expectedStatus, workStatus)
    }
}