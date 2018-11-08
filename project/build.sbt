// Note: sbt's build is recursive - it is a build inside a build.
// https://stackoverflow.com/questions/34828688/how-can-i-use-a-library-dependency-in-the-definition-of-an-sbt-task

// This lets me use "commons-email" in the sbt task runCanaryTask
libraryDependencies ++= Seq(
  "org.apache.commons" % "commons-email" % "1.5"
)
