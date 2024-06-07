package ch.epfl.pyalgos;

import ch.epfl.pyalgos.client.PyAlgosClient;
import ch.epfl.pyalgos.gui.ParametersDialog;
import com.google.gson.JsonObject;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ClientParametersDialog extends ParametersDialog implements ActionListener {

    public ClientParametersDialog(String algoName, List<JsonObject> parametersList) {
        super(algoName, parametersList);
        btnRun.addActionListener(this);
        btnCancel.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnRun) {
            btnRun.setEnabled(false);
            SwingUtilities.invokeLater(() -> {
                Map<String, Object> parametersValues = readValues();
                PyAlgosClient client = PyAlgosClient.getInstance();
                try {
                    client.run(algoName, parametersValues);
                } catch (IOException runIOE) {
                    client.displayError("Processing with " + algoName + " failed!");
                } finally {
                    btnRun.setEnabled(true);
                }
            });

        } else if (e.getSource() == btnCancel) {
            dispose();
        }
    }
}