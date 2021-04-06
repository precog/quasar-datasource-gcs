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

import quasar.blobstore.gcs.Bucket
import quasar.contrib.scalaz.MonadError_
import quasar.connector.ResourceError
import quasar.connector.datasource.LightweightDatasourceModule
import quasar.physical.blobstore.BlobstoreDatasourceSpec

import cats.effect.{IO, Resource}

import org.slf4s.{Logger, LoggerFactory}

class GCSDatasourceSpec extends BlobstoreDatasourceSpec {

  import AzureDatasourceSpec.ioMonadResourceErr

  val log: Logger = LoggerFactory("quasar.blobstore.gcs.GCSListServiceSpec")

  val cfg = common.getGCSConfig(
    "precog-ci-275718-9de94866bc77.json",
    Bucket("precog-test-bucket"))
  
  override def datasource: Resource[IO, LightweightDatasourceModule.DS[IO]] =
    GCSDatasource.mk[IO](log, cfg)
}

object AzureDatasourceSpec {

  implicit val ioMonadResourceErr: MonadError_[IO, ResourceError] =
    MonadError_.facet[IO](ResourceError.throwableP)
}
