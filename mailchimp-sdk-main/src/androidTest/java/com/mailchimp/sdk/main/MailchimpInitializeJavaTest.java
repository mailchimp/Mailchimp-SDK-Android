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

package com.mailchimp.sdk.main;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.app.Application;
import android.content.Context;

import com.google.gson.Gson;
import com.mailchimp.sdk.api.SdkWebService;
import com.mailchimp.sdk.api.di.ApiDependencies;
import com.mailchimp.sdk.core.MailchimpSdkConfiguration;
import com.mailchimp.sdk.core.di.CoreDependencies;
import com.mailchimp.sdk.core.work.WorkProcessor;
import com.mailchimp.sdk.core.work.WorkStatusProvider;
import com.mailchimp.sdk.main.di.MailchimpInjector;

import org.junit.Test;

public class MailchimpInitializeJavaTest {

    @Test
    public void testMailchimpSdk() {
        Context mockContext = mock(Application.class);

        CoreDependencies mockCore = mock(CoreDependencies.class);
        ApiDependencies apiDependencies = mock(ApiDependencies.class);

        when(mockContext.getApplicationContext()).thenReturn(mockContext);

        MailchimpSdkConfiguration configuration =
                new MailchimpSdkConfiguration.Builder(mockContext, "sdkKey-us1").build();

        when(apiDependencies.getGson()).thenReturn(new Gson());
        when(apiDependencies.getSdkWebService()).thenReturn(mock(SdkWebService.class));
        when(mockCore.getWorkProcessor()).thenReturn(mock(WorkProcessor.class));
        when(mockCore.getWorkStatusProvider()).thenReturn(mock(WorkStatusProvider.class));

        MailchimpInjector injector =
                new MailchimpMockInjector(mockCore, apiDependencies, configuration);
        MailchimpMock mockSdk = new MailchimpMock(injector);
        MailchimpMock.Companion.setMock(mockSdk);

        assertNotNull(Mailchimp.sharedInstance());
    }
}
