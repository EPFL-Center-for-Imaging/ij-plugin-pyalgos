package ch.epfl.pyalgos.client;

import com.google.gson.*;
import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.Roi;
import ij.io.FileSaver;
import ij.io.Opener;
import ij.process.ImageProcessor;
import okhttp3.Response;

import java.io.IOException;
import java.net.URL;
import java.util.*;


public class PyAlgosClient {
    // Python API URL defined by the hostname or IP address and port of the server
    private URL apiUrl;

    // Default Python API URL
    public static String defaultUrl = "http://127.0.0.1:8000";

    public static String name = "Python algos";

    private final PyAlgosHttpClient httpClient = new PyAlgosHttpClient();

    private static PyAlgosClient instance = new PyAlgosClient();

    private PyAlgosClient() {
    }

    public static PyAlgosClient getInstance() {
        return instance;
    }

    public void setServerURL(String URL) throws IOException {
        URL = URL.trim();
        if (URL.endsWith("/")) {
            URL = URL.substring(0, URL.length() - 1);
        }
        this.apiUrl = new URL(URL);
        httpClient.setURL(this.apiUrl);
        if (!httpClient.isConnected()) throw new IOException();
    }

    public URL getServerURL() {
        return apiUrl;
    }

    /**
     * Check if the Http Client exists and can successfully connect to the server
     *
     * @return
     */
    public boolean isConnected() {
        return this.httpClient.isConnected();
    }

    /**
     * Parse the {@link Response}'s body as a {@link JsonObject}, and close the response.
     *
     * @param response
     * @return
     * @throws IOException
     */
    public static JsonObject parseResponseToJsonObject(Response response) throws IOException {
        JsonObject object = JsonParser.parseString(response.body().string()).getAsJsonObject();
        response.close();
        return object;
    }

    /**
     * Parse the {@link Response}'s body as {@link JsonObject}, then parse the list of {@link JsonObject} from the
     * value of the JsonObject at the given key, and close the response.
     *
     * @param response
     * @param key
     * @return
     * @throws IOException
     */
    public static List<JsonObject> parseResponseToJsonObjectList(Response response, String key) throws IOException {
        JsonObject object = parseResponseToJsonObject(response);
        JsonArray array = object.get(key).getAsJsonArray();
        List<JsonObject> list = new ArrayList<>(array.size());
        for (JsonElement element : array) {
            list.add(element.getAsJsonObject());
        }
        return list;
    }

    public String[] getAlgos() throws IOException {
        Response algoResponse = this.httpClient.getAlgosNames();
        JsonObject algos = parseResponseToJsonObject(algoResponse);
        JsonArray algosJsonArray = algos.get("algos_names").getAsJsonArray();

        String[] arr = new String[algosJsonArray.size()];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = algosJsonArray.get(i).getAsString();
        }

