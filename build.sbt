import uk.gov.hmrc.DefaultBuildSettings

val appName = "income-tax-software-choices-frontend"

ThisBuild / majorVersion := 0
ThisBuild / scalaVersion := "2.13.16"

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .settings(
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test,
    Assets / pipelineStages := Seq(gzip),
    scalacOptions += "-Wconf:src=.*/views/.*:s",
    scalacOptions += "-Wconf:src=routes/.*:s",
  )
  .settings(PlayKeys.playDefaultPort := 9591)
  .settings(CodeCoverageSettings.settings *)

lazy val it = project
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test") // the "test->test" allows reusing test code and test dependencies
  .settings(DefaultBuildSettings.itSettings())
  .settings(libraryDependencies ++= AppDependencies.itTest)

TwirlKeys.templateImports ++= Seq(
  "uk.gov.hmrc.govukfrontend.views.html.components._",
  "uk.gov.hmrc.hmrcfrontend.views.html.components._",
  "uk.gov.hmrc.hmrcfrontend.views.html.helpers._",
  "uk.gov.hmrc.govukfrontend.views.html.components.implicits._"
)