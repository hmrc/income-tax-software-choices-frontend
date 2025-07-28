import sbt.*

object AppDependencies {

  val bootstrapPlayVersion = "9.13.0"
  val playFrontendHMRCVersion = "12.7.0"
  private val hmrcMongoVersion = "2.7.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc" %% "bootstrap-frontend-play-30" % bootstrapPlayVersion,
    "uk.gov.hmrc" %% "play-frontend-hmrc-play-30" % playFrontendHMRCVersion,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30" % hmrcMongoVersion,
    "org.webjars" % "jquery" % "3.7.1"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc" %% "bootstrap-test-play-30" % bootstrapPlayVersion % "test",
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-test-play-30" % hmrcMongoVersion % "test",
    "org.scalatestplus" %% "scalacheck-1-17" % "3.2.18.0" % "test",
    "org.jsoup" % "jsoup" % "1.21.1" % "test"
  )

  val itTest: Seq[ModuleID] = Seq()

}
