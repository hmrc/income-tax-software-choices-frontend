import sbt._

object AppDependencies {

  val bootstrapPlayVersion = "8.2.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc" %% "bootstrap-frontend-play-30" % bootstrapPlayVersion,
    "uk.gov.hmrc" %% "play-frontend-hmrc-play-30" % "8.1.0",
    "org.webjars" % "jquery" % "3.7.1"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc" %% "bootstrap-test-play-30" % bootstrapPlayVersion % "test",
    "org.jsoup" % "jsoup" % "1.13.1" % Test
  )

  val itTest: Seq[ModuleID] = Seq()

}
