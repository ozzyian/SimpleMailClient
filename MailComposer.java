import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.swing.*;
import java.awt.GridLayout;


/**
 * Class that handles the UI and functionality
 * of composing and sending a mail.
 */
public class MailComposer extends JFrame {


    private JTextField to = new JTextField();
    private JTextField subject = new JTextField();
    private JButton send = new JButton("Send");
    private JTextArea mailContent = new JTextArea();
    private JPanel buttonPanel = new JPanel();
    private ComposeSession session;
    private AttachmentList attachments = new AttachmentList(false);
    private JCheckBox sendAsHTML = new JCheckBox("HTML");

    /**
     * Construcor which initates the UI and session.
     * @param composeSession the info of the compose session
     */
    public MailComposer(ComposeSession composeSession) {
        this.session = composeSession;
        JLabel toLabel = new JLabel("To:");
        JLabel fromLabel = new JLabel("From: ");
        JLabel from = new JLabel(session.user);
        JLabel subjectJLabel = new JLabel("Subject:");
        JPanel infoContent = new JPanel();
        mailContent.setLineWrap(true);
        infoContent.setLayout(new GridLayout(3, 2));
        infoContent.add(toLabel);
        infoContent.add(to);
        infoContent.add(fromLabel);
        infoContent.add(from);
        infoContent.add(subjectJLabel);
        infoContent.add(subject);
        buttonPanel.add(sendAsHTML);
        buttonPanel.add(send);
        buttonPanel.add(attachments);
        send.addActionListener(ae -> {
            send();
        });

        add("North", infoContent);
        add("Center", new JScrollPane(mailContent));
        add("South", buttonPanel);

        setSize(1024, 800);
        setVisible(true);

    }

    /**
     * Composes the mail depending on if its a html or just plain text.
     * Checks if there are any attachments in the attachmentlist object
     * and creates necessary parts for it to be added to the mail.
     * Lastly sends the mail.
     */
    public void send() {
        try {
            String contentType = sendAsHTML.isSelected() ? "text/html" : "text/plain";
            Message message = new MimeMessage(session.session);
            message.setFrom(new InternetAddress(session.user));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to.getText()));
            message.setSubject(subject.getText());
            BodyPart textContent = new MimeBodyPart();
            textContent.setContent(mailContent.getText(), contentType);
            Multipart multiPart = new MimeMultipart();

            if (!attachments.isEmpty()) {
                Attachment[] attachmentArray = attachments.getAttachments();
                for (int i = 0; i < attachmentArray.length; i++) {
                    multiPart.addBodyPart(attachmentArray[i].getBodyPart());
                    
                }
            }
            multiPart.addBodyPart(textContent);
            message.setContent(multiPart);
            Transport.send(message);

            subject.setText("");
            to.setText("");
            mailContent.setText("");
            attachments.clearAll();
            JOptionPane.showMessageDialog(this, "Mail was successfully sent!", "Mail sending status", JOptionPane.INFORMATION_MESSAGE);
        } catch (MessagingException e) {
            JOptionPane.showMessageDialog(this, "Mail could not be sent!", "Mail sending status", JOptionPane.ERROR_MESSAGE);
        }

    }

}