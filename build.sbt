enablePlugins(JavaAppPackaging)

name := "content-diff"

organization := "org.content.diff"

version := "1.0.0"

Defaults.itSettings

lazy val root = project.in(file(".")).configs(IntegrationTest)

scalaVersion := "2.12.3"

scalacOptions ++= Seq(
  "-deprecation",
  "-feature",
  "-language:postfixOps",
  "-unchecked",
  "-Xfatal-warnings",
  "-Ywarn-unused-import",
  "-Ywarn-unused",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Xlint:missing-interpolator")

libraryDependencies ++= Seq(
  "org.springframework.boot" % "spring-boot-starter-web" % "1.5.6.RELEASE",
  "org.springframework.boot" % "spring-boot-starter-jetty" % "1.5.6.RELEASE",
  "com.typesafe.akka" %% "akka-actor" % "2.5.3",
  "com.typesafe" % "config" % "1.3.1",
  "io.reactivex.rxjava2" % "rxjava" % "2.1.1",
  "org.scalatest" %% "scalatest" % "3.0.3" % "test,it",
  "org.scalacheck" %% "scalacheck" % "1.13.5" % "test,it",
  "junit" % "junit" % "4.12" % "test,it",
  "org.springframework.boot" % "spring-boot-starter-test" % "1.5.6.RELEASE" % "test,it",
  "com.typesafe.akka" %% "akka-testkit" % "2.5.3" % "test,it",
  "org.awaitility" % "awaitility" % "3.0.0" % "test,it")

scalastyleConfig := file("content_diff-scalastyle-config.xml")
scalastyleFailOnError := true

scalacOptions in (Compile, doc) := Seq(
  "-groups",
  "-implicits",
  "-no-link-warnings")
scalacOptions in (Test, doc) := Seq(
  "-groups",
  "-implicits",
  "-no-link-warnings")
scalacOptions in (IntegrationTest, doc) := Seq(
  "-groups",
  "-implicits",
  "-no-link-warnings")
apiMappings += (
  scalaInstance.value.libraryJar -> url(s"http://www.scala-lang.org/api/${scalaVersion.value}/"))
autoAPIMappings := true
apiURL := Some(url("http://diff.content.org/api/"))

jacoco.settings

jacoco.excludes in jacoco.Config := Seq(
  "*ContentDiffApplication*",
  "*ContentDiffConfig*",
  "*ContentJson*",
  "*DiffsJson*",
  "*DiffJson*",
  "*ContentDoNotMatch*",
  "*ContentDiffMessages*",
  "*ContentDiffService*")

jacoco.thresholds in jacoco.Config := de.johoop.jacoco4sbt.Thresholds(
  instruction = 51.00,
  method = 85.00,
  branch = 51.00,
  complexity = 51.00,
  line = 85.0,
  clazz = 85.0)

mappings in (Compile, packageDoc) := Seq()
