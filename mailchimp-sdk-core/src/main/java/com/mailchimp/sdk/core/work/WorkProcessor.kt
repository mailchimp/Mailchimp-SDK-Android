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
import androidx.work.ExistingWorkPolicy
import androidx.work.Operation
import androidx.work.WorkInfo
import androidx.work.WorkManager
import java.util.UUID

open class WorkProcessor(private val workManager: WorkManager) {

    /**
     * Submits a work request to the work manager. This will internally make sure that the work request is queued properly.
     *
     * @param workRequest the SDK work request that will be submitted to the work manager.
     *
     * @return a pair that includes the work request ID and the operation representing submission of the work request.
     */
    fun submitWork(workRequest: SdkWorkRequest): Pair<UUID, Operation> {
        val request = workRequest.buildRequest()

        var activePrecedingWorkName: String? = null
        val precedentNames = workRequest.getPrecedingWorkNames()
        for (name in precedentNames) {
            val precedingWork = workManager.getWorkInfosForUniqueWork(name).get()
            if (precedingWork.any { workInfo -> !workInfo.state.isFinished }) {
                activePrecedingWorkName = name
            }
        }

        val uniqueWorkName = activePrecedingWorkName ?: workRequest.getUniqueWorkName()
        val operation = workManager.enqueueUniqueWork(uniqueWorkName, ExistingWorkPolicy.APPEND, request)
        return Pair(request.id, operation)
    }

    /**
     * Get work info by the work request identifier.
     *
     * @param id the work request id.
     *
     * @return WorkInfo which represents the status of the work at the time of call.
     */
    fun getWorkById(id: UUID): WorkInfo {
        return workManager.getWorkInfoById(id).get()
    }

    /**
     * Get a livedata that represents work info, by the work request identifier.
     *
     * @param id the work request id.
     *
     * @return A livedata,  WorkInfo that will update at each status change.
     */
    fun getWorkByIdLiveData(id: UUID): LiveData<WorkInfo> {
        return workManager.getWorkInfoByIdLiveData(id)
    }

    /**
     * Get a list of work info by the unique name.
     *
     * @param name the unique name of the work you are trying to get status for.
     *
     * @return a list of work info with the given unique work name.
     */
    fun getWorkByName(name: String): List<WorkInfo> {
        return workManager.getWorkInfosForUniqueWork(name).get()
    }

    /**
     * Get a livedata that represents list of work info by the unique name.
     *
     * @param name the unique name of the work you are trying to get status for.
     *
     * @return a livedata, which will be updated everytime a WorkInfo's status changes
     */
    fun getWorkByNameLiveData(name: String): LiveData<List<WorkInfo>> {
        return workManager.getWorkInfosForUniqueWorkLiveData(name)
    }
}