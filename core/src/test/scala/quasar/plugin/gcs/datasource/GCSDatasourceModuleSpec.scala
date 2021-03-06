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
import scala.Left

import quasar.RateLimiter
import quasar.blobstore.gcs.Bucket
import quasar.connector.ByteStore
import quasar.api.datasource.DatasourceError

import argonaut._

import cats.effect.{ContextShift, IO, Timer}
import cats.implicits._
import cats.kernel.instances.uuid._

import org.specs2.mutable.Specification
import scala.concurrent.ExecutionContext

import java.util.UUID


class GCSDatasourceModuleSpec extends Specification {

  import GCSDatasourceSpec._
  import json._

  implicit val ec: ExecutionContext = ExecutionContext.global
  implicit val cs: ContextShift[IO] = IO.contextShift(ec)
  implicit val timer: Timer[IO] = IO.timer(ec)

  def init(j: Json) =
    RateLimiter[IO, UUID](IO.delay(UUID.randomUUID()))
    .flatMap(rl =>
      GCSDatasourceModule.datasource[IO, UUID](
        j, rl, ByteStore.void[IO], _ => IO(None)))
    .use(r => IO.pure(r.void))
    .unsafeRunSync()

  val validBucket = Bucket("bucket-8168b20d-a6f0-427f-a21b-232a2e8742e1")

  val gcsConfigJson = common.getGCSConfigAsJson(
    "precog-ci-275718-9de94866bc77.json",
    validBucket)

  val badGCSConfigJson = common.getGCSConfigAsJson(
    "bad-auth-file.json",
    validBucket)

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

    "access denied with invalid auth file" >> {
      init(badGCSConfigJson) must beLike {
        case Left(DatasourceError.AccessDenied(_, _, _)) => ok
      }
    }

    "access denied with valid config and invalid bucket " >> {
      init(gcsConfigJsonWithInvalidBucket) must beLike {
        case Left(DatasourceError.AccessDenied(_, _, _)) => ok
      }
    }
  }

  "sanitize config" >> {
    "redacts config with credentials" >> {
      val saCfgJson = common.getGCSConfigFromSAConfigAsJson(
        GCSConfig.SanitizedAuth,
        validBucket)

      GCSDatasourceModule.sanitizeConfig(gcsConfigJson) must_=== saCfgJson
    }
  }

}
