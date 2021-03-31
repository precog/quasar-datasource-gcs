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

import java.net.URI
import scala.Boolean
import scala.util.{Either, Left, Right}

import quasar.blobstore.gcs.{Bucket, ServiceAccountConfig}
import quasar.connector.DataFormat

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
}
