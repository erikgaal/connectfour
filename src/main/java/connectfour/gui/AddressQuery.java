package connectfour.gui;

import connectfour.Controller;
import connectfour.objects.Board;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * JFrame that queries the monkey behind the computer which server to connect to.
 * Default values are our own server, hydrogen. Currently only allows ipv4 addresses because of the RegEx check that is made.
 * RegEx checks the input on all fields so 'no' wrong inputs can be made.
 */
public class AddressQuery extends JFrame {

    private final Controller controller;

    private JTextField host;
    private JTextField port;
    private JTextField name;
    private JTextField ai;

    private JButton cancel;
    private JButton connect;

    private JLabel hostlabel;
    private JLabel portlabel;
    private JLabel namelabel;
    private JLabel ailabel;

    private JPanel container;
    private GridBagConstraints gbc;

    public AddressQuery(final Controller controller) {
        this.controller = controller;

        setTitle("Connecting to...");
        setSize(400, 150);

        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((int) (screen.getWidth() - getSize().getWidth()) / 2, (int) (screen.getHeight() - getSize().getHeight()) / 2);

        container = new JPanel();
        container.setLayout(new GridBagLayout());
        container.setBorder(new EmptyBorder(10, 10, 10, 10));
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.gridy = 0;

        hostlabel = new JLabel("Hostname");
        portlabel = new JLabel("Port");
        namelabel = new JLabel("Username");
        ailabel = new JLabel("AI Strength (2-42)");

        gbc.gridx = 0;
        container.add(hostlabel, gbc);
        gbc.gridx = 1;
        container.add(portlabel, gbc);
        gbc.gridx = 2;
        container.add(namelabel, gbc);
        gbc.gridx = 3;
        container.add(ailabel, gbc);

        gbc.gridy = 1;
        host = new JTextField();
        port = new JTextField();
        name = new JTextField();
        ai = new JTextField();

        gbc.gridx = 0;
        container.add(host, gbc);
        gbc.gridx = 1;
        container.add(port, gbc);
        gbc.gridx = 2;
        container.add(name, gbc);
        gbc.gridx = 3;
        container.add(ai, gbc);

        host.setText("130.89.160.234");
        port.setText("8888");
        name.setText("Baas4");
        ai.setText("42");

        gbc.gridy = 2;
        cancel = new JButton("Cancel");
        cancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });
        connect = new JButton("Connect");
        connect.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!host.getText().matches("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$")) {
                    JOptionPane.showMessageDialog(null, "Please enter a valid ipv4 address");
                } else if (!port.getText().matches("^[0-9]{1,5}$") && Integer.parseInt(port.getText()) < Short.MAX_VALUE) {
                    JOptionPane.showMessageDialog(null, "Please enter a valid port number.");
                } else if (!name.getText().matches("^[a-zA-Z0-9]{1,31}$")) {
                    JOptionPane.showMessageDialog(null, "Please enter a valid username.");
                } else if (!ai.getText().matches("^[0-9]{1,2}$") && Integer.parseInt(ai.getText()) <= Board.HEIGHT * Board.WIDTH) {
                    JOptionPane.showMessageDialog(null, "Please enter a valid AI strength.");
                } else {
                    controller.setDepth(Integer.parseInt(ai.getText()));
                    controller.connect(host.getText(), Integer.parseInt(port.getText()), name.getText());
                }
            }
        });

        gbc.gridx = 0;
        container.add(cancel, gbc);
        gbc.gridx = 3;
        container.add(connect, gbc);

        add(container);
    }
}
