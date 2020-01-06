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

package com.mailchimp.sdk.core.work

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.work.WorkInfo
import java.util.*

interface WorkStatusProvider {
    /**
     * Returns the [WorkStatus] of the given job.
     *
     * @param uuid The [UUID] of the job to be polled.
     * @return the work status of the job.
     */
    fun getStatusById(uuid: UUID): WorkStatus

    /**
     * Returns a [LiveData] that emits the status of the given job.
     *
     * @param uuid The [UUID] of the job to be polled.
     * @return a [LiveData] corresponding to the supplied job
     */
    fun getStatusByIdLiveData(uuid: UUID): LiveData<WorkStatus>
}

class WorkManagerStatusProvider(private val workProcessor: WorkProcessor) : WorkStatusProvider {

    override fun getStatusById(uuid: UUID): WorkStatus {
        val workInfo = workProcessor.getWorkById(uuid)
        return mapInfoToWorkStatus(workInfo)
    }

    override fun getStatusByIdLiveData(uuid: UUID): LiveData<WorkStatus> {
        val workInfoLiveData = workProcessor.getWorkByIdLiveData(uuid)
        return Transformations.map(workInfoLiveData, this::mapInfoToWorkStatus)
    }

    private fun mapInfoToWorkStatus(workInfo: WorkInfo): WorkStatus {
        return when (workInfo.state) {
            WorkInfo.State.ENQUEUED -> WorkStatus.QUEUED
            WorkInfo.State.RUNNING -> WorkStatus.RUNNING
            WorkInfo.State.SUCCEEDED -> WorkStatus.FINISHED
            WorkInfo.State.FAILED -> WorkStatus.FAILED
            WorkInfo.State.BLOCKED -> WorkStatus.QUEUED
            WorkInfo.State.CANCELLED -> WorkStatus.FAILED
        }
    }
}

/**
 * WorkStatus represents the possible status for a job.
 */
enum class WorkStatus {
    /**
     * The Queued Status is used to indicate that the job is waiting on other jobs to complete before starting.
     */
    QUEUED,
    /**
     * The Running Status is used to indicate that the job is currently in progress.
     */
    RUNNING,
    /**
     * The Finished Status is used to indicate that the job completed and encountered no errors.
     */
    FINISHED,
    /**
     * The Failed Status is used to indicate that the job encountered an error and may or may not have had the intended effect.
     */
    FAILED
}