package ch.epfl.pyalgos.gui;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;


public abstract class ConnectionDialog extends JDialog {

    // Main panel
    protected final JPanel mainPanel = new JPanel();
    protected final JButton btnClose = new JButton("Close");

    // Components related to the connection to the server
    protected final JLabel labelServer = new JLabel("Server URL");
    protected final JTextField textFieldServer = new JTextField(20);
    protected final JButton btnConnect = new JButton("Connect");

    protected final JLabel labelStatus = new JLabel("Status: ");
    protected final JLabel labelStatusDetails = new JLabel("Not connected");

    // Components related to the available algorithms
    protected final JLabel algosLabel = new JLabel("Available algorithms");
    protected final JComboBox<String> comboBox = new JComboBox<String>(new String[]{});
    protected final JButton btnSelect = new JButton("Select");

    public ConnectionDialog() {
        super((Dialog) null);
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel serverPanel = buildServerPanel();
        mainPanel.add(serverPanel, BorderLayout.NORTH);

        JPanel algosPanel = buildAlgosPanel();
        mainPanel.add(algosPanel, BorderLayout.CENTER);

        JPanel bnPanel = new JPanel();
        GridLayout gridLayout = new GridLayout();
        bnPanel.setLayout(gridLayout);

        bnPanel.setLayout(new GridLayout(1, 2));
        bnPanel.add(btnClose);

        mainPanel.add(bnPanel, BorderLayout.SOUTH);
        add(mainPanel);

        pack();
        setResizable(false);
        setFocusConnect();
    }

    protected void setFocusConnect() {
        textFieldServer.requestFocusInWindow();
        mainPanel.getRootPane().setDefaultButton(btnConnect);
    }

    protected void setFocusSelectAlgo() {
        comboBox.requestFocusInWindow();
        mainPanel.getRootPane().setDefaultButton(btnSelect);
    }

    private JPanel buildServerPanel() {
        JPanel panel = new JPanel();
        GridBagLayout gridBagLayout = new GridBagLayout();
        panel.setLayout(gridBagLayout);
        GridBagConstraints gbc = new GridBagConstraints();
        Border border = BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5), BorderFactory.createTitledBorder("Server"));
        panel.setBorder(border);

        // Add server label
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.insets = new Insets(1, 1, 1, 10);
        panel.add(labelServer, gbc);

        // Add text field for input server URL
        gbc.gridx = 1;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.insets = new Insets(1, 1, 1, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(textFieldServer, gbc);

        // Add connect button
        gbc.gridx = 2;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.insets = new Insets(1, 1, 1, 1);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(btnConnect);

        // Add connection status labels
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.insets = new Insets(1, 1, 1, 10);
        panel.add(labelStatus, gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.gridheight = 1;
        gbc.insets = new Insets(1, 1, 1, 1);
        panel.add(labelStatusDetails, gbc);
        return panel;
    }

    private JPanel buildAlgosPanel() {
        JPanel panel = new JPanel();
        GridBagLayout gridBagLayout = new GridBagLayout();
        panel.setLayout(gridBagLayout);
        GridBagConstraints gbc = new GridBagConstraints();
        Border border = BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5), BorderFactory.createTitledBorder("Algorithms"));
        panel.setBorder(border);

        // Add algorithms label, ComboBox and Select button
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.insets = new Insets(1, 1, 1, 10);
        panel.add(algosLabel, gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.insets = new Insets(1, 1, 1, 10);
        panel.add(comboBox, gbc);
        comboBox.setEnabled(false);

        gbc.gridx = 2;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.insets = new Insets(1, 1, 1, 1);
        panel.add(btnSelect);
        btnSelect.setEnabled(false);

        return panel;
    }
}
