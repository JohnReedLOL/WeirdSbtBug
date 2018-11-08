name := "EmailTestingBot"
organization := "com.example"
scalaVersion := "2.11.8"
version      := "0.1.0-SNAPSHOT"

// Can compile on multiple different Scala versions
crossScalaVersions := Seq("2.11.8", "2.12.7", "2.13.0-M2")

scalacOptions ++= Seq(
"-Xlint",
"-encoding", "utf-8",                // Specify character encoding used by source files.
"-Ywarn-unused-import",              // Warn if an import selector is not referenced.
"-deprecation",                      // Emit warning and location for usages of deprecated APIs.
"-encoding", "utf-8",                // Specify character encoding used by source files.
"-explaintypes",                     // Explain type errors in more detail.
"-feature",                          // Emit warning and location for usages of features that should be imported explicitly.
"-language:existentials",            // Existential types (besides wildcard types) can be written and inferred
"-language:experimental.macros",     // Allow macro definition (besides implementation and application)
"-language:higherKinds",             // Allow higher-kinded types
"-language:implicitConversions",     // Allow definition of implicit functions called views
"-unchecked",                        // Enable additional warnings where generated code depends on assumptions.
"-Ywarn-dead-code",                  // Warn when dead code is identified.
"-Ywarn-inaccessible",               // Warn about inaccessible types in method signatures.
"-Ywarn-infer-any",                  // Warn when a type argument is inferred to be `Any`.
"-Ywarn-nullary-override",           // Warn when non-nullary `def f()' overrides nullary `def f'.
"-Ywarn-nullary-unit",               // Warn when nullary methods return Unit.
"-Ywarn-numeric-widen",              // Warn when numerics are widened.
"-Ywarn-value-discard",              // Warn when non-Unit expression results are unused.
"-Ywarn-unused",
"-Xfatal-warnings"
)

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.0.5" % Test,
  // https://mvnrepository.com/artifact/org.apache.commons/commons-email
  "org.apache.commons" % "commons-email" % "1.5"
)

// Now let's define some custom sbt settings and tasks
// Note: A Setting just contains a value, a Task executes something and then returns a value

val runCanaryTask = taskKey[Unit]("A task that randomly decides to send an email or not. " +
  "It is meant to emulate a scenario where a test failure results in a notification.")

runCanaryTask in Scope.GlobalScope := {
  // runCanaryTask dependencies:
  val log = streams.value.log // streams task happens-before runCanaryTask

  // ---- runCanaryTask begins here ----
  // This function has a 50% chance of returning true.
  def decideRandomly(): Boolean = {
    val rand = new java.util.Random()
    val n = rand.nextInt(10) + 1
    // 10 is the maximum and the 1 is the minimum
    if(n % 2 == 1) { // odd
      true
    } else { // even
      false
    }
  }
  // This loops forever
  while(true) {
    val sendEmail = decideRandomly()
    if(sendEmail) {
      log.info(s"Sending email.")

      val emailUsername = "UsernameForAutomatedTesting"
      val emailDomain = "@gmail.com"
      val emailPassword = "TestingPassword"
      val myEmailAddress = s"$emailUsername$emailDomain"

      /**
        * Sends an email with the given subject and body to the specified recipient.
        *
        * @param subject   The subject of the email.
        * @param body      The body of the email.
        * @param recipient The recipient of the email.
        */
      def sendEmail(subject: String, body: String, recipient: String): Unit = {
        assert(recipient.contains("@"))
        assert(recipient.contains("."))
        import org.apache.commons.mail._
        val email: Email = new SimpleEmail()
        email.setHostName("smtp.googlemail.com")
        val portNumber = 465
        email.setSmtpPort(portNumber)
        email.setAuthenticator(new DefaultAuthenticator(emailUsername, emailPassword))
        val sendResult = email.setSSLOnConnect(true)
          .setFrom(myEmailAddress)
          .setSubject(subject)
          .setMsg(body)
          .addTo(recipient)
          .send()
        log.info(s"sendResult: $sendResult")
      }
      val emailSubject = "Autoreply: Test Internet Failure"
      val emailBody = "A test failed. It is all your fault!"
      val emailRecipient = myEmailAddress // Defaults to sending email to yourself.
      try {
        sendEmail(emailSubject, emailBody, emailRecipient)
      } catch {
        case e: Exception => log.info(s"Attempting to send the email to '$emailRecipient' failed with this exception: ${e.getMessage}")
      } finally {
        log.info(s"Finished sending email from: '$myEmailAddress' to: '$emailRecipient'")
      }
    } else {
      log.info(s"Not sending email.")
    }
    val durationInMs = 5000
    Thread.sleep(durationInMs)
  }
}
