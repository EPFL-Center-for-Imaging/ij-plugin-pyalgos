package ch.epfl.pyalgos.client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class to handle the client's HTTP requests
 */
public class PyAlgosHttpClient {

    // Python API URL defined by the hostname or IP address and port of the server
    private URL apiUrl;

    // HttpClient used for all the HTTP requests
    private final OkHttpClient httpClient;
    public static final MediaType JSON = MediaType.get("application/json");
    // Timeout duration (minutes)
    private final int timeout;
    static int defaultTimeout = 10;

    /**
     * Initialize by building the HTTP client with the default timeout value
     */
    public PyAlgosHttpClient() {
        this(defaultTimeout);
    }

    /**
     * Initialize by building the HTTP client with the given timeout value (in minutes)
     */
    public PyAlgosHttpClient(int timeout) {
        Logger.getLogger(OkHttpClient.class.getName()).setLevel(Level.FINE);

        this.timeout = timeout;
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(timeout, TimeUnit.MINUTES)
                .writeTimeout(timeout, TimeUnit.MINUTES)
                .readTimeout(timeout, TimeUnit.MINUTES);
        this.httpClient = builder.build();
    }

    /**
     * Get the timeout value in minutes
     *
     * @return
     */
    public int getTimeout() {
        return this.timeout;
    }

    /**
     * Get the API URL
     */
    public URL getApiUrl() {
        return this.apiUrl;
    }

    /**
     * Set the API URL for the HTTP requests
     *
     * @param serverURL (e.g. "http://127.0.0.1:8000" as a String)
     */
    public void setURL(String serverURL) throws MalformedURLException {
        setURL(new URL(serverURL));
    }


    /**
     * Set the API URL for the HTTP requests
     *
     * @param serverURL (e.g. "http://127.0.0.1:8000" as a URL)
     */
    public void setURL(URL serverURL) {
        this.apiUrl = serverURL;
    }


    /**
     * Send an HTTP GET request to the server
     *
     * @param path (appended to the apiUrl)
     * @return {@link Response} from the server
     * @throws IOException
     */
    private Response get(String path) throws IOException {
        Request request = new Request.Builder()
                .url(new URL(apiUrl + path))
                .header("Content-Type", "application/json")
                .build();
        return httpClient.newCall(request).execute();
    }

    /**
     * Send an HTTP POST request to the server with a body in String
     *
     * @param path relative path appended to the apiUrl
     * @param body Content as {@link String} to be sent in the body of the POST request
     * @return {@link Response} from the server
     * @throws IOException
     */
    private Response post(String path, String body) throws IOException {
        RequestBody requestBody = RequestBody.create(body, JSON);
        Request request = new Request.Builder()
                .url(new URL(apiUrl + path))
                .header("Content-Type", "application/json")
                .post(requestBody)
                .build();
        return httpClient.newCall(request).execute();
    }

    /**
     * Send an HTTP POST request to the server with a body in byte[]
     *
     * @param path  relative path appended to the apiUrl
     * @param bytes Content as {@link byte[]} to be sent in the body of the POST request
     * @return {@link Response} from the server
     * @throws IOException
     */
    private Response post(String path, byte[] bytes) throws IOException {
        RequestBody requestBody = RequestBody.create(bytes);
        Request request = new Request.Builder()
                .url(new URL(apiUrl + path))
                .header("Content-Type", "application/octet-stream")
                .post(requestBody)
                .build();
        return httpClient.newCall(request).execute();
    }

    /**
     * Send an HTTP DELETE request to the server
     *
     * @param path (appended to the apiUrl)
     * @return {@link Response} from the server
     * @throws IOException
     */
    private Response delete(String path) throws IOException {
        Request request = new Request.Builder()
                .url(new URL(apiUrl + path))
                .header("Content-Type", "application/json")
                .delete()
                .build();
        return httpClient.newCall(request).execute();
    }


