import sbt._
import Keys._

import scala.util.Properties.envOrNone

object Http4sBuild extends Build {
  // keys
  val apiVersion = TaskKey[(Int, Int)]("api-version", "Defines the API compatibility version for the project.")
  val jvmTarget = TaskKey[String]("jvm-target-version", "Defines the target JVM version for object files.")

  def extractApiVersion(version: String) = {
    val VersionExtractor = """(\d+)\.(\d+)\..*""".r
    version match {
      case VersionExtractor(major, minor) => (major.toInt, minor.toInt)
    }
  }

  lazy val sonatypeEnvCredentials = (for {
    user <- envOrNone("SONATYPE_USER")
    pass <- envOrNone("SONATYPE_PASS")
  } yield Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", user, pass)).toSeq

  def compatibleVersion(version: String, scalazVersion: String) = {
    val currentVersionWithoutSnapshot = version.replaceAll("-SNAPSHOT$", "")
    val (targetMajor, targetMinor) = extractApiVersion(version)
    val targetVersion = scalazCrossBuild(s"${targetMajor}.${targetMinor}.0", scalazVersion)
    if (targetVersion != currentVersionWithoutSnapshot)
      Some(targetVersion)
    else
      None
  }

  val macroParadiseSetting =
    libraryDependencies <++= scalaVersion (
      VersionNumber(_).numbers match {
        case Seq(2, 10, _*) => Seq(
          compilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full),
          "org.scalamacros" %% "quasiquotes" % "2.1.0" cross CrossVersion.binary
        )
        case _ => Seq.empty
      })

  val scalazVersion = settingKey[String]("The version of Scalaz used for building.")
  def scalazCrossBuild(version: String, scalazVersion: String) =
    VersionNumber(scalazVersion).numbers match {
      case Seq(7, 1, _*) =>
        version
      case Seq(7, 2, _*) =>
        if (version.endsWith("-SNAPSHOT"))
          version.replaceFirst("-SNAPSHOT$", "a-SNAPSHOT")
        else
          s"${version}a"
    }
  def specs2Version(scalazVersion: String) =
    VersionNumber(scalazVersion).numbers match {
      case Seq(7, 1, _*) => "3.8.3-scalaz-7.1"
      case Seq(7, 2, _*) => "3.8.3"
    }

  lazy val alpnBoot            = "org.mortbay.jetty.alpn"    % "alpn-boot"               % "8.1.7.v20160121"
  def argonaut(scalazVersion: String) = "io.argonaut"       %% "argonaut"                % scalazCrossBuild("6.1", scalazVersion)
  lazy val asyncHttpClient     = "org.asynchttpclient"       % "async-http-client"       % "2.0.5"
  lazy val blaze               = "org.http4s"               %% "blaze-http"              % "0.12.0"
  lazy val cats                = "org.typelevel"            %% "cats"                    % "0.6.1"
  lazy val circeGeneric        = "io.circe"                 %% "circe-generic"           % circeJawn.revision
  lazy val circeJawn           = "io.circe"                 %% "circe-jawn"              % "0.4.1"
  lazy val discipline          = "org.typelevel"            %% "discipline"              % "0.5"
  lazy val fs2Core             = "co.fs2"                   %% "fs2-core"                % "0.9.0-M6"
  lazy val fs2Scalaz           = "co.fs2"                   %% "fs2-scalaz"              % "0.1.0-M6"
  lazy val gatlingTest         = "io.gatling"                % "gatling-test-framework"  % "2.2.1"
  lazy val gatlingHighCharts   = "io.gatling.highcharts"     % "gatling-charts-highcharts" % gatlingTest.revision
  lazy val http4sWebsocket     = "org.http4s"               %% "http4s-websocket"        % "0.1.3"
  lazy val javaxServletApi     = "javax.servlet"             % "javax.servlet-api"       % "3.1.0"
  lazy val jawnJson4s          = "org.spire-math"           %% "jawn-json4s"             % jawnParser.revision
  lazy val jawnParser          = "org.spire-math"           %% "jawn-parser"             % "0.8.4"
  def jawnStreamz(scalazVersion: String) = "org.http4s"     %% "jawn-streamz"            % scalazCrossBuild("0.9.0", scalazVersion)
  lazy val jettyServer         = "org.eclipse.jetty"         % "jetty-server"            % "9.3.9.v20160517"
  lazy val jettyServlet        = "org.eclipse.jetty"         % "jetty-servlet"           % jettyServer.revision
  lazy val json4sCore          = "org.json4s"               %% "json4s-core"             % "3.3.0"
  lazy val json4sJackson       = "org.json4s"               %% "json4s-jackson"          % json4sCore.revision
  lazy val json4sNative        = "org.json4s"               %% "json4s-native"           % json4sCore.revision
  lazy val jspApi              = "javax.servlet.jsp"         % "javax.servlet.jsp-api"   % "2.3.1" // YourKit hack
  lazy val log4s               = "org.log4s"                %% "log4s"                   % "1.3.0"
  lazy val logbackClassic      = "ch.qos.logback"            % "logback-classic"         % "1.1.7"
  lazy val metricsCore         = "io.dropwizard.metrics"     % "metrics-core"            % "3.1.2"
  lazy val metricsJetty9       = "io.dropwizard.metrics"     % "metrics-jetty9"          % metricsCore.revision
  lazy val metricsServlet      = "io.dropwizard.metrics"     % "metrics-servlet"         % metricsCore.revision
  lazy val metricsServlets     = "io.dropwizard.metrics"     % "metrics-servlets"        % metricsCore.revision
  lazy val metricsJson         = "io.dropwizard.metrics"     % "metrics-json"            % metricsCore.revision
  lazy val parboiled           = "org.parboiled"            %% "parboiled"               % "2.1.2"
  lazy val reactiveStreamsTck  = "org.reactivestreams"       % "reactive-streams-tck"    % "1.0.0"
  def scalaReflect(sv: String) = "org.scala-lang"            % "scala-reflect"           % sv
  lazy val scalaXml            = "org.scala-lang.modules"   %% "scala-xml"               % "1.0.5"
  def scalazCore(version: String)               = "org.scalaz"           %% "scalaz-core"               % version
  def scalazScalacheckBinding(version: String)  = "org.scalaz"           %% "scalaz-scalacheck-binding" % version
  def specs2Core(scalazVersion: String)         = "org.specs2"           %% "specs2-core"               % specs2Version(scalazVersion)
  def specs2MatcherExtra(scalazVersion: String) = "org.specs2"           %% "specs2-matcher-extra"      % specs2Core(scalazVersion).revision
  def specs2Scalacheck(scalazVersion: String)   = "org.specs2"           %% "specs2-scalacheck"         % specs2Core(scalazVersion).revision
  def scalazStream(scalazVersion: String)       = "org.scalaz.stream"    %% "scalaz-stream"             % scalazCrossBuild("0.8.2", scalazVersion)
  lazy val tomcatCatalina      = "org.apache.tomcat"         % "tomcat-catalina"         % "8.0.36"
  lazy val tomcatCoyote        = "org.apache.tomcat"         % "tomcat-coyote"           % tomcatCatalina.revision
  lazy val twirlApi            = "com.typesafe.play"        %% "twirl-api"               % "1.2.0"
}
