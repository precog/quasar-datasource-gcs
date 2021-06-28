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

import quasar.connector.datasource.DatasourceModule

import java.util.UUID

import scala._
import scala.Predef._
import scala.concurrent.ExecutionContext

import quasar.RateLimiting
import quasar.api.datasource.DatasourceType
import quasar.api.datasource.DatasourceError.{ConfigurationError, InitializationError}
import quasar.connector.{ByteStore, MonadResourceErr, ExternalCredentials}
import quasar.connector.datasource.Reconfiguration

import argonaut.Json

import cats.effect.{ConcurrentEffect, ContextShift, Resource, Sync, Timer}
import cats.kernel.Hash

import org.slf4s.Logging

object GCSDatasourceModule extends DatasourceModule with Logging {

   val kind: DatasourceType = DatasourceType("gcs", 1L)

   def datasource[F[_]: ConcurrentEffect: ContextShift: MonadResourceErr: Timer, A: Hash](
       config: Json,
       rateLimiting: RateLimiting[F, A],
       byteStore: ByteStore[F],
       getAuth: UUID => F[Option[ExternalCredentials[F]]])(
       implicit ec: ExecutionContext)
       : Resource[F, Either[InitializationError[Json], DatasourceModule.DS[F]]] = ???

   def reconfigure(original: Json, patch: Json)
       : Either[ConfigurationError[Json], (Reconfiguration, Json)] = ???

   def sanitizeConfig(config: Json): Json = ???

   def migrateConfig[F[_]: Sync](from: Long, to: Long, config: Json): F[Either[ConfigurationError[Json], Json]] =
     Sync[F].pure(Right(config))
}
