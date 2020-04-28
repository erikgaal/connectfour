package connectfour.gui;

import connectfour.Controller;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * JFrame that shows when the client starts. Allows the monkey to choose whether to start a monkey game or an AI game.
 */
public class Menu extends JFrame {

    private final Controller controller;

    private JButton start;
    private JButton startai;
    private JButton quit;

    private JPanel container;
    private GridBagConstraints gbc;

    public Menu(final Controller controller) {
        this.controller = controller;

        setTitle("JConnectFour");
        setSize(300, 450);

        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((int) (screen.getWidth() - getSize().getWidth()) / 2, (int) (screen.getHeight() - getSize().getHeight()) / 2);

        container = new JPanel();
        container.setLayout(new GridBagLayout());
        container.setBorder(new EmptyBorder(10, 10, 10, 10));
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.gridx = 0;

        start = new JButton("New Game");
        start.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                controller.setAI(false);
                controller.setVisible(controller.getAddressQueryFrame(), true);
            }
        });

        startai = new JButton("New AI Game");
        startai.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                controller.setAI(true);
                controller.setVisible(controller.getAddressQueryFrame(), true);
            }
        });

        quit = new JButton("Quit");
        quit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.shutdown();
            }
        });

        gbc.gridy = 0;
        container.add(start, gbc);
        gbc.gridy = 1;
        container.add(startai, gbc);
        gbc.gridy = 2;
        container.add(quit, gbc);

        add(container);

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }
}
