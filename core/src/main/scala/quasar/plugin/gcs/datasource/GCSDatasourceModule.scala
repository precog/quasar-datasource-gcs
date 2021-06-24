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

import quasar.connector.datasource.LightweightDatasourceModule

import java.net.{MalformedURLException, UnknownHostException}
import java.util.UUID

import scala._
import scala.concurrent.ExecutionContext
import scala.util.control.NonFatal

import quasar.RateLimiting
import quasar.api.datasource.{DatasourceError, DatasourceType}, DatasourceError._
import quasar.blobstore.BlobstoreStatus
import quasar.connector.{ByteStore, MonadResourceErr, ExternalCredentials}
import quasar.connector.datasource.Reconfiguration
import quasar.plugin.gcs.datasource.json._

import argonaut._, Argonaut._
import cats.ApplicativeError
import cats.effect.{ConcurrentEffect, ContextShift, Resource, Sync, Timer}
import cats.kernel.Hash
import cats.implicits._
import org.slf4s.Logging
import scalaz.NonEmptyList

object GCSDatasourceModule extends LightweightDatasourceModule with Logging {

   val kind: DatasourceType = DatasourceType("gcs", 1L)

   def lightweightDatasource[F[_]: ConcurrentEffect: ContextShift: MonadResourceErr: Timer, A: Hash](
       json: Json,
       rateLimiting: RateLimiting[F, A],
       byteStore: ByteStore[F],
       getAuth: UUID => F[Option[ExternalCredentials[F]]])(
       implicit ec: ExecutionContext)
       : Resource[F, Either[InitializationError[Json], LightweightDatasourceModule.DS[F]]] = {

    val sanitizedJson = sanitizeConfig(json)

    json.as[GCSConfig].result match {
      case Right(cfg) =>
        val r = for {
          ds <- GCSDatasource.mk(log, cfg)
          l <- Resource.eval(ds.status)
          res = l match {
            case BlobstoreStatus.Ok =>
              Right(ds.asDsType)

            case BlobstoreStatus.NoAccess =>
              Left(DatasourceError
                .accessDenied[Json, InitializationError[Json]](kind, sanitizedJson, "Access to blobstore denied"))

            case BlobstoreStatus.NotFound =>
              Left(DatasourceError
                .invalidConfiguration[Json, InitializationError[Json]](kind, sanitizedJson, NonEmptyList("Blobstore not found")))

            case BlobstoreStatus.NotOk(msg) =>
              Left(DatasourceError
                .invalidConfiguration[Json, InitializationError[Json]](kind, sanitizedJson, NonEmptyList(msg)))
          }
        } yield res

        ApplicativeError[Resource[F, *], Throwable].handleError(r) {
          case _: MalformedURLException =>
            Left(DatasourceError
              .invalidConfiguration[Json, InitializationError[Json]](kind, sanitizedJson, NonEmptyList("Invalid storage url")))

          case _: UnknownHostException =>
            Left(DatasourceError
              .invalidConfiguration[Json, InitializationError[Json]](kind, sanitizedJson, NonEmptyList("Non-existing storage url")))

          case NonFatal(t) =>
            Left(DatasourceError
              .invalidConfiguration[Json, InitializationError[Json]](kind, sanitizedJson, NonEmptyList(t.getMessage)))
        }

      case Left((msg, _)) =>
        DatasourceError
          .invalidConfiguration[Json, InitializationError[Json]](kind, sanitizedJson, NonEmptyList(msg))
          .asLeft[LightweightDatasourceModule.DS[F]]
          .pure[Resource[F, ?]]
    }
  }

  override def sanitizeConfig(config: Json): Json = config.as[GCSConfig].result match {
    case Left(_) =>
      config
    case Right(cfg) =>
      cfg.sanitize.asJson
  }


  override def migrateConfig[F[_]: Sync](from: Long, to: Long, config: Json): F[Either[ConfigurationError[Json], Json]] =
    Sync[F].pure(Right(config))

  override def reconfigure(original: Json, patch: Json): Either[ConfigurationError[Json], (Reconfiguration, Json)] = {
    val back = for {
      originalConfig <-
        original.as[GCSConfig].result.leftMap(_ =>
          MalformedConfiguration[Json](
            kind,
            sanitizeConfig(original),
            "Source configuration in reconfiguration is malformed."))

      patchConfig <-
        patch.as[GCSConfig].result.leftMap(_ =>
          MalformedConfiguration[Json](
            kind,
            sanitizeConfig(patch),
            "Target configuration in reconfiguration is malformed."))

      reconfig <- originalConfig.reconfigureNonSensitive(patchConfig).leftMap(c =>
        InvalidConfiguration[Json](
          kind,
          c.asJson,
          NonEmptyList("Target configuration contains sensitive information.")))

    } yield reconfig.asJson

    back.tupleLeft(Reconfiguration.Reset)
  }
}
