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

import quasar.api.datasource.DatasourceType
import quasar.blobstore.gcs.{GCSGetService, GCSListService, GCSStatusService, GoogleCloudStorage}
import quasar.connector.MonadResourceErr
import quasar.physical.blobstore.BlobstoreDatasource

import cats.effect.{Concurrent, ConcurrentEffect, ContextShift, Resource, Timer}
import org.http4s.client.Client
import org.slf4s.Logger

object GCSDatasource {
  val dsType: DatasourceType = DatasourceType("gcs", 1L)

  def mk[F[_]: Concurrent: ConcurrentEffect: ContextShift: MonadResourceErr: Timer](
      log: Logger,
      cfg: GCSConfig)
      : Resource[F, BlobstoreDatasource[F, Client[F]]] = {

    for {
      client <- GoogleCloudStorage.mkContainerClient(cfg.gac)
    } yield
        BlobstoreDatasource[F, Client[F]](
          dsType, 
          cfg.format,
          GCSStatusService[F](client, cfg.bucket, cfg.gac),
          GCSListService[F](log, client, cfg.bucket),
          scala.Predef.???,
          GCSGetService.mk[F](client, cfg.bucket, cfg.gac))
  }
}