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

package com.mailchimp.sdk.api

import okhttp3.tls.HandshakeCertificates
import okhttp3.tls.HeldCertificate

object SslHelper {

    private val localhostCertificate = HeldCertificate.Builder()
        .addSubjectAlternativeName("localhost")
        .build()

    private val serverCertificates = HandshakeCertificates.Builder()
        .heldCertificate(localhostCertificate)
        .build()

    private val clientCertificates = HandshakeCertificates.Builder()
        .addTrustedCertificate(localhostCertificate.certificate)
        .build()

    val serverSslSocketFactory = serverCertificates.sslSocketFactory()

    val clientSslSocketFactory = clientCertificates.sslSocketFactory()
    val clientTrustManager = clientCertificates.trustManager
}