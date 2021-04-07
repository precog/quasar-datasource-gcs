ThisBuild / crossScalaVersions := Seq("2.12.10")
ThisBuild / scalaVersion := (ThisBuild / crossScalaVersions).value.head

ThisBuild / githubRepository := "quasar-datasource-gcs"

ThisBuild / homepage := Some(url("https://github.com/precog/quasar-datasource-gcs"))

ThisBuild / scmInfo := Some(ScmInfo(
  url("https://github.com/precog/quasar-datasource-gcs"),
  "scm:git@github.com:precog/quasar-datasource-gcs.git"))

ThisBuild / githubWorkflowBuildPreamble +=
  WorkflowStep.Sbt(
    List("decryptSecret core/src/test/resources/precog-ci-275718-9de94866bc77.json.enc"),
    name = Some("Decrypt gcp service account json key"))

ThisBuild / githubWorkflowBuildPreamble +=
  WorkflowStep.Sbt(
    List("decryptSecret core/src/test/resources/bad-auth-file.json.enc"),
    name = Some("Decrypt bad gcp service account json key"))

ThisBuild / publishAsOSSProject := true

// Include to also publish a project's tests
lazy val publishTestsSettings = Seq(
  Test / packageBin / publishArtifact := true)

lazy val quasarVersion =
  Def.setting[String](managedVersions.value("precog-quasar"))

val Specs2Version = "4.9.4"
val Http4sVersion = "0.21.13"
val Slf4sVersion = "1.7.25"

lazy val root = project
  .in(file("."))
  .settings(noPublishSettings)
  .aggregate(core)

lazy val core = project
  .in(file("core"))
  .settings(
    name := "quasar-datasource-gcs",

    quasarPluginName := "gcs",

    quasarPluginQuasarVersion := quasarVersion.value,

    quasarPluginDatasourceFqcn := Some("quasar.plugin.gcs.datasource.GCSDatasourceModule$"),

    // The quasarPluginDependencies key is analogous to libraryDependencies, except it
    // will be considered as part of the assembly and packaging process for your plugin.
    // You should declare all of your non-Test dependencies using this key rather than
    // libraryDependencies.
    quasarPluginDependencies ++= Seq(
      "com.precog" %% "async-blobstore-gcs" % managedVersions.value("precog-async-blobstore"),
      "com.precog" %% "quasar-lib-blobstore" % managedVersions.value("precog-quasar-lib-blobstore"),
      "org.http4s" %% "http4s-async-http-client" % Http4sVersion,
      "org.slf4s" %% "slf4s-api" % Slf4sVersion),

    libraryDependencies ++= Seq(
      "com.precog" %% "quasar-lib-blobstore" % managedVersions.value("precog-quasar-lib-blobstore") % "test->test" classifier "tests",
      "org.specs2" %% "specs2-core" % Specs2Version % Test))

  .enablePlugins(QuasarPlugin)
  .evictToLocal("QUASAR_PATH", "connector", true)
  .evictToLocal("QUASAR_LIB_BLOBSTORE_PATH", "core", true)
  .evictToLocal("ASYNC_BLOBSTORE_PATH", "core", true)
  .evictToLocal("ASYNC_BLOBSTORE_PATH", "gcs", true)

