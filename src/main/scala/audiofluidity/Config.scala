package audiofluidity

import java.io.BufferedInputStream
import java.nio.file.{Files, Path}
import java.util.Properties
import scala.collection.*
import scala.jdk.StreamConverters.*

object Config:
  val DefaultPath           = Path.of("config")
  val LibNameInConfig       = Path.of("lib")
  val ScalaDirNameInConfig  = Path.of("scala")
  val TmpNameInConfig       = Path.of("tmp")
  val ClassesNameInTmp      = Path.of("classes")

  val AudiofluidityPropertiesNameInConfig = Path.of("audiofluidity.properties")
  val ExpectedPodcastSourcePathInConfig = Path.of("scala","AudiofluiditySource.scala")


import Config.*

class Config( val basePath : Path = Config.DefaultPath ):
  val libDir                = basePath.resolve(LibNameInConfig)
  val scalaDir              = basePath.resolve( ScalaDirNameInConfig )
  val tmpDir                = basePath.resolve( TmpNameInConfig )
  val tmpClassesDir         = tmpDir.resolve( ClassesNameInTmp )

  private val audiofluidityPropertiesFile = basePath.resolve(AudiofluidityPropertiesNameInConfig)

  private lazy val configProps =
    val tmp = new Properties()
    if Files.exists(audiofluidityPropertiesFile) then
      val is = new BufferedInputStream( Files.newInputStream(audiofluidityPropertiesFile) )
      try tmp.load( is ) finally is.close()
    tmp

  def prop( key : String ) = configProps.getProperty(key)

  def initDirs() : Unit =
    Files.createDirectories(libDir)        // makes base config dir if necessary along the way
    Files.createDirectories(scalaDir)
    Files.createDirectories(tmpClassesDir) // makes tmpDir along the way

  def expectedPodcastSourcePath = basePath.resolve(ExpectedPodcastSourcePathInConfig)

  def configResourceBase : Path = Path.of("initsite", "config")

  def configResources = immutable.Set(ExpectedPodcastSourcePathInConfig)

  def expectedPodcastSourceFile = basePath.resolve(ExpectedPodcastSourcePathInConfig)

  lazy val libJars : List[Path] =
    if Files.exists(libDir) then
      Files.list(libDir).toScala(List).filter(_.toString.endsWith(".jar"))
    else
      Nil