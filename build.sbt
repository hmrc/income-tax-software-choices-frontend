import uk.gov.hmrc.DefaultBuildSettings.integrationTestSettings
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings

import scala.sys.process._

val appName = "income-tax-software-choices-frontend"

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .settings(
    majorVersion := 0,
    scalaVersion := "2.13.8",
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test,
    Assets / pipelineStages := Seq(gzip),
    scalacOptions += "-Wconf:src=.*/views/.*:s",
    scalacOptions += "-Wconf:src=routes/.*:s",
  )
  .settings(publishingSettings: _*)
  .configs(IntegrationTest)
  .settings(integrationTestSettings(): _*)
  .settings(resolvers += Resolver.jcenterRepo)
  .settings(CodeCoverageSettings.settings: _*)

lazy val results = taskKey[Unit]("Opens test results'")
results := { "open target/test-reports/html-report/index.html" ! }
Test / results := (results).value

lazy val itResults = taskKey[Unit]("Opens it test results'")
itResults := { "open target/int-test-reports/html-report/index.html" ! }
IntegrationTest / results := (itResults).value

