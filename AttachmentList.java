import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.awt.Dimension;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.swing.*;


/**
 * Class works as a modified JList to show
 * what attachments are sent with a mail.
 */
public class AttachmentList extends JPanel {

    private DefaultListModel<Attachment> attachmentPartsData;
    private JList<Attachment> attachmentParts;
    private JButton addAttachment;
    private JButton removeAttachment;
    private JButton getAttachments;
    private JFileChooser fileChooser;

    /**
     * Construcor which initates the components and listeners
     * differently depending on if the class will be used when 
     * composing a mail or looking up a received mail.
     * @param receivedAttachments initates for received mails if true, false initiates for mail composing.
     */
    public AttachmentList(boolean receivedAttachments) {
        fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
        attachmentPartsData = new DefaultListModel<Attachment>();
        attachmentParts = new JList<Attachment>(attachmentPartsData);
        attachmentParts.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        attachmentParts.setPreferredSize(new Dimension(200, 100));
        if (receivedAttachments) {
            initReceivedAttachments();
        } else {
            initComposeAttachments();
        }

    }

    /**
     * Initiates the filechooser to choose directory and
     * make the download attachments button disabled when 
     * no items in the list is selected. Also initiates 
     * the download button with an actionlistener that 
     * gets the selected attachments and downloads them to the selected 
     * directory which executes on its own thread.
     */
    private void initReceivedAttachments() {
        getAttachments = new JButton("Download Attachment(s)");
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        attachmentParts.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting() == false) {
                if (attachmentParts.getSelectedIndex() == -1) {
                    getAttachments.setEnabled(false);
                } else {
                    getAttachments.setEnabled(true);
                }
            }
        });

        getAttachments.addActionListener(ae -> {
            if (!attachmentParts.isSelectionEmpty()) {
                int returnValue = fileChooser.showSaveDialog(this);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    getAttachments.setEnabled(false);
                    ArrayList<Attachment> attachments = (ArrayList<Attachment>) attachmentParts.getSelectedValuesList();
                    new Thread() {
                        public void run() {
                            try {
                                for (Attachment attachment : attachments) {
                                    System.out.println(fileChooser.getCurrentDirectory() + " " + attachment.toString());
                                    attachment.getBodyPart().saveFile(
                                            new File(fileChooser.getCurrentDirectory(), attachment.toString()));
                                }
                                getAttachments.setEnabled(true);
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (MessagingException e) {
                                e.printStackTrace();
                            }
                        }
                    }.start();
                }
            }
        });
        getAttachments.setEnabled(false);

        add(getAttachments);
        add(new JScrollPane(attachmentParts));

    }

    /**
     * initiates listener to remove diabled remove 
     * button if no item in list is selected and vice versa.
     * Adds a actionListener to addAttachment button which adds
     * the selected file from the filechooser to a mimebodypart 
     * and creates an attachment object and adds it to the list.
     */
    private void initComposeAttachments() {

        attachmentParts.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting() == false) {
                if (attachmentParts.getSelectedIndex() == -1) {
                    removeAttachment.setEnabled(false);
                } else {
                    removeAttachment.setEnabled(true);
                }
            }
        });

        addAttachment = new JButton("Add Attachment");
        removeAttachment = new JButton("Remove Attachment");
        removeAttachment.setEnabled(false);

        addAttachment.addActionListener(ae -> {
            int returnValue = fileChooser.showOpenDialog(this);
            String filePath;
            String fileName;
            MimeBodyPart attachmentPart;
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                filePath = selectedFile.getAbsolutePath();
                fileName = selectedFile.getName();
                System.out.println(filePath);

                attachmentPart = new MimeBodyPart();
                DataSource source = new FileDataSource(filePath);
                try {
                    attachmentPart.setDataHandler(new DataHandler(source));
                    attachmentPart.setFileName(fileName);
                    attachmentPartsData.addElement(new Attachment(attachmentPart));
                } catch (MessagingException e1) {
                    e1.printStackTrace();
                }
            }
        });
        removeAttachment.addActionListener(ae -> {
            ArrayList<Attachment> selectedItems = (ArrayList<Attachment>) attachmentParts.getSelectedValuesList();
            for (Attachment attachment : selectedItems) {
                attachmentPartsData.removeElement(attachment);
            }
        });

        add(addAttachment);
        add(new JScrollPane(attachmentParts));
        add(removeAttachment);
    }

    /**
     * Adds a new attachment to the list 
     * @param part the bodypart with the attachment to be added
     * @throws MessagingException is thrown if attachment cant be created.
     */
    public void addAttachment(MimeBodyPart part) throws MessagingException {
        attachmentPartsData.addElement(new Attachment(part));
    }
    /**
     * checks if there are no attachments
     * @return true if empty, false if not
     */
    public boolean isEmpty() {
        return attachmentPartsData.getSize() == 0;
    }
    public void clearAll(){
        attachmentPartsData.clear();
    }
    /**
     * Returns the list of attachments as an array.
     * @return attachments array
     */
    public Attachment[] getAttachments() {
        Attachment[] attachments = new Attachment[attachmentPartsData.getSize()];
        attachmentPartsData.copyInto(attachments);
        return attachments;
    }

}