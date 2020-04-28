package connectfour.gui;

import connectfour.Controller;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.ArrayList;

/**
 * JFrame that shows the Leaderboard following our own implementation.
 * Only shows when the server supports Leaderboard as specified in the features of the server.
 */
public class Leaderboard extends JFrame {

    private final Controller controller;

    private JPanel container;
    private GridBagConstraints gbc;

    private DefaultTableModel rankings;
    private JTable table;
    private JScrollPane scrollPane;

    private JButton refresh;

    public Leaderboard(final Controller controller) {
        this.controller = controller;

        setTitle("Leaderboard");
        setSize(600, 450);

        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((int) (screen.getWidth() - 2.5 * getSize().getWidth()) / 2, (int) (screen.getHeight() - getSize().getHeight()) / 2);

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

        rankings = new DefaultTableModel(new Object[][]{}, new String[]{"Player", "Games Won", "Games Lost", "Games Played", "ELO"});
        table = new JTable(rankings);

        TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(table.getModel());
        table.setRowSorter(sorter);

        List<RowSorter.SortKey> sortKeys = new ArrayList<RowSorter.SortKey>();
        sortKeys.add(new RowSorter.SortKey(4, SortOrder.ASCENDING));
        sortKeys.add(new RowSorter.SortKey(1, SortOrder.ASCENDING));
        sorter.setSortKeys(sortKeys);
        sorter.sort();

        scrollPane = new JScrollPane(table);

        container.add(scrollPane, gbc);

        gbc.gridwidth = 1;
        gbc.weighty = 1;
        gbc.gridy = 1;
        refresh = new JButton("Refresh");
        refresh.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                controller.requestLeaderboard();
            }
        });

        container.add(refresh, gbc);

        add(container);
    }

    /**
     * Called by the controller, updates the data stored in the table
     * @param data
     */
    public void updateList(Object[][] data) {
        rankings.setRowCount(0);
        for (Object[] row : data) {
            rankings.addRow(row);
        }
    }
}