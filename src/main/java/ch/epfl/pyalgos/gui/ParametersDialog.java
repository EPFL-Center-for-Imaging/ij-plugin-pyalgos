package ch.epfl.pyalgos.gui;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public abstract class ParametersDialog extends JDialog {

    // Run & Cancel buttons
    protected final JButton btnRun = new JButton("Run");
    protected final JButton btnCancel = new JButton("Cancel");

    // Algorithm and parameters fields
    protected String algoName;
    protected Map<String, JComponent> parameterComponentMap = new HashMap<>();
    protected Map<String, String> parameterTypeMap = new HashMap<>();

    public ParametersDialog(String algoName, List<JsonObject> parametersList) {
        super((Dialog) null, "Parameters");
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        this.algoName = algoName;
        JPanel parametersPanel = buildUI(parametersList);
        Border border = BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5), BorderFactory.createTitledBorder(this.algoName));
        parametersPanel.setBorder(border);
        mainPanel.add(parametersPanel, BorderLayout.CENTER);
        add(mainPanel);

        JPanel btnPanel = new JPanel();
        GridLayout gridLayout = new GridLayout();
        btnPanel.setLayout(gridLayout);

        btnPanel.setLayout(new GridLayout(1, 2));
        btnRun.addPropertyChangeListener("enabled", evt -> {
            if (evt.getSource() == btnRun) {
                if (btnRun.isEnabled()) {
                    btnRun.setText("Run");
                } else {
                    btnRun.setText("Please wait...");
                }
            }
        });
        btnPanel.add(btnCancel);
        btnPanel.add(btnRun);

        mainPanel.add(btnPanel, BorderLayout.SOUTH);

        pack();
        setResizable(true);
        mainPanel.getRootPane().setDefaultButton(btnRun);
    }

    private JPanel buildUI(List<JsonObject> parameters) {
        JPanel panel = new JPanel();
        // panel.setLayout(new GridLayout(4, 2));

        GridBagLayout gridBagLayout = new GridBagLayout();
        panel.setLayout(gridBagLayout);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;

        panel.setBorder(BorderFactory.createEtchedBorder());

        int index = 0;
        for (JsonObject p : parameters) {
            String key = p.get("name").getAsString();
            String prompt = p.get("display_name").getAsString();
            String type = p.get("type").getAsString();
            JsonElement defaultValue = p.get("default_value");
            String unit = p.get("unit") != null ? p.get("unit").getAsString() : null;
            String description = p.get("description") != null ? p.get("description").getAsString() : null;


            // Creat list of JComponents so I get back the values easily later ??
            // Make a method to get the value with the correct type for each type of JComponent and each parameter "type".

            // Add the label (on the left)
            gbc.ipadx = 20;
            gbc.ipady = 20;
            gbc.gridx = 0;
            gbc.gridy = index;
            gbc.anchor = GridBagConstraints.NORTHWEST;
            JLabel labelParameter = new JLabel(prompt);
            labelParameter.setToolTipText(description);
            panel.add(labelParameter, gbc);

            // Add the specific JComponent depending on the variable type (on the right)
            gbc.ipadx = 20;
            gbc.ipady = 20;
            gbc.gridx = 2;
            gbc.gridy = index;
            JComponent jComponent = null;
            switch (type) {
                case "bool":
                    jComponent = new JCheckBox("", defaultValue.getAsBoolean());
                    break;
                case "int":
                case "float":
                case "string":
                    jComponent = new JTextField(defaultValue.getAsString());
                    break;
                case "list":
                    JsonArray choicesArray = p.get("values").getAsJsonArray();
                    String[] choices = new String[choicesArray.size()];
                    for (int i = 0; i < choicesArray.size(); i++) {
                        choices[i] = choicesArray.get(i).getAsString();
                    }
                    jComponent = new JComboBox<String>(choices);
                    break;
            }
            if (jComponent != null) {
                parameterComponentMap.put(key, jComponent);
                parameterTypeMap.put(key, type);
                panel.add(jComponent, gbc);
            }
            ++index;
        }
        return panel;
    }

    protected Map<String, Object> readValues() {
        Map<String, Object> parametersValues = new HashMap<>();
        for (Map.Entry<String, JComponent> entry : parameterComponentMap.entrySet()) {
            String key = entry.getKey();
            JComponent component = entry.getValue();
            Object value = null;
            if (component instanceof JTextField) {
                switch (parameterTypeMap.get(key)) {
                    case "int":
                        value = Integer.parseInt(((JTextField) component).getText());
                        break;
                    case "float":
                        value = Float.parseFloat(((JTextField) component).getText());
                        break;
                    case "string":
                        value = ((JTextField) component).getText();
                }
            } else if (component instanceof JComboBox) {
                // Getting the selected item as a string
                value = ((JComboBox<String>) component).getSelectedItem().toString();
            } else if (component instanceof JCheckBox) {
                value = ((JCheckBox) component).isSelected();
            }
            parametersValues.put(key, value);
        }
        return parametersValues;
    }
}
