lazy val gabbler = project
  .copy(id = "gabbler")
  .in(file("."))
  .enablePlugins(AutomateHeaderPlugin, GitVersioning)

name := "gabbler"

libraryDependencies ++= Vector(
  Library.scalaCheck % "test"
)

initialCommands := """|import de.heikoseeberger.gabbler._
                      |""".stripMargin
