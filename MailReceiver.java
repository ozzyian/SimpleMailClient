import javax.mail.*;

import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.event.WindowEvent;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.awt.event.WindowAdapter;

import javax.swing.*;

/**
 * Class acts as the main window of the program which contains a list of folders
 * and mails inside them.
 */
public class MailReceiver extends JFrame {

    private ComposeSession composeSession;
    private Store store;
    private Folder currentFolder;
    private Message[] messages;
    private int mailIndex = 0;
    private int mostRecent = 0;
    private LinkedList<String> folderPath = new LinkedList<>();

    private JPanel centerPanel = new JPanel();
    private JPanel south = new JPanel(new GridLayout(1, 4));

    private JButton compose = new JButton("Compose");
    private JButton getMail = new JButton("Load 10 mail");
    private JButton openMail = new JButton("Open Mail");
    private JButton reload = new JButton("Reload current folder");
    private JButton openFolder = new JButton("Open folder");
    private JButton goBack = new JButton("Go to parent of current folder");

    private JPanel eastSouthPanel = new JPanel(new GridLayout(2, 1));
    private DefaultListModel<MailItem> listMailData;
    private JList<MailItem> mailList;

    private DefaultListModel<String> folderListData;
    private JList<String> folderList;

    /**
     * Initiates the store and tries to get the folders and display them in a JList.
     * 
     * @param composeSession the created compsession to send mail
     * @param store          the store to open and get folders/messages
     */
    public MailReceiver(ComposeSession composeSession, Store store) {
        this.composeSession = composeSession;
        this.store = store;

        folderListData = new DefaultListModel<String>();
        folderList = new JList<String>(folderListData);
        folderList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        openFolder.addActionListener(ae -> {
            if (!folderList.isSelectionEmpty()) {
                mailIndex = 0;
                mostRecent = 0;
                System.out.println("button " + folderPath.toString());
                System.out.println("button " + folderPath.size());
                openFolder(folderList.getSelectedValue());

            }
        });

        listMailData = new DefaultListModel<MailItem>();
        mailList = new JList<MailItem>(listMailData);
        mailList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        centerPanel.setLayout(new GridLayout(1, 1));
        centerPanel.add(new JScrollPane(mailList));

        JPanel eastPanel = new JPanel(new BorderLayout());
        eastPanel.add("North", new JLabel("Folders"));
        eastPanel.add("Center", folderList);
        eastSouthPanel.add(goBack);
        eastSouthPanel.add(openFolder);
        eastPanel.add("South", eastSouthPanel);

        add("Center", centerPanel);
        add("East", eastPanel);

        compose.addActionListener(ae -> {
            new Thread() {
                public void run() {
                    new MailComposer(composeSession);
                }
            }.start();

        });

        getMail.setEnabled(false);
        getMail.addActionListener(ae -> {
            getMail();
        });

        openMail.addActionListener(ae -> {
            if (!mailList.isSelectionEmpty()) {
                MailItem m = mailList.getSelectedValue();
                new MailHandler(m);
            }
        });

        reload.addActionListener(ae -> {
            mailIndex = 0;
            listMailData.removeAllElements();
            try {
                if (currentFolder != null && currentFolder.getType() != 2)
                    openFolder(currentFolder.getName());
            } catch (MessagingException e) {
                e.printStackTrace();
            }
        });

        goBack.addActionListener(ae -> {
            
            backFolderPath();

            
        });
        south.add(compose);
        south.add(getMail);
        south.add(openMail);
        south.add(reload);
        add("South", south);

        try {
            populateFolderList(null);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        addWindowListener(new WL());
        setSize(800, 500);
        setVisible(true);

    }

    /**
     * Updates the folderpath
     */
    private void backFolderPath() {
        if(folderPath.size() != 0){
            folderPath.removeLast();
            //listMailData.clear();
        }
        System.out.println("back " + folderPath.toString());
        System.out.println(folderPath.peek());
        System.out.println(folderPath.size());

        if (folderPath.size() == 0) {
            try {
                populateFolderList(null);
            } catch (MessagingException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * Tries to show the different folders located in the store object.
     * 
     * @throws MessagingException
     */
    private void populateFolderList(String folder) throws MessagingException {
        Folder[] folders = null;
        folderListData.clear();
        if (folder == null) {
            folders = store.getDefaultFolder().list();
        } else {
            folders = store.getFolder(folder).list();
        }

        for (Folder f : folders) {
            folderListData.addElement(f.getName());
        }
    }

    /**
     * Opens a currentFolder and tries to get the messages from it.
     */
    private void openFolder(String folderName) {
        try {
            Folder openFolder = store.getFolder(folderName);
            if (folderPath.size() == 0 && openFolder.getType() == 2) {
                folderPath.addLast(folderName);
                currentFolder = openFolder;
                populateFolderList(folderName);
                return;
            }
            if(folderPath.size() == 0 && openFolder.getType() ==3){
                System.out.println(openFolder.getName());
                openFolder.open(Folder.READ_ONLY);
                currentFolder = openFolder;
                messages = openFolder.getMessages();
                getMail();
                return;
            }
            
            Folder deepFolder = store.getFolder(String.join("/",folderPath)+"/"+folderName);
            if(deepFolder.getType() == 2){
                folderPath.addLast(folderName);
                currentFolder = deepFolder;
                populateFolderList(deepFolder.getName());
                return;
            }

            if(deepFolder.getType() == 3){
                deepFolder.open(Folder.READ_ONLY);
                messages = deepFolder.getMessages();
                currentFolder = deepFolder;
                listMailData.clear();
                getMail();
                return;
            }
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    /**
     * Iterates over the loaded messages and displays ten of them at a time in its
     * own thread.
     * 
     */
    private void getMail() {
        getMail.setEnabled(false);
        mostRecent = messages.length - 1 - mailIndex;
        new Thread() {
            public void run() {
                int range = 9;
                if (range > mostRecent) {
                    range = mostRecent;

                }
                for (int i = mostRecent; i >= mostRecent - range; i--) {
                    Message current = messages[i];
                    MailItem item = new MailItem(current);
                    listMailData.addElement(item);
                    mailIndex++;
                }
                getMail.setEnabled(true);
            }
        }.start();
    }

    /**
     * Listener that closes the store and currentFolder when window is closing.
     */
    class WL extends WindowAdapter {
        @Override
        public void windowClosing(WindowEvent e) {
            try {
                if (currentFolder != null && currentFolder.isOpen())
                    currentFolder.close(true);
                store.close();
            } catch (MessagingException ex) {
                ex.printStackTrace();
            }
            System.exit(0);
        }
    }
}