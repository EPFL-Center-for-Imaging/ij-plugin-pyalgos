package ch.epfl.pyalgos;

import ij.IJ;
import ij.ImageJ;
import ij.plugin.PlugIn;

public class PyAlgosIJ implements PlugIn {

    @Override
    public void run(String s) {
        try {
            ClientConnectionDialog connectionDialog = new ClientConnectionDialog();
            connectionDialog.setVisible(true);
        } catch (Exception e) {
            IJ.log(e.getMessage());
        }
    }

    /**
     * This main function serves for development purposes.
     * It allows you to run the plugin immediately out of
     * your integrated development environment (IDE).
     *
     * @param args whatever, it's ignored
     * @throws Exception
     */
    public static void main(final String... args) throws Exception {
        // create the ImageJ application context with all available services
        final ImageJ ij = new ImageJ();
        // Run the plugin
        new PyAlgosIJ().run(null);
    }

}
