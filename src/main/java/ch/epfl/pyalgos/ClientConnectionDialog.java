package ch.epfl.pyalgos;

import ch.epfl.pyalgos.client.PyAlgosClient;
import ch.epfl.pyalgos.gui.ConnectionDialog;
import com.google.gson.JsonObject;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;


public class ClientConnectionDialog extends ConnectionDialog implements ActionListener {

    protected ClientParametersDialog parametersDialog;

    public ClientConnectionDialog() {
        super();
        setTitle(PyAlgosClient.name);
        textFieldServer.setText(PyAlgosClient.defaultUrl);
        btnConnect.addActionListener(this);
        btnSelect.addActionListener(this);
        btnClose.addActionListener(this);
    }

    private void updateAlgos(String[] algos) {
        comboBox.removeAllItems();
        if (algos == null || algos.length == 0) {
            comboBox.setEnabled(false);
            btnSelect.setEnabled(false);
        } else {
            for (String algo : algos) {
                comboBox.addItem(algo);
            }
            comboBox.setEnabled(true);
            btnSelect.setEnabled(true);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnConnect) {
            PyAlgosClient client = PyAlgosClient.getInstance();

            // Connect to the server URL input by the user
            String serverURL = textFieldServer.getText();
            try {
                client.setServerURL(serverURL);
                labelStatusDetails.setText("Connected to " + serverURL);
                this.setFocusSelectAlgo();
            } catch (IOException serverIOE) {
                String connectErrMessage = "Could not connect to " + serverURL;
                labelStatusDetails.setText(connectErrMessage);
                // Disable existing algos since server is disconnected
                this.updateAlgos(null);
                return;
            }

            // Update the available algos
            String[] algos;
            try {
                algos = client.getAlgos();
            } catch (IOException algosIOE) {
                String algosErrMessage = "Could not retrieve algorithms from " + serverURL;
                labelStatusDetails.setText(algosErrMessage);
                return;
            }
            this.updateAlgos(algos);

        } else if (e.getSource() == btnSelect) {
            PyAlgosClient client = PyAlgosClient.getInstance();
            String algoName = comboBox.getSelectedItem().toString();

            // Open parameter dialogs
            try {
                List<JsonObject> parametersList = client.getRequiredParameters(algoName);
                parametersDialog = new ClientParametersDialog(algoName, parametersList);
                parametersDialog.setVisible(true);
            } catch (IOException paramsIOE) {
                client.displayError("Could not retrieve required parameters for " + algoName);
            }
        } else if (e.getSource() == btnClose) {
            dispose();
            if (parametersDialog != null) parametersDialog.dispose();
        }
    }


    public static void main(String[] args) {
        ClientConnectionDialog pDialogJson = new ClientConnectionDialog();
        pDialogJson.setVisible(true);
    }
}
