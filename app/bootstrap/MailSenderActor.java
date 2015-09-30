package bootstrap; 
/**
 * Created by Alvaro on 18/03/2015.
 */

import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import models.Order;
import models.StatusOrder;
import services.MongoService;
import services.SendEmail;

import java.util.Comparator;
import java.util.Date;

public class MailSenderActor extends UntypedActor {
  LoggingAdapter log = Logging.getLogger(getContext().system(), this);


	static public class Greeting {
	    private final String from;
	 
	    public Greeting(String from) {
	      this.from = from;
	    }
	 
	    public String getGreeter() {
	      return from;
	    }
  	}

  public void onReceive(Object message) throws Exception {
    if (message instanceof String) {
        Order order = MongoService.findOrderById(message.toString());
        if(order!=null){
          String url = "https://musicamiseadmin.herokuapp.com/costumer/unsubscribe/"+order.getEmail();
            SendEmail.sendOrderStatus(order,url);
        }
        log.info("Received String message: {}", message);
        getSender().tell(message, getSelf());
    } else {
        unhandled(message);
    }
  }
}