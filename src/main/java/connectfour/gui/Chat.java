package connectfour.gui;

import connectfour.Controller;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

/**
 * JFrame that shows the incoming chat messages and allows the monkey to send chat messages to the server.
 * Only shows when the server supports Chat as specified in the features of the server.
 */
public class Chat extends JFrame {

    private final Controller controller;

    private JPanel container;
    private GridBagConstraints gbc;

    private DefaultListModel<String> messages;
    private JList<String> chat;
    private JScrollPane scrollPane;

    private JTextField text;
    private JButton send;

    public Chat(final Controller controller) {
        this.controller = controller;

        setTitle("Chat");
        setSize(300, 450);

        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((int) (screen.getWidth() + getSize().getWidth()) / 2, (int) (screen.getHeight() - getSize().getHeight()) / 2);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                controller.disconnect();
            }
        });

        container = new JPanel();
        container.setLayout(new GridBagLayout());
        container.setBorder(new EmptyBorder(10, 10, 10, 10));
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 10;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;

        messages = new DefaultListModel<String>();
        chat = new JList<String>(messages);
        scrollPane = new JScrollPane(chat);

        container.add(scrollPane, gbc);

        gbc.gridwidth = 1;
        gbc.weighty = 1;
        gbc.gridy = 1;
        text = new JTextField("");
        text.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sendChat();
            }
        });

        send = new JButton("Send");
        send.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sendChat();
            }
        });

        addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                sendChat();
            }
        });

        gbc.weightx = 9;
        gbc.gridx = 0;
        container.add(text, gbc);
        gbc.weightx = 1;
        gbc.gridx = 1;
        container.add(send, gbc);

        add(container);
    }

    /**
     * Called by pressing Enter or the 'Send' button.
     * Makes the controller send the CHAT packet to the server and shows own chat in the window.
     */
    private void sendChat() {
        if (!text.getText().isEmpty()) {
            controller.sendChat(text.getText());
            showMessage("You: " + text.getText());
            text.setText("");
        }
    }

    /**
     * Removes all messages, called when leaving the server.
     */
    public void clearMessages() {
        messages.clear();
    }

    /**
     * Called by the controller to show a message on the window.
     * @param message text message to display
     */
    public void showMessage(String message) {
        messages.addElement(message);
        if (chat.getLastVisibleIndex() == messages.getSize() - 2) {
            chat.ensureIndexIsVisible(messages.getSize() - 1);
        }
    }
}