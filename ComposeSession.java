import javax.mail.Session;

/**
 * Helper class to keep the info of a send mail 
 * session through login to sending mail.
 */
public class ComposeSession {

    public String user;
    public Session session;

    public ComposeSession(String user, Session session) {
        this.user = user;
        this.session = session;
    }

}