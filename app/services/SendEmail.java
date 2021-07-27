package services;

import models.Order;
import models.StatusOrder;
import org.apache.commons.mail.EmailAttachment;
import play.Configuration;
import play.Logger;
import play.Play;
import play.libs.mailer.Email;
import play.mvc.Controller;
import play.mvc.Result;

import java.io.File;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import play.libs.mailer.MailerPlugin;
import models.*;

public class SendEmail {




    public static void send() {
        Email email = new Email();
        email.setSubject("Simple email");
        email.setFrom("Mister FROM <administrador@musicamise.com.br>");
        email.addTo("Miss TO <>");
// adds attachment
        // email.addAttachment("attachment.pdf", new File("/some/path/attachment.pdf"));
// adds inline attachment from byte array
        //email.addAttachment("data.txt", "data".getBytes(), "text/plain", "Simple data", EmailAttachment.INLINE);
// sends text, HTML or both...
        email.setBodyText("A text message");
        email.setBodyHtml("<html><body><p>An <b>html</b> message</p></body></html>");
        MailerPlugin.send(email);
    }

    public void sendEmail2() {
        Email email = new Email();
        email.setSubject("Simple email");
        email.setFrom("Mister FROM <from@email.com>");
        email.addTo("Miss TO <to@email.com>");
        // adds attachment
        email.addAttachment("attachment.pdf", new File("/some/path/attachment.pdf"));
        // adds inline attachment from byte array
        email.addAttachment("data.txt", "data".getBytes(), "text/plain", "Simple data", EmailAttachment.INLINE);
        // sends text, HTML or both...
        email.setBodyText("A text message");
        email.setBodyHtml("<html><body><p>An <b>html</b> message</p></body></html>");
        // configures the mailer before sending the email
        Map<String, Object> conf = new HashMap<>();
        conf.put("host", "typesafe.org");
        conf.put("port", 1234);
//        mailerClient.configure(new Configuration(conf)).send(email);
    }

    public static void sendOrderStatus(Order order,String unsubUrl) {
        try {

            String  emailTo = order.getEmail()!=null||!order.getEmail().equals("")?order.getEmail():order.getUser().getEmail();
            if(emailTo!=null||!emailTo.equals("")){
                Email email = new Email();
                email.setSubject("Musicamise; Status de sua compra.");
                email.setFrom("Equipe Musicamise <administrador@musicamise.com.br>");
                email.addTo("Sr(a) <{email}>".replace("{email}",emailTo));

                // adds attachment
                // email.addAttachment("attachment.pdf", new File("/some/path/attachment.pdf"));
                // adds inline attachment from byte array
                //email.addAttachment("data.txt", "data".getBytes(), "text/plain", "Simple data", EmailAttachment.INLINE);
                // sends text, HTML or both...
                email.setBodyText("A text message");

                StatusOrder lastSatus = order.getLastStatus();
                email.setBodyHtml(views.html.EmailTemplateOrderStatus.render(order,lastSatus,unsubUrl).toString());
                MailerPlugin.send(email);
                //save that you sent a email with that status
                Order.EmailSent emailSent = order.new EmailSent();
                emailSent.setStatus(lastSatus.getStatus());

                emailSent.setStatusEntrega(Utils.StatusEntrega.getStatusByName(order.getStatusEntrega()));

                MongoService.updateOrderEmailSent(order.getId(),emailSent);
            }

        }catch (Exception e){
            Logger.error("error on email"+e.toString());
        }

    }
}
