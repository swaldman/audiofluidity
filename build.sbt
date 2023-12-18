ThisBuild / organization := "com.mchange"
ThisBuild / version      := "0.0.4-SNAPSHOT"
ThisBuild / maintainer   := "swaldman@mchange.com"
ThisBuild / scalaVersion := "3.3.1"

enablePlugins(JavaAppPackaging)

lazy val root = project
  .in(file("."))
  .settings(
    name                := "audiofluidity",
    libraryDependencies += "com.mchange" %% "audiofluidity-rss" % "0.0.5-SNAPSHOT",
    libraryDependencies += "com.mchange" %% "mlog-scala" % "0.3.14",
    libraryDependencies += "com.mpatric" % "mp3agic" % "0.9.1",
    libraryDependencies += "org.scala-lang" %% "scala3-compiler" % scalaVersion.value,
    resolvers += Resolver.mavenLocal,
    // libraryDependencies += "com.googlecode.soundlibs" % "mp3spi" % "1.9.5.4",
    // libraryDependencies += "io.d11" %% "zhttp" % "1.0.0.0-RC17"
  )
