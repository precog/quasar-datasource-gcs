ThisBuild / crossScalaVersions := Seq("2.12.10")
ThisBuild / scalaVersion := (ThisBuild / crossScalaVersions).value.head

ThisBuild / githubRepository := "quasar-datasource-gcs"

ThisBuild / homepage := Some(url("https://github.com/precog/quasar-datasource-gcs"))

ThisBuild / scmInfo := Some(ScmInfo(
  url("https://github.com/precog/quasar-datasource-gcs"),
  "scm:git@github.com:precog/quasar-datasource-gcs.git"))

ThisBuild / publishAsOSSProject := true

// Include to also publish a project's tests
lazy val publishTestsSettings = Seq(
  Test / packageBin / publishArtifact := true)

lazy val root = project
  .in(file("."))
  .settings(noPublishSettings)
  .aggregate(core)

lazy val core = project
  .in(file("core"))
  .settings(name := "quasar-datasource-gcs")
