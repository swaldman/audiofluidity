val ScalaVersion = "3.0.2"

enablePlugins(JavaAppPackaging)

lazy val root = project
  .in(file("."))
  .settings(
    name                := "audiocity",
    version             := "0.1.0",
    scalaVersion        := ScalaVersion,
    libraryDependencies += "org.scala-lang" %% "scala3-compiler" % ScalaVersion,
    libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % "2.0.1",
    libraryDependencies += "com.mpatric" % "mp3agic" % "0.9.1",
    // libraryDependencies += "com.googlecode.soundlibs" % "mp3spi" % "1.9.5.4",
    // libraryDependencies += "io.d11" %% "zhttp" % "1.0.0.0-RC17"
  )
