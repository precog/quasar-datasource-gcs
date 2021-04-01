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

import scala.None

import quasar.{RateLimiter, NoopRateLimitUpdater}
import quasar.blobstore.gcs.Bucket
import quasar.connector.ByteStore

import argonaut._

import cats.effect.{ContextShift, IO, Timer}
import cats.implicits._
import cats.kernel.instances.uuid._

import org.specs2.mutable.Specification
import scala.concurrent.ExecutionContext

import java.util.UUID


class GCSDatasourceModuleSpec extends Specification {

  import AzureDatasourceSpec._
  import json._

  implicit val ec: ExecutionContext = ExecutionContext.global
  implicit val cs: ContextShift[IO] = IO.contextShift(ec)
  implicit val timer: Timer[IO] = IO.timer(ec)

  def init(j: Json) =
    RateLimiter[IO, UUID](
      1.0, IO.delay(UUID.randomUUID()), NoopRateLimitUpdater[IO, UUID])
    .flatMap(rl =>
      GCSDatasourceModule.lightweightDatasource[IO, UUID](
        j, rl, ByteStore.void[IO], _ => IO(None))
      .use(r => IO.pure(r.void)))
    .unsafeRunSync()

  val gcsConfigJson = common.getGCSConfigAsJson(
    "precog-ci-275718-9de94866bc77.json",
    Bucket("bucket-8168b20d-a6f0-427f-a21b-232a2e8742e1"))

  val badGCSConfigJson = common.getGCSConfigAsJson(
    "bad-auth-file.json",
    Bucket("bucket-8168b20d-a6f0-427f-a21b-232a2e8742e1"))

  val gcsConfigJsonWithInvalidBucket = common.getGCSConfigAsJson(
    "precog-ci-275718-9de94866bc77.json",
    Bucket("bad-bucket"))

  "datasource init" >> {

    "check we can encode GCSConig json" >> {
      gcsConfigJson.as[GCSConfig].result must beRight
    }

    "succeeds when correct cfg without credentials" >> {
      init(gcsConfigJson) must beRight
    }

    //TODO: make GCSAccessToken handle wrong configs in async-blobstore

    // "access denied with invalid auth file" >> {
    //   init(badGCSConfigJson) must beLike {
    //     case Left(DatasourceError.AccessDenied(_, _, _)) => ok
    //   }
    // }

    //TODO: GCSGetService returns empty stream when no bucket is present
    // so this fails since we get a Right(())

    // "invalid config when config is valid and bucket is invalid" >> {
    //   init(gcsConfigJsonWithInvalidBucket) must beLike {
    //     case Left(DatasourceError.InvalidConfiguration(_, _, _)) => ok
    //   }
    // }
  }

  "sanitize config" >> {
    "redacts config with credentials" >> {
      val saCfgJson = common.getGCSConfigFromSAConfigAsJson(
        GCSConfig.SanitizedAuth,
        Bucket("bucket-8168b20d-a6f0-427f-a21b-232a2e8742e1"))

      GCSDatasourceModule.sanitizeConfig(gcsConfigJson) must_=== saCfgJson
    }
  }

}