lazy val root = project
  .in(file("."))
  .settings(
    name                := "audiocity",
    version             := "0.1.0",
    scalaVersion        := "3.0.2",
    libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % "2.0.1",
    // libraryDependencies += "io.d11" %% "zhttp" % "1.0.0.0-RC17"
  )
