# ij-plugin-pyalgos

ImageJ/Fiji plugin to run Python-based image processing algorithms, in combination with the Python
API [api-py-algos](https://github.com/EPFL-Center-for-Imaging/api-py-algos).

The type of algorithm available are image-to-image algorithms, with additional parameters:

- The input image is either the full 2D image from the selected window, or only the rectangular selection if there is
  one.
- The parameters can be float, int, boolean or string values. They are initially defined on the Python server
  and are then set by the user in the ImageJ/Fiji user interface.
- The output is a 2D image.

### Installation

For the setup of the Python server, see
the [api-pyalgos README.md](https://github.com/EPFL-Center-for-Imaging/api-py-algos?tab=readme-ov-file#setup).

For the plugin:
Drag and drop the latest version of the plugin jar file PyAlgos-{version}.jar
into the main ImageJ/Fiji window, then restart ImageJ/Fiji.

### Update

Delete the existing .jar file in the plugins/jars folder of your ImageJ/Fiji installation.
Then follow the installation steps with the new .jar file.

### Usage

After installation, the plugin can be accessed via the menu Plugins>PyAlgos.

1. First, the Python server from [api-py-algos](https://github.com/EPFL-Center-for-Imaging/api-py-algos) must be up and
   running.
   Then, in the main Python algos window, enter the server URL and click on "Connect".
   The status indicates whether the connection to the server was successful or not.
   Once successfully connected, the available algorithms on that server are added in the drop-down list of available
   algorithms.
2. Choose an algorithm from the drop-down list and click on "Select". This opens a window to set the algorithm
   parameters.
   The default values are pre-entered for each field, and by hovering on the parameter name, a description is shown.
3. Once all the parameters are set, click on "Run". Depending on the size of the image and on the algorithm processing
   time, this can take a while. If there is an error during the processing on the Python server, the details are sent
   back and
   displayed in an error message in ImageJ/Fiji directly.
   Once the processing is completed successfully, the resulting image is displayed in Image/Fiji.

### Build

This is a Maven project, use the `mvn` command to build it.
The .jar file can then be found in the target directory.