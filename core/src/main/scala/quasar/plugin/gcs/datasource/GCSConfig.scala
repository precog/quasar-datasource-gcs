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

import scala.util.Either

import argonaut.Json
import quasar.blobstore.gcs.{Bucket, GoogleAuthConfig}
import quasar.connector.DataFormat

final case class GCSConfig(gac: GoogleAuthConfig, bucket: Bucket, format: DataFormat) {
    def sanitize: GCSConfig = scala.Predef.???
    def asJson: Json = scala.Predef.???
    def reconfigureNonSensitive(c: GCSConfig): Either[GCSConfig, GCSConfig] = scala.Predef.???
}