    /**
     * Check whether the client is connected to the server
     *
     * @return true if successful
     */
    public boolean isConnected() {
        try (Response httpResponse = this.get("/")) {
            JsonObject response = JsonParser.parseString(httpResponse.body().string()).getAsJsonObject();
            return httpResponse.isSuccessful() && response.get("message").getAsString().equals("hello");
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Get the list of available algorithms names
     *
     * @return {@link Response} with the list of available algorithms names
     * @throws IOException
     */
    public Response getAlgosNames() throws IOException {
        return this.get("/algos_names/");
    }

    /**
     * Get info about a specific algorithm
     *
     * @param algoName
     * @return {@link Response} containing the info about the algorithm (description, parameters, ...)
     * @throws IOException
     */
    public Response getAlgoInfo(String algoName) throws IOException {
        return this.get("/algos/" + algoName);
    }

    /**
     * Get the required parameters for the selected algorithm
     *
     * @param algoName
     * @return {@link Response} containing the required parameters for the algorithm
     * @throws IOException
     */
    public Response getAlgoRequiredParams(String algoName) throws IOException {
        return this.get("/algos/" + algoName + "/required_parameters");
    }

    /**
     * Send a DELETE request all the information related to the image and result on the server
     *
     * @return {@link Response} from the server
     * @throws IOException
     */
    public Response deleteImageData() throws IOException {
        return this.delete("/image");
    }

    /**
     * Send a POST request for the image
     *
     * @param image
     * @return
     * @throws IOException
     */
    public Response sendImage(JsonObject image) throws IOException {
        return this.post("/image", new Gson().toJson(image));
    }

    public Response sendImage(byte[] bytes) throws IOException {
        return this.post("/image_bytes", bytes);
    }

    /**
     * Send a POST request for the algo parameters for the given algoName
     *
     * @param algoName
     * @param algoParams
     * @return HTTPResponse
     * @throws IOException
     */
    public Response setAlgoParams(String algoName, String algoParams) throws
            IOException {
        return this.post("/image/" + algoName + "/parameters", algoParams);
    }

    /**
     * Get the parameters that were set for the selected algoName
     *
     * @param algoName
     * @return HTTPResponse containing the parameters for the algorithm
     * @throws IOException
     */
    public Response getSelectedAlgoParams(String algoName) throws IOException {
        return this.get("/image/" + algoName + "/parameters");
    }

    /**
     * Send the POST request to compute the result for the given algoName. The image data & the algorithm parameters
     * should already be available on the server.
     *
     * @param algoName
     * @return HTTPResponse
     * @throws IOException
     */
    public Response computeResult(String algoName) throws IOException {
        return this.post("/image/" + algoName + "/result", "result");
    }

    /**
     * Get the computed result
     *
     * @param algoName
     * @return
     * @throws IOException
     */
    public Response getComputedResult(String algoName) throws IOException {
        return this.get("/image/" + algoName + "/result");
    }

    /**
     * Get the image from the result
     *
     * @param algoName
     * @return
     * @throws IOException
     */
    public Response getComputedResultImage(String algoName) throws IOException {
        return this.get("/image" + algoName + "/result/image");
    }

    /**
     * Get the mask from the result
     *
     * @param algoName
     * @return
     * @throws IOException
     */
    public Response getComputedResultMask(String algoName) throws IOException {
        return this.get("/image" + algoName + "/result/mask");
    }

    /**
     * Get the geojson features from the result
     *
     * @param algoName
     * @return
     * @throws IOException
     */
    public Response getComputedResultFeatures(String algoName) throws IOException {
        return this.get("/image/" + algoName + "/result/features");
    }

    /**
     * Get the specific endpoint from the result
     *
     * @param algoName
     * @param endpoint
     * @return
     * @throws IOException
     */
    public Response getComputedResultEndpoint(String algoName, String endpoint) throws IOException {
        return this.get("/image/" + algoName + "/result/" + endpoint);
    }
}
