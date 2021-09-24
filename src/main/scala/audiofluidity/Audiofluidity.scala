package audiofluidity

import java.nio.file.Path

object Audiofluidity {
  val DefaultPodcastSourceFqcn = "AudiofluiditySource"

  @main def go(args : String*) : Unit =
    val configPath           = DefaultConfigPath
    val configScalaPath      = configPath.resolve(DefaultScalaDirNameInConfig)
    val configTmpPath        = configPath.resolve(DefaultTmpNameInConfig)
    val configTmpClassesPath = configTmpPath.resolve(DefaultClassesNameInTmp)

    val podcastSourceFqcn = DefaultPodcastSourceFqcn

    val sep = sys.props("path.separator")
    val appClassPath = sys.props("java.class.path") + sep + configTmpClassesPath

    def buildConfigPodcast() : Podcast =
      val podcastSource = compileConfig( podcastSourceFqcn, appClassPath, configPath )
      podcastSource.toPodcast

    if args.nonEmpty then
      val command = args.head
      command match
        case "clean"    =>
          val podcast = buildConfigPodcast()
          recursiveDeleteDirectory(configTmpPath, leaveTop = true)
          recursiveDeleteDirectory(podcast.build.podcastgenDir, leaveTop = true)
        case "generate" =>
          val podcast = buildConfigPodcast()
          generate( podcast )
          println(s"Successfully generated podcast '${podcast.title}''")
        case _ => usage()
    else usage()

  def usage() : Unit =
    println("Usage: audiocity <command>")
    println("Commands:")
    println("  generate -- Compiles and generates a configured podcast")
}
