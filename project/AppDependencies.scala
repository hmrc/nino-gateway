import play.sbt.PlayImport._
import sbt._

object AppDependencies {
  val compile = Seq(
    "uk.gov.hmrc" %% "bootstrap-backend-play-28" % "7.12.0"
  )

  val test = Seq(
    "uk.gov.hmrc" %% "bootstrap-test-play-28" % "7.12.0" % "test, it"
  )
}
