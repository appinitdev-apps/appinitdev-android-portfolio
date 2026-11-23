



class GmailSender(
    private val remitente: String = "jasibeyamilethsarmientoramirez@gmail.com",
    private val appPassword: String = ""
) {
    fun interface EmailCallback {
        fun onResult(success: Boolean)
    }

    fun enviarCorreo(destinatario: String, asunto: String, cuerpo: String, callback: EmailCallback) {
        Thread {
            try {
                val props = Properties().apply {
                    put("mail.smtp.auth", "true")
                    put("mail.smtp.starttls.enable", "true")
                    put("mail.smtp.host", "smtp.gmail.com")
                    put("mail.smtp.port", "587")
                }

                val session = Session.getInstance(props, object : Authenticator() {
                    override fun getPasswordAuthentication(): PasswordAuthentication {
                        return PasswordAuthentication(remitente, appPassword)
                    }
                })

                val message = MimeMessage(session).apply {
                    setFrom(InternetAddress(remitente))
                    addRecipient(Message.RecipientType.TO, InternetAddress(destinatario))
                    subject = asunto
                    setText(cuerpo)
                }

                Transport.send(message)
                callback.onResult(true)

            } catch (e: Exception) {
                Log.e("GmailSender", "Error: ${e.javaClass.simpleName} - ${e.message}")
                callback.onResult(false)
            }
        }.start()
    }
}
