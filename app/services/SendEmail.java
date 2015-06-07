package services;

import models.User;
import play.Play;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

public class SendEmail {

    public static String email = Play.application().configuration().getString("send.email.sender");
    public static String password = Play.application().configuration().getString("send.email.pass");
    public static String emailContact = Play.application().configuration().getString("send.email.contact");


    public static void sendMail(){
		
		// Recipient's email ID needs to be mentioned.
		String to = "renato.moura@sodet.biz";
		String marcio = "marcio@sodet.biz";

		// Sender's email ID needs to be mentioned
		String from = "renato.moura@sodet.biz";

		// Assuming you are sending email from localhost
		String host = "localhost";

		// Get system properties
		Properties properties = System.getProperties();

		// Setup mail server
		properties.setProperty("mail.smtp.host", host);

		// Get the default Session object.
		Session session = Session.getDefaultInstance(properties);

		try{
			// Create a default MimeMessage object.
			MimeMessage message = new MimeMessage(session);

			// Set From: header field of the header.
			message.setFrom(new InternetAddress(from));

			// Set To: header field of the header.
			message.addRecipient(Message.RecipientType.TO,
					new InternetAddress(to));
			
			message.addRecipient(Message.RecipientType.TO,
								new InternetAddress(marcio));

			// Set Subject: header field
			message.setSubject("Problema no Crawler de cinemas!");

			// Now set the actual message
			message.setText("Algum problema ocorreu no crawler do yahoo... O code não foi 200!");

			// Send message
			Transport.send(message);
		}catch(javax.mail.MessagingException mex) {
			mex.printStackTrace();
		}
	}

    public static void sendEmailEsqueciSenha(String to,String fullUrl) throws MessagingException {
        // Recipient's email ID needs to be mentioned.


        // Sender's email ID needs to be mentioned
        String from = "cinemas@sodet.biz";

        // Assuming you are sending email from localhost
        String host = "localhost";

        // Get system properties
        Properties properties = System.getProperties();

        // Setup mail server
        properties.setProperty("mail.smtp.host", host);

        // Get the default Session object.
        Session session = Session.getDefaultInstance(properties);

            // Create a default MimeMessage object.
            MimeMessage message = new MimeMessage(session);

            // Set From: header field of the header.
            message.setFrom(new InternetAddress(from));

            // Set To: header field of the header.
            message.addRecipient(Message.RecipientType.TO,
                    new InternetAddress(to));

            // Set Subject: header field
            message.setSubject("Redefinir senha Porjeto Sonae Cinemas");

            // Now set the actual message


            message.setText("Para redefinir a senha click no seguinte Link:<br/> <a href='"+fullUrl+"'>Redefinir senha</a>","text/html");

            // Send message
            Transport.send(message);
    }

    public static void sendGmailEsqueciSenha(String to,String fullUrl) throws MessagingException {
        // Recipient's email ID needs to be mentioned.

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");

        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication("", "");
                    }
                });

        try {

            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress("from@no-spam.com"));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(to));
            message.setSubject("Testing Subject");
            message.setContent("Para redefinir a senha click no seguinte Link:<br/> <a href='" + fullUrl + "'>Redefinir senha</a>", "text/html");

            Transport.send(message);

            System.out.println("Done");

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    public static void sendEmailAtiveCount(User user,String fullUrl) throws MessagingException {
        // Recipient's email ID needs to be mentioned.
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");




        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(email,password);
                    }
                });

        // try {

        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress("from@no-spam.com"));
        message.setRecipients(Message.RecipientType.TO,
                InternetAddress.parse("user.getEmail()"));
        message.setSubject("Ative sua conta");
        message.setContent("Para ativar a senha segue o Link:<br/> <a href='" + fullUrl + "'>Ative sua conta</a>", "text/html");

        Transport.send(message);

        System.out.println("Done");

        // } catch (MessagingException e) {
        //     throw new RuntimeException(e);
        // }
    }

    public static void sendEmailForgetPass(User user,String fullUrl) throws MessagingException {
        // Recipient's email ID needs to be mentioned.
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");


        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(email, password);
                    }
                });

        // try {

        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress("from@no-spam.com"));
        message.setRecipients(Message.RecipientType.TO,
                InternetAddress.parse("user.getEmail()"));
        message.setSubject("Redefinir sua senha");
        message.setContent("Para redefinir acesse o Link:<br/> <a href='" + fullUrl + "'>Redefina sua senha</a>", "text/html");

        Transport.send(message);

        System.out.println("Done");

        // } catch (MessagingException e) {
        //     throw new RuntimeException(e);
        // }
    }

    public static void sendEmailContact(String name,String messageContent, String emailContent,String subject) throws MessagingException {
        // Recipient's email ID needs to be mentioned.
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");


        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(email, password);
                    }
                });

        // try {

        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress("from@no-spam.com"));
        message.setRecipients(Message.RecipientType.TO,
                InternetAddress.parse(emailContact));
        message.setSubject("Contato do Site");
        message.setContent("Nome: "+name+" <br/> email: "+emailContent+"<br/> subject: "+subject+"<br/>message: "+messageContent, "text/html");

        Transport.send(message);

        System.out.println("Done");

        // } catch (MessagingException e) {
        //     throw new RuntimeException(e);
        // }
    }

}