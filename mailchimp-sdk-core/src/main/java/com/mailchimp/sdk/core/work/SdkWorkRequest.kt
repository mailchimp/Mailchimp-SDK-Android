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

import androidx.work.*
import java.util.concurrent.TimeUnit

abstract class SdkWorkRequest {

    protected open val requireInternet = true
    protected open val backoffPolicy = BackoffPolicy.EXPONENTIAL
    protected open val backoffDelay: Long = 1
    protected open val backoffDelayUnits = TimeUnit.MINUTES

    /**
     * Provide the worker class that will be performing the work for this request.
     *
     * @return the class of the worker for this request.
     */
    abstract fun sdkWorkerClass(): Class<out SdkWorker>

    /**
     * Override this method to specify input data for the supplied worker class.
     *
     * @return input data for the worker.
     */
    open fun workParameters(): Data? {
        return null
    }

    /**
     * Override this method to specify the "unique name" of the work that is being performed. This name is used to
     * queue work sequentially. Meaning that a SdkWorker that has the same unique name as another SdkWorker will be
     * sequentially queued one after the other. For example: Worker A with unique name "Task", Worker B with unique
     * name "TaskB" and Worker C with unique name "Task" will cause worker A and C to be queued sequentially and B to be
     * queued independently and run parallel (if the work manager configuration has a multi-thread executor configured,
     * which is the default).
     *
     * @return The unique name of the work being performed.
     */
    open fun getUniqueWorkName(): String {
        return "SdkWorker"
    }

    /**
     * Override this method to specify any work names that this SdkWorker is dependent on. If there is active work queued
     * or running when this worker is submitted, it will be queued using that unique name, meaning this SdkWorker will
     * execute sequentially after any work active with any of the provided names.
     *
     * You can specify multiple names, but only the first match will be used. (you cannot chain work to multiple independent
     * workers).
     *
     * @return A list of unique work names that this SdkWorker is dependent on.
     */
    open fun getPrecedingWorkNames(): List<String> {
        return emptyList()
    }

    /**
     * Builds a one time work request that represents this SDK work request.
     */
    internal fun buildRequest(): OneTimeWorkRequest {
        val constraints = if (requireInternet) {
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
        } else {
            Constraints.Builder().build()
        }

        val requestBuilder =
            OneTimeWorkRequest.Builder(sdkWorkerClass())
                .setConstraints(constraints)
                .setBackoffCriteria(backoffPolicy, backoffDelay, backoffDelayUnits)

        workParameters()?.let {
            requestBuilder.setInputData(it)
        }

        return requestBuilder.build()
    }
}