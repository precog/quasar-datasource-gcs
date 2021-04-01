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

import slamdata.Predef._

import quasar.blobstore.gcs.{Bucket, ServiceAccountConfig}
import quasar.connector.DataFormat

import argonaut._, Argonaut._

import org.specs2.ScalaCheck
import org.specs2.mutable.Specification

import java.net.URI

class JsonSpec extends Specification with ScalaCheck {

  import json.codecConfig

  val BogusAuthConfig: ServiceAccountConfig = ServiceAccountConfig(
    tokenUri = URI.create("https://oauth2.googleapis.com/token"),
    authProviderCertUrl = URI.create("https://www.googleapis.com/oauth2/v1/certs"),
    clientCertUrl = URI.create("https://www.googleapis.com/robot/v1/metadata/x509/read-bucket-sa%40project-name.iam.gserviceaccount.com"),
    authUri = URI.create("https://accounts.google.com/o/oauth2/auth"),
    privateKey = "1234567890",
    clientId = "1234567890",
    projectId = "project-name",
    privateKeyId = "1234567890",
    clientEmail = "read-bucket-sa@project-name.iam.gserviceaccount.com",
    accountType = "service-account"
  )

  "json decoder" >> {

    "succeeds reading config" >> {
      val testCfg = 
        """
            |{
            |  "auth": {
            |    "token_uri": "https://oauth2.googleapis.com/token",
            |    "auth_provider_x509_cert_url": "https://www.googleapis.com/oauth2/v1/certs",
            |    "private_key": "1234567890",
            |    "client_id": "1234567890",
            |    "client_x509_cert_url": "https://www.googleapis.com/robot/v1/metadata/x509/read-bucket-sa%40project-name.iam.gserviceaccount.com",
            |    "auth_uri": "https://accounts.google.com/o/oauth2/auth",
            |    "project_id": "project-name",
            |    "private_key_id": "1234567890",
            |    "client_email": "read-bucket-sa@project-name.iam.gserviceaccount.com",
            |    "type": "service-account"
            |  },
            |  "bucket": "bucket-8168b20d-a6f0-427f-a21b-232a2e8742e1",
            |  "format": { "format": { "type": "json", "variant": "array-wrapped", "precise": false } }
            |}
        """.stripMargin


      testCfg.decodeOption[GCSConfig] must_=== Some(GCSConfig(
        BogusAuthConfig,
        Bucket("bucket-8168b20d-a6f0-427f-a21b-232a2e8742e1"),
        DataFormat.json))
    }

    "fails reading inproperly formatted config" >> {
      val testCfg =
        """
          | {"a": "wrong", "config": "be", "here": true }
        """
      testCfg.decodeOption[GCSConfig]  must_=== None
    }

  }

}