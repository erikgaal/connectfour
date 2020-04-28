package connectfour.gui;

import connectfour.Controller;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Set;

/**
 * JFrame that shows the Lobby, a list containing all monkeys on the server.
 * Allows the monkey to invite, accept and decline invites from other players.
 */
public class Lobby extends JFrame {

    private final Controller controller;

    private JPanel container;
    private GridBagConstraints gbc;

    private DefaultListModel<String> playerlist;
    private JList<String> list;
    private JScrollPane scrollPane;

    private JButton exit;
    private JButton invite;

    public Lobby(final Controller controller) {
        this.controller = controller;

        setSize(300, 450);

        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((int) (screen.getWidth() - getSize().getWidth()) / 2, (int) (screen.getHeight() - getSize().getHeight()) / 2);

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
        gbc.weighty = 9;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;

        playerlist = new DefaultListModel<String>();
        list = new JList<String>(playerlist);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        scrollPane = new JScrollPane(list);

        container.add(scrollPane, gbc);

        gbc.gridwidth = 1;
        gbc.weighty = 1;
        gbc.gridy = 1;
        exit = new JButton("Quit");
        exit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                controller.disconnect();
            }
        });

        invite = new JButton("Invite");
        invite.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (controller.getName().equals(list.getSelectedValue())) {
                    JOptionPane.showMessageDialog(null, "You cannot invite yourself!");
                } else {
                    controller.invite(list.getSelectedValue());
                }
            }
        });

        gbc.gridx = 0;
        container.add(exit, gbc);
        gbc.gridx = 1;
        container.add(invite, gbc);

        add(container);
    }

    /**
     * Called by the controller, shows a dialog asking for confirmation of an invite.
     * @param player name of the invitee
     */
    public void showInvite(String player) {
        int result = JOptionPane.showConfirmDialog(null, player + " invited you.");
        if (result == JOptionPane.OK_OPTION) {
            controller.acceptInvite(player);
        } else {
            controller.declineInvite(player);
        }
    }

    /**
     * Called by the controller, updates the list of players in the server.
     * @param players Set of players
     */
    public void updateList(Set<String> players) {
        playerlist.clear();
        for (String player : players) {
            playerlist.addElement(player);
        }
    }
}
