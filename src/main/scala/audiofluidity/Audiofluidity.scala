package audiofluidity

import java.io.{BufferedInputStream, FileNotFoundException}
import java.nio.file.{Files, Path}
import java.util.Properties

import com.mchange.sc.v1.log.MLogger
import com.mchange.sc.v1.log.MLevel.*

object Audiofluidity {

  java.util.logging.LogManager.getLogManager.readConfiguration(this.getClass.getClassLoader.getResourceAsStream("logging.properties"))

  given logger : MLogger = MLogger(this)

  @main def go(args : String*) : Unit =

    val config =
      val mbExternal = Option( sys.props(ConfigDirSysProp) ) orElse sys.env.get(ConfigDirEnvVar)
      val mbExternalPath = mbExternal.map( v => Path.of(v) )
      mbExternalPath.foreach { path =>
        if Files.notExists(path) then
          throw new FileNotFoundException(s"Config dir '${path}' does not exist.'")
        else if !Files.isDirectory(path) then
          throw new AudiofluidityException(s"Config dir '${path}' is not a directory!")
      }
      Config(mbExternalPath.getOrElse(Config.DefaultPath))

    val podcastSourceFqcn = DefaultPodcastSourceFqcn

    val sep = sys.props("path.separator")
    val cfgLibJars = config.libJars.mkString(sep)
    val appClassPath = sys.props("java.class.path") + sep + config.tmpClassesDir + sep + cfgLibJars

    def buildConfigPodcast() : Podcast =
      try
        INFO.log("Compiling scala-defined podcast...")
        val podcastSource = compileConfig( podcastSourceFqcn, appClassPath, config )
        INFO.log("Compilation of scala-defined podcast succeeded.")
        podcastSource.toPodcast
      catch
        case ite : java.lang.reflect.InvocationTargetException if ite.getCause.isInstanceOf[scala.NotImplementedError] =>
          SEVERE.log("Please replace all fields marked '???' with valid values in your podcast source!")
          throw ite.getCause

    val cl = this.getClass.getClassLoader

    if args.nonEmpty then
      val command = args.head
      command match
        case "init" =>
          val (build, renderer) =
            try
              val p = buildConfigPodcast()
              (p.build, p.renderer)
            catch
              case _ : Exception => (new Build(), new Renderer.Basic)
          config.initDirs()
          fillInResources(config.configResourceBase, config.configResources, config.basePath, cl)
          build.initDirs()
          fillInResources(build.buildResourceBase, build.buildResources, build.baseDir, cl)
          fillInResources(renderer.srcStaticResourceBase, renderer.srcStaticResources, build.srcStaticDir, cl)
          println("Podcast template initialized.")
        case "clean" =>
          val podcast = buildConfigPodcast()
          recursiveDeleteDirectory(config.tmpDir, leaveTop = true)
          recursiveDeleteDirectory(podcast.build.podcastgenDir, leaveTop = true)
        case "generate" =>
          val podcast = buildConfigPodcast()
          INFO.log("Generating podcast website and RSS feed.")
          generate( podcast )
          INFO.log(s"Successfully generated podcast '${podcast.title}'.")
        case _ => usage()
    else usage()

  def usage() : Unit =
    println("Usage: audiocity <command>")
    println("Commands:")
    println("  clean    -- Deletes generated artifacts from build directory (usually the current working directory)")
    println("  generate -- Compiles and generates a configured podcast")
    println("  init     -- Creates template for a podcast in the build directory (usually the current working directory)")
}
