import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import javax.mail.*;
import javax.mail.internet.MimeBodyPart;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * Handles the interpreting of the mail contents. Can show html5 and css and
 * make attachments able for download.
 */
public class MailHandler extends JFrame {

    private JFXPanel jfxPanel = new JFXPanel();
    private WebEngine engine;
    private JPanel panel = new JPanel(new BorderLayout());
    private JPanel northPanel = new JPanel(new GridLayout(2, 1));
    private JPanel southPanel = new JPanel();

    private MailItem mailContent;
    private JLabel from = new JLabel();
    private JLabel subject = new JLabel();
    private AttachmentList attachments = new AttachmentList(true);

    public MailHandler(MailItem item) {
        super();
        initComponents();
        setVisible(true);
        mailContent = item;
        try {
            loadMailContent();
        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * creates the different components and initates them to the frame.
     */
    private void initComponents() {
        createScene();
        panel.add(jfxPanel, BorderLayout.CENTER);

        northPanel.add(from);
        northPanel.add(subject);

        southPanel.add(attachments);
        add("North", northPanel);
        add("Center", panel);
        add("South", southPanel);
        setPreferredSize(new Dimension(1024, 1024));
        pack();
    }

    /**
     * Creates the JavaFX components
     */
    private void createScene() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                WebView view = new WebView();
                engine = view.getEngine();
                jfxPanel.setScene(new Scene(view));
            }
        });
    }

    /**
     * Takes the content of a mail as string and sets the from and subject labels
     * and then loads the content in its own thread to the engine for display.
     * 
     * @throws MessagingException
     * @throws IOException
     */
    public void loadMailContent() throws MessagingException, IOException {
        String content = getTextAndAttachment(mailContent.getMessage());
        from.setText("From: " + mailContent.getFrom());
        subject.setText("Subject: " + mailContent.getSubject());
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                engine.loadContent(content);
            }
        });
    }

    /**
     * Iterates over the different bodyparts over the mail object recursively if
     * there are parts inside a part etc. Tries to get the right parts by their
     * mimetype and adds them to their respective place.
     */
    private String getTextAndAttachment(Part p) throws MessagingException, IOException {

        if (p.isMimeType("multipart/mixed")) {
            Multipart mp = (Multipart) p.getContent();
            for (int i = 0, n = mp.getCount(); i < n; i++) {
                MimeBodyPart part = (MimeBodyPart) mp.getBodyPart(i);
                if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
                    attachments.addAttachment(part);
                }
            }
        }

        if (p.isMimeType("text/html")) {
            String s = (String) p.getContent();
            return s;

        } else if (p.isMimeType("text/plain")) {
            String s = (String) p.getContent();
            return s;
        }

        if (p.isMimeType("multipart/*")) {
            Multipart mp = (Multipart) p.getContent();
            String text = null;
            for (int i = 0; i < mp.getCount(); i++) {
                Part bp = mp.getBodyPart(i);
                if (bp.isMimeType("text/plain")) {
                    if (text == null)
                        text = getTextAndAttachment(bp);
                    continue;
                } else if (bp.isMimeType("text/html")) {
                    String s = getTextAndAttachment(bp);
                    if (s != null)
                        return s;
                } else {
                    return getTextAndAttachment(bp);
                }
            }
            return text;
        } 
        
        return "";
    }

}
