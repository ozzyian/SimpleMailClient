import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/**
 * The starter window that acts as a log 
 * and collects the data and sets up the different
 * mail sessions.
 */
public class Mailer extends JFrame {

    public static void main(String[] args) {
        new Mailer();
    }

    private JPanel panel = new JPanel();
    private JTextField hostText;
    private JTextField senderText;
    private JTextField userText;
    private JPasswordField passwordText;
    private ComposeSession composeSession;

    public Mailer() {

        panel.setLayout(null);

        JLabel hostLabel = new JLabel("Host");
        hostLabel.setBounds(10, 10, 80, 25);
        panel.add(hostLabel);

        hostText = new JTextField(20);
        hostText.setBounds(100, 10, 160, 25);
        panel.add(hostText);

        JLabel senderLabel = new JLabel("Sender Host");
        senderLabel.setBounds(10, 40, 80, 25);
        panel.add(senderLabel);

        senderText = new JTextField(20);
        senderText.setBounds(100, 40, 160, 25);
        panel.add(senderText);

        JLabel userLabel = new JLabel("User");
        userLabel.setBounds(10, 70, 80, 25);
        panel.add(userLabel);

        userText = new JTextField(20);
        userText.setBounds(100, 70, 160, 25);
        panel.add(userText);

        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setBounds(10, 110, 80, 25);
        panel.add(passwordLabel);

        passwordText = new JPasswordField(20);
        passwordText.setBounds(100, 110, 160, 25);
        panel.add(passwordText);

        JButton loginButton = new JButton("login");
        loginButton.setBounds(100, 150, 80, 25);
        panel.add(loginButton);
        loginButton.addActionListener(ae -> {
            login();
        });

        add(panel);
        setTitle("Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(300, 250);
        setVisible(true);
    }

    /**
     * Creates a session and access the store 
     * with mails if successfull. Creates the composersession and
     * then starts the mailreceiver and disposes itself.
     */
    private void login() {
        Properties props = new Properties();

        props.setProperty("mail.imap.ssl.enable", "true");
        Session recieverSession = Session.getDefaultInstance(props);

        Store store = null;
        try {
            store = recieverSession.getStore("imap");
            store.connect(hostText.getText(), userText.getText(), new String(passwordText.getPassword()));
        } catch (MessagingException e) {
            JOptionPane.showMessageDialog(this, "Wrong credentials, try again.");
            e.printStackTrace();
        }

        if (store != null) {
            composeSession = createComposeSession();
            new MailReceiver(composeSession, store);
            setVisible(false);
            dispose();
        }

    }

    /**
     * Creates the compsing session with the log in 
     * info.
     * @return
     */
    private ComposeSession createComposeSession() {
        Properties props = new Properties();
        props.put("mail.smtp.host", senderText.getText());
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");

        Authenticator auth = new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(userText.getText(), passwordText.getText());
            }
        };
        Session session = Session.getInstance(props, auth);
        return new ComposeSession(userText.getText(), session);
    }

}