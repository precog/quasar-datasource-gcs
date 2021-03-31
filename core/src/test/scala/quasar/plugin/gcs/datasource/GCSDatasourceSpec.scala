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

import scala.Predef.String

import quasar.blobstore.gcs.{Bucket, ServiceAccountConfig}
import quasar.contrib.scalaz.MonadError_
import quasar.connector.{DataFormat, ResourceError}
import quasar.connector.datasource.LightweightDatasourceModule
import quasar.physical.blobstore.BlobstoreDatasourceSpec

import argonaut._, Argonaut._

import cats.effect.{IO, Resource}

import org.slf4s.{Logger, LoggerFactory}

import java.nio.file.{Files, Paths}
import java.nio.charset.StandardCharsets.UTF_8

import scala.util.{Left, Right}

abstract class GCSDatasourceSpec extends BlobstoreDatasourceSpec {

  import GCSConfig.serviceAccountConfigCodecJson
  import AzureDatasourceSpec.ioMonadResourceErr

  val log: Logger = LoggerFactory("quasar.blobstore.gcs.GCSListServiceSpec")

  val AUTH_FILE="precog-ci-275718-9de94866bc77.json"
  val bucket = Bucket("bucket-8168b20d-a6f0-427f-a21b-232a2e8742e1")
  val authCfgPath = Paths.get(getClass.getClassLoader.getResource(AUTH_FILE).toURI)
  val authCfgString = new String(Files.readAllBytes(authCfgPath), UTF_8)
  val authCfgJson: Json = Parse.parse(authCfgString) match {
    case Left(value) => Json.obj("malformed" := true)
    case Right(value) => value
  }
  val format = DataFormat.json
  val authConfig = authCfgJson.as[ServiceAccountConfig].toOption.get
  val cfg = GCSConfig(authConfig, bucket, format)
  
  override def datasource: Resource[IO, LightweightDatasourceModule.DS[IO]] =
    GCSDatasource.mk[IO](log, cfg)
}

object AzureDatasourceSpec {

  implicit val ioMonadResourceErr: MonadError_[IO, ResourceError] =
    MonadError_.facet[IO](ResourceError.throwableP)
}
