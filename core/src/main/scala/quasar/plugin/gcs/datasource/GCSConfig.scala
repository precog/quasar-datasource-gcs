/*
 * Copyright 2020 Precog Data
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package quasar.plugin.gcs.datasource

import scala.{Boolean, StringContext}
import scala.Predef.String

import quasar.blobstore.gcs.{Bucket, ServiceAccountConfig}
import quasar.connector.DataFormat

import argonaut._, Argonaut._

import cats.implicits._

import scala.util.{Either, Left, Right}

import java.net.{URI, URISyntaxException}


final case class GCSConfig(
    auth: ServiceAccountConfig,
    bucket: Bucket,
    format: DataFormat) {

  import GCSConfig._

  def sanitize: GCSConfig = copy(auth = SanitizedAuth)

  def isSensitive: Boolean = auth != EmptyAuth

  def reconfigureNonSensitive(patch: GCSConfig): Either[GCSConfig, GCSConfig] =
    if (patch.isSensitive)
      Left(patch.sanitize)
    else
      Right(copy(bucket = patch.bucket, format = patch.format))
}

object GCSConfig {
  val Redacted = "<REDACTED>"
  val RedactedUri = URI.create("REDACTED")
  val EmptyUri = URI.create("")

  val SanitizedAuth: ServiceAccountConfig = ServiceAccountConfig(
    tokenUri = RedactedUri,
    authProviderCertUrl = RedactedUri,
    clientCertUrl = RedactedUri,
    authUri = RedactedUri,
    privateKey = Redacted,
    clientId = Redacted,
    projectId = Redacted,
    privateKeyId = Redacted,
    clientEmail = Redacted,
    accountType = Redacted
  )

  val EmptyAuth: ServiceAccountConfig = ServiceAccountConfig(
    tokenUri = EmptyUri,
    authProviderCertUrl = EmptyUri,
    clientCertUrl = EmptyUri,
    authUri = EmptyUri,
    privateKey = "",
    clientId = "",
    projectId = "",
    privateKeyId = "",
    clientEmail = "",
    accountType = ""
  )

  implicit val uriCodecJson: CodecJson[URI] =
    CodecJson(
      uri => Json.jString(uri.toString),
      c => for {
        uriStr <- c.jdecode[String]
        uri0 = Either.catchOnly[URISyntaxException](new URI(uriStr))
        uri <- uri0.fold(
          ex => DecodeResult.fail(s"Invalid URI: ${ex.getMessage}", c.history),
          DecodeResult.ok(_))
      } yield uri)

  implicit val serviceAccountConfigCodecJson: CodecJson[ServiceAccountConfig] = 
    casecodec10[URI,URI, String, String, URI, URI, String, String, String, String, ServiceAccountConfig](
      (tokenUri,
      authProviderCertUrl,
      privateKey,
      clientId,
      clientCertUrl,
      authUri,
      projectId,
      privateKeyId,
      clientEmail,
      accountType) => ServiceAccountConfig(
        tokenUri = tokenUri,
        authProviderCertUrl = authProviderCertUrl,
        privateKey = privateKey,
        clientId = clientId,
        clientCertUrl = clientCertUrl,
        authUri = authUri,
        projectId = projectId,
        privateKeyId = privateKeyId,
        clientEmail = clientEmail,
        accountType = accountType),
      sac => 
        (sac.tokenUri, 
        sac.authProviderCertUrl,
        sac.privateKey,
        sac.clientId,
        sac.clientCertUrl,
        sac.authUri,
        sac.projectId,
        sac.privateKeyId,
        sac.clientEmail,
        sac.accountType).some)(
          "token_uri",
          "auth_provider_x509_cert_url",
          "private_key",
          "client_id",
          "client_x509_cert_url",
          "auth_uri",
          "project_id",
          "private_key_id",
          "client_email",
          "type")

  implicit val gbqConfigCodecJson: CodecJson[GCSConfig] =
    casecodec3[ServiceAccountConfig, Bucket, DataFormat, GCSConfig](
      (auth, bucket, format) => GCSConfig(auth, bucket, format),
      gbqc => (gbqc.auth, gbqc.bucket, gbqc.format).some)("auth", "bucket", "format")

  implicit val bucketCodecJson: CodecJson[Bucket] = 
    casecodec1(Bucket.apply, Bucket.unapply)("value")
}
