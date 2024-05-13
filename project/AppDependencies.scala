import sbt._

object AppDependencies {

  val bootstrapPlayVersion = "8.4.0"
  val playFrontendHMRCVersion = "9.10.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc" %% "bootstrap-frontend-play-30" % bootstrapPlayVersion,
    "uk.gov.hmrc" %% "play-frontend-hmrc-play-30" % playFrontendHMRCVersion,
    "org.webjars" % "jquery" % "3.7.1"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc" %% "bootstrap-test-play-30" % bootstrapPlayVersion % Test,
    "org.jsoup" % "jsoup" % "1.15.4" % Test
  )

  val itTest: Seq[ModuleID] = Seq()

}
