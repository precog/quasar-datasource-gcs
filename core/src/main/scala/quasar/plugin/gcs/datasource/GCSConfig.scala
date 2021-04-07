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

import scala.Boolean
import quasar.blobstore.gcs.{Bucket, ServiceAccountConfig, Url}
import quasar.connector.DataFormat

import scala.util.{Either, Left, Right}

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
  val RedactedUri = Url("REDACTED")
  val EmptyUri = Url("")

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

  val BogusAuth: ServiceAccountConfig = ServiceAccountConfig(
    tokenUri = Url("https://oauth2.googleapis.com/token"),
    authProviderCertUrl = Url("https://www.googleapis.com/oauth2/v1/certs"),
    clientCertUrl = Url("https://www.googleapis.com/robot/v1/metadata/x509/read-bucket-sa%40project-name.iam.gserviceaccount.com"),
    authUri = Url("https://accounts.google.com/o/oauth2/auth"),
    privateKey = "1234567890",
    clientId = "1234567890",
    projectId = "project-name",
    privateKeyId = "1234567890",
    clientEmail = "read-bucket-sa@project-name.iam.gserviceaccount.com",
    accountType = "service-account"
  )
}
