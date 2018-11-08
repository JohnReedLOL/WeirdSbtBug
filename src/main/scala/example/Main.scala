package example

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
