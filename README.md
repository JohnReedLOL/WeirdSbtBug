#### Testing Canary

At my previous job, a "canary" was a term used to describe a system that continually runs tests and, if a failure occurs, sends out emails.

This is a demo of a very simple canary that sends email via the Apache-Commons-Email library (https://commons.apache.org/proper/commons-email/).

To demo the email testing functionality, run Main and then sign into the dummy email account to see the email.

To run the canary continuously, execute this task from the sbt shell:

> runCanaryTask

This task will generate a random number every 5 seconds. If the random number is odd, it will send an email.

Note that to enable login, I turned off the email security settings in Gmail via these steps:

- Go to: https://myaccount.google.com/
- Sign in as "UsernameForAutomatedTesting@gmail.com"
- Go to: Sign-in & Security
- 2-Step Verification: Off
- Allow less secure apps: ON

Do not use a dummy email account for anything other than automatically generated email on test failure.

Note that if you know the network carrier (ex. AT&T, Sprint, T-Mobile, or Verizon) and the phone number associated with a cell phone, it is possible to send an email that gets received as a text message.

See: https://www.lifewire.com/sms-gateway-from-email-to-sms-text-message-2495456

Also, it is possible to extract the email address of the last person who pushed a code change by parsing the output of the "git log" command.
This can be used to bother people who broke the code.

By using a testing canary, it is possible to continuously make sure that the codebase is working.

Note: If you cannot access GitHub, here is some sample code:

build.sbt:
```
name := "EmailTestingBot"

organization := "com.example"

scalaVersion := "2.11.8"

version      := "0.1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  // https://mvnrepository.com/artifact/org.apache.commons/commons-email
  "org.apache.commons" % "commons-email" % "1.5"
)
```

Main:
```
object Main {
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
    println(s"sendResult: $sendResult")
  }

  def main(args: Array[String]): Unit = {
    val emailSubject = "Email_Subject"
    val emailBody = "Email_Body"
    val emailRecipient = myEmailAddress // Defaults to sending email to yourself.
    try {
      sendEmail(emailSubject, emailBody, emailRecipient)
    } catch {
      case e: Exception => println(s"Attempting to send the email to '$emailRecipient' failed with this exception: ${e.getMessage}")
    } finally {
      println(s"Finished sending email from: '$myEmailAddress' to: '$emailRecipient'")
    }
  }
}
```

project/build.sbt:
```
libraryDependencies ++= Seq(
  // https://mvnrepository.com/artifact/org.apache.commons/commons-email
  "org.apache.commons" % "commons-email" % "1.5"
)
```

See: https://stackoverflow.com/questions/34828688/how-can-i-use-a-library-dependency-in-the-definition-of-an-sbt-task 

runCanaryTask definition in build.sbt:
```
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
```

You should see output that looks like this:

```
[info] Set current project to EmailTestingBot (in build file:.../EmailTestingBot/)
[info] Not sending email.
[info] Sending email.
[info] sendResult: <1452870640.0.1541644807523@mycomputer>
[info] Finished sending email from: 'UsernameForAutomatedTesting@gmail.com' to: 'UsernameForAutomatedTesting@gmail.com'
```

