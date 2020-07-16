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

import scala._
import scala.Predef._

import quasar.api.datasource.DatasourceType
import quasar.api.resource.{ResourceName, ResourcePath, ResourcePathType}
import quasar.connector.QueryResult
import quasar.connector.datasource.{LightweightDatasource, Loader}
import quasar.qscript.InterpretedRead

import cats.data.NonEmptyList
import cats.effect.Resource

import fs2.Stream

import org.slf4s.Logging

final class GCSDatasource[F[_]](
    config: GCSConfig)
    extends LightweightDatasource[Resource[F, ?], Stream[F, ?], QueryResult[F]]
    with Logging {

  val kind: DatasourceType = GCSDatasourceModule.kind

  val loaders: NonEmptyList[Loader[Resource[F, ?], InterpretedRead[ResourcePath], QueryResult[F]]] = ???

  def pathIsResource(path: ResourcePath): Resource[F, Boolean] = ???

  def prefixedChildPaths(prefixPath: ResourcePath)
      : Resource[F, Option[Stream[F,(ResourceName, ResourcePathType.Physical)]]] = ???
}
