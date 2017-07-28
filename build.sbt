name := "tcs_greenhouse"
version := "0.1"
scalaVersion := "2.12.2"

libraryDependencies ++= Seq(
  "com.github.pathikrit" %% "better-files" % "3.0.0",
  "com.github.pureconfig" %% "pureconfig" % "0.7.2",
  "io.monix" %% "monix" % "2.3.0",

  "org.scalatest"     %% "scalatest"         % "3.0.1"         % Test
)

scalacOptions ++= Seq(
  "-unchecked",
  "-deprecation",
  "-feature",
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-language:postfixOps",
  "-Ywarn-dead-code",
  "-Ywarn-infer-any",
  "-Ywarn-unused-import",
  "-Xfatal-warnings",
  "-Xlint"
)