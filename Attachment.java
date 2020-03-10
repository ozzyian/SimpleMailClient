import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;


/***
 * Helper class that stores the bodypart
 * with the file which it can return and return
 * the name of the file.
 */
public class Attachment {
    private String fileName;
    private MimeBodyPart part;

    public Attachment(MimeBodyPart part) throws MessagingException {
        this.part = part;
        fileName = part.getFileName();
    }
    /**
     * returns the bodypart
     * @return bodypart with file inside
     */
    public MimeBodyPart getBodyPart() {
        return part;
    }
    /**
     * returns the name of the file
     */
    @Override
    public String toString() {
        return fileName;
    }

}