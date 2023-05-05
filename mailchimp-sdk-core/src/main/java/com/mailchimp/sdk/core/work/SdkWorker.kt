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

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import timber.log.Timber

/**
 * Base worker that should be used for all workers in SDK components. It is meant really for synchronous work to be done
 * in the {@link com.mailchimp.sdk.work.SdkWorker#performWork()} function. The call to perform work will be done typically
 * on a background thread, but will depend on WorkManger configuration.
 */
abstract class SdkWorker(context: Context, workParams: WorkerParameters) : Worker(context, workParams) {

    /**
     * Max number to retry this worker.
     */
    protected open val maxRetries = 5

    /**
     * The result code that will be used when the retry limit is reached.
     */
    protected open val onRetryLimitReachedResponse = Response.FAILURE_CONTINUE_CHAIN

    /**
     * The result code that will be used when a unhandled exception is thrown by the worker.
     */
    protected open val onErrorResponse = Response.RETRY

    final override fun doWork(): Result {
        var workResult: Response
        try {
            Timber.d("starting to perform work")
            workResult = performWork()
            Timber.d("finished work with result of %s", workResult)
        } catch (e: Exception) {
            Timber.d(e)
            workResult = onErrorResponse
        }

        if (runAttemptCount >= maxRetries && workResult == Response.RETRY) {
            workResult = onRetryLimitReachedResponse
        }

        return when (workResult) {
            Response.RETRY -> Result.retry()
            Response.FAILURE_END_CHAIN -> Result.failure()
            Response.FAILURE_CONTINUE_CHAIN -> Result.success()
            Response.SUCCESS -> Result.success()
        }
    }

    /**
     * Override this method to do your actual background processing.  This method is called on a
     * background thread - you are required to <b>synchronously</b> do your work and return the
     * {@link androidx.work.ListenableWorker.Result} from this method.  Once you return from this
     * method, the Worker is considered to have finished what its doing and will be destroyed.  If
     * you need to do your work asynchronously on a thread of your own choice, see
     * {@link ListenableWorker}.
     * <p>
     * A Worker is given a maximum of ten minutes to finish its execution and return a
     * {@link androidx.work.ListenableWorker.Result}.  After this time has expired, the Worker will
     * be signalled to stop.
     *
     * @return The {@link androidx.work.ListenableWorker.Result} of the computation; note that
     *         dependent work will not execute if you use
     *         {@link androidx.work.ListenableWorker.Result#failure()} or
     *         {@link androidx.work.ListenableWorker.Result#failure(Data)}
     */
    abstract fun performWork(): Response

    /**
     * Response that is specific to the SDK, not WorkManager
     */
    enum class Response {
        /**
         * Indicates that work completion failed, but should be retried.
         *
         * Retry the given worker after whatever the original backoff criteria and retry count is. If the retry count is
         * greater than the maximum, the task will be failed as #FAILURE_CONTINUE_CHAIN.
         *
         * See default max retry account at SdkWorker#maxRetries (which can be overridden).
         */
        RETRY,

        /**
         * Indicates that work completion failed, and should not be retried.
         *
         * If this worker is in a work continuation chain (unique work name), fail any tasks in the given chain as well.
         * Note: this fails future workers that are queued into the same work continuation until the failed worker is
         * pruned from WorkManager.
         */
        FAILURE_END_CHAIN,

        /**
         * Indicates that work completion failed, and should not be retried.
         *
         * If this worker is in a work continuation chain (unique work name), continue any tasks in the given chain as
         * well. New workers that are submitted after this will run, regardless of this failure.
         */
        FAILURE_CONTINUE_CHAIN,

        /**
         * Indicates that the worker successfully completed its work.
         */
        SUCCESS
    }
}