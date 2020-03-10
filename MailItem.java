import javax.mail.Message;
import javax.mail.MessagingException;


/**
 * Helper class that saves the info needed
 * to display the information of message object
 * in a JList.
 */
public class MailItem {

    private int number;
    private String from;
    private String subject;
    private Message message;

    public MailItem(Message message) {
        this.message = message;
        try {
            this.number = message.getMessageNumber();
            this.from = ""+message.getFrom()[0];
            this.subject = message.getSubject();
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
    public String toString(){
        String messageNumber = "Message: " + this.number;
        String from = " From: " + this.from;
        String subject = "Subject: " + this.subject;
        return messageNumber + from + subject;
    }
    public int getNumber(){
        return number;
    }
    public Message getMessage(){
        return this.message;
    }
    public String getFrom(){
        return from;
    }
    public String getSubject(){
        return subject;
    }
}