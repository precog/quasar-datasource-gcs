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

import quasar.blobstore.gcs.{Bucket, ServiceAccountConfig}

import scala.StringContext
import scala.Predef.String

import argonaut._, Argonaut._

import cats.implicits._
import cats.effect.implicits._

import org.http4s.argonaut._

import scala.util.Either

import java.net.{URI, URISyntaxException}
import quasar.connector.DataFormat

object json {

  implicit val uriCodecJson: CodecJson[URI] =
    CodecJson(
      uri => Json.jString(uri.toString),
      c => for {
        uriStr <- c.jdecode[String]
        uri0 = Either.catchOnly[URISyntaxException](new URI(uriStr))
        uri <- uri0.fold(
          ex => DecodeResult.fail(s"Invalid URI: ${ex.getMessage}", c.history),
          DecodeResult.ok(_))
      } yield uri)

  implicit val codecBucket: CodecJson[Bucket] =
    CodecJson(_.value.asJson, jdecode1(Bucket(_)).decode)

  implicit val serviceAccountConfigCodecJson: CodecJson[ServiceAccountConfig] = 
    casecodec10[URI,URI, String, String, URI, URI, String, String, String, String, ServiceAccountConfig](
      (tokenUri,
      authProviderCertUrl,
      privateKey,
      clientId,
      clientCertUrl,
      authUri,
      projectId,
      privateKeyId,
      clientEmail,
      accountType) => ServiceAccountConfig(
        tokenUri = tokenUri,
        authProviderCertUrl = authProviderCertUrl,
        privateKey = privateKey,
        clientId = clientId,
        clientCertUrl = clientCertUrl,
        authUri = authUri,
        projectId = projectId,
        privateKeyId = privateKeyId,
        clientEmail = clientEmail,
        accountType = accountType),
      sac => 
        (sac.tokenUri, 
        sac.authProviderCertUrl,
        sac.privateKey,
        sac.clientId,
        sac.clientCertUrl,
        sac.authUri,
        sac.projectId,
        sac.privateKeyId,
        sac.clientEmail,
        sac.accountType).some)(
          "token_uri",
          "auth_provider_x509_cert_url",
          "private_key",
          "client_id",
          "client_x509_cert_url",
          "auth_uri",
          "project_id",
          "private_key_id",
          "client_email",
          "type")

  implicit val codecConfig: CodecJson[GCSConfig] =
    casecodec3(GCSConfig.apply, GCSConfig.unapply)("auth", "bucket", "format")
}
