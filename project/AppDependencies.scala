import sbt._

object AppDependencies {

  val bootstrapPlayVersion = "5.24.0"

  val compile = Seq(
    "uk.gov.hmrc" %% "bootstrap-frontend-play-28" % bootstrapPlayVersion,
    "uk.gov.hmrc" %% "play-frontend-hmrc" % "3.14.0-play-28",
    "org.webjars" % "jquery" % "3.6.0"
  )

  val test = Seq(
    "uk.gov.hmrc" %% "bootstrap-test-play-28" % bootstrapPlayVersion % "test, it",
    "org.jsoup" % "jsoup" % "1.13.1" % Test,
    "com.vladsch.flexmark" % "flexmark-all" % "0.36.8" % "test, it",
    "org.mockito" %% "mockito-scala" % "1.17.5" % Test
  )

}
