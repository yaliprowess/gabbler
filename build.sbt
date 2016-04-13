lazy val gabbler = project
  .copy(id = "gabbler")
  .in(file("."))
  .enablePlugins(AutomateHeaderPlugin, GitVersioning)
  .aggregate(gabblerUser)

lazy val gabblerUser = project
  .copy(id = "gabbler-user")
  .in(file("gabbler-user"))
  .enablePlugins(AutomateHeaderPlugin, JavaAppPackaging, DockerPlugin)

name := "gabbler"

unmanagedSourceDirectories.in(Compile) := Vector.empty
unmanagedSourceDirectories.in(Test)    := Vector.empty

publishArtifact := false