        return arr;
    }

    /**
     * Send the image as a byte array via HTTP POST request
     *
     * @param image {{@link ImagePlus}
     * @return
     * @throws IOException
     */
    public Response sendImage(ImagePlus image) throws IOException {
        byte[] serializedImage = new FileSaver(image).serialize();
        return this.httpClient.sendImage(serializedImage);
    }

    /**
     * Send the image as a JsonObject via HTTP POST request
     *
     * @param image {{@link ImagePlus}
     * @return
     * @throws IOException
     */
    public Response sendImageJson(ImagePlus image) throws IOException {
        byte[] serializedImage = new FileSaver(image).serialize();
        String img = Base64.getEncoder().encodeToString(serializedImage);
        JsonObject imageJson = new JsonObject();
        imageJson.addProperty("data", img);
        return this.httpClient.sendImage(imageJson);
    }

    public List<JsonObject> getRequiredParameters(String algoName) throws IOException {
        Response paramsResponse = this.httpClient.getAlgoRequiredParams(algoName);
        return parseResponseToJsonObjectList(paramsResponse, "parameters");
    }

    /**
     * Set the algorithm's parameters via HTTP POST request
     *
     * @param algoName   Name of the algorithm
     * @param parameters Json-formatted string containing the parameters
     * @return
     * @throws IOException
     */
    public Response setParameters(String algoName, Map parameters) throws IOException {
        Map<String, Object> parametersMap = new LinkedHashMap<>();
        parametersMap.put("parameters", parameters);
        String parametersString = new Gson().toJson(parametersMap);
        return this.httpClient.setAlgoParams(algoName, parametersString);
    }

    /**
     * Send the POST request to compute the result for the given algoName
     *
     * @param algoName
     * @return
     * @throws IOException
     */
    public Response computeResult(String algoName) throws IOException {
        return this.httpClient.computeResult(algoName);
    }


    /**
     * Get the computed result via HTTP GET request
     *
     * @param algoName
     * @return
     * @throws IOException
     */
    public JsonObject getComputedResult(String algoName) throws IOException {
        Response response = this.httpClient.getComputedResult(algoName);
        return parseResponseToJsonObject(response);
    }

    /**
     * Get the geojson features from the computed result via HTTP GET request
     *
     * @param algoName
     * @return
     * @throws IOException
     */
    public Response getComputedResultFeature(String algoName) throws IOException {
        return this.httpClient.getComputedResultFeatures(algoName);
    }

    /**
     * Get the specified endpoint result from the computed result via HTTP GET request
     *
     * @param algoName
     * @param endpoint
     * @return
     * @throws IOException
     */
    public Response getComputedResultEndpoint(String algoName, String endpoint) throws IOException {
        return this.httpClient.getComputedResultEndpoint(algoName, endpoint);
    }

    /**
     * Get a message detailing the HTTP response code and detail, and close the response
     *
     * @param response
     * @param description
     * @return
     */
    private String getHttpMessage(Response response, String description) {
        String detail = null;
        try {
            JsonElement jsonResponse = parseResponseToJsonObject(response).get("detail");
            detail = (jsonResponse == null) ? null : jsonResponse.getAsString();
        } catch (IOException ignored) {
        }
        String message = description + "\n" +
                "HTTP response " + response.code() + ": " + detail;
        response.close();
        return message;
    }

    /**
     * Display the error in a window
     *
     * @param description
     */
    public void displayError(String description) {
        IJ.error(PyAlgosClient.name + ": Error", description);
    }

    /**
     * Display the HTTP error in a window (and close the response)
     *
     * @param response
     * @param description
     */
    protected void displayHttpError(Response response, String description) {
        IJ.error(PyAlgosClient.name + ": Error", this.getHttpMessage(response, description));
    }

    /**
     * Get an {@link ImagePlus} containing the pixels of the selected slice/channel at the selected ROI
     *
     * @param img
     * @return
     */
    private ImagePlus getSelectedImage(ImagePlus img) {
        ImagePlus selectedImg = null;
        Roi roi = img.getRoi();
        if (roi != null) {
            selectedImg = IJ.createImage("Selected ROI image", (int) roi.getFloatWidth(), (int) roi.getFloatHeight(), 1, img.getBitDepth());
            ImageProcessor imageProcessor = selectedImg.getProcessor();
            for (int x = 0; x < roi.getFloatWidth(); ++x) {
                for (int y = 0; y < roi.getFloatHeight(); ++y) {
                    int[] pixel = img.getPixel(x + (int) roi.getXBase(), y + (int) roi.getYBase());
                    imageProcessor.putPixel(x, y, pixel);
                }
            }
        } else {
            selectedImg = IJ.createImage("Selected Image", img.getWidth(), img.getHeight(), 1, img.getBitDepth());
            ImageProcessor imageProcessor = selectedImg.getProcessor();
            for (int x = 0; x < img.getWidth(); ++x) {
                for (int y = 0; y < img.getHeight(); ++y) {
                    int[] pixel = img.getPixel(x, y);
                    imageProcessor.putPixel(x, y, pixel);
                }
            }
        }
        return selectedImg;
    }

    /**
     * Run the algorithm and display the result
     *
     * @param algoName
     * @param parametersValues
     * @return
     * @throws IOException
     */
    public void run(String algoName, Map<String, Object> parametersValues) throws IOException {
        // Send the parameters defined by the user
        Response parametersResponse = this.setParameters(algoName, parametersValues);
        if (parametersResponse.isSuccessful()) {
            parametersResponse.close();
        } else {
            displayHttpError(parametersResponse, "Could not set the user parameters for " + algoName);
            return;
        }

        // Get the pixels of the selected slice/channel at the selected Roi & send the image
        ImagePlus img = WindowManager.getCurrentImage();
        if (img == null) {
            displayError("No open image");
            return;
        }
        String imgTitle = img.getTitle();
        ImagePlus selectedImg = getSelectedImage(img);
        Response imgSentResponse = this.sendImage(selectedImg);
        if (imgSentResponse.isSuccessful()) {
            imgSentResponse.close();
        } else {
            displayHttpError(imgSentResponse, "Could not send image to server");
            return;
        }

        // Run the algo
        Response processingResponse = this.computeResult(algoName);
        // Leave the response open if successful to check the available endpoints
        if (!processingResponse.isSuccessful()) {
            displayHttpError(processingResponse, "Processing with " + algoName + " failed");
            return;
        }

        // Check which endpoints are available for this algo and then display the result accordingly
        List<String> endpoints = new ArrayList<>();
        try {
            JsonObject endpointsJson = parseResponseToJsonObject(processingResponse);
            JsonArray endpointsArray = endpointsJson.get("output_endpoints").getAsJsonArray();
            for (JsonElement element : endpointsArray) {
                endpoints.add(element.getAsString());
            }
        } catch (Exception e) {
            displayError("Could not retrieve the output endpoints for " + algoName + " - cannot display result\n" +
                    "Please check the algorithm implementation on the Python server");
            return;
        }
        if (endpoints.contains("image")) {
            String endpoint = "image";
            Response resultResponse = this.getComputedResultEndpoint(algoName, endpoint);
            JsonObject object = parseResponseToJsonObject(resultResponse);
            String encodedImage = object.get(endpoint).getAsString();

            // Deserialized encoded TIFF image and show it
            byte[] bytes = Base64.getDecoder().decode(encodedImage);
            ImagePlus dsImage = new Opener().deserialize(bytes);
            dsImage.setTitle(imgTitle + " - " + algoName);
            dsImage.show();
        } else {
            displayError("Unknown display for result with endpoints: " + Arrays.toString(endpoints.toArray()) + "\n" +
                    "Only the following endpoints can be displayed in ImageJ/Fiji: [image]");
        }

        Response deletedResponse = this.httpClient.deleteImageData();
        deletedResponse.close();
    }
}
