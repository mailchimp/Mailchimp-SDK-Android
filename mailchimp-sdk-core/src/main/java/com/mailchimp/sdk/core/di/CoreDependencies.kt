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

package com.mailchimp.sdk.core.di

import android.content.Context
import androidx.work.WorkManager
import com.mailchimp.sdk.api.di.Dependency
import com.mailchimp.sdk.core.work.WorkManagerStatusProvider
import com.mailchimp.sdk.core.work.WorkProcessor
import com.mailchimp.sdk.core.work.WorkStatusProvider

interface CoreDependencies {
    val workProcessor: WorkProcessor
    val workStatusProvider: WorkStatusProvider
}

class CoreImplementation(context: Context) : CoreDependencies {
    override val workProcessor by lazy { WorkProcessor(workManager) }
    override val workStatusProvider by Dependency { WorkManagerStatusProvider(workProcessor) }

    private val workManager by lazy { WorkManager.getInstance(context) }
}