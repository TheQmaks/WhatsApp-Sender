package qmaks.utils;

import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

public class HTTP {

    private static String API_KEY = "YOUR_API_KEY";

    public static String getNumber() throws Exception {
        return sendGetRequest("&action=getNumber&country=or&service=wa&count=1");
    }

    public static String setStatus(int id, int status) throws Exception {
        return sendGetRequest("&action=setStatus&id=" + id+ "&status=" + status);
    }

    public static String getStatus(int id) throws Exception {
        return sendGetRequest("&action=getStatus&id=" + id);
    }

    public static String sendGetRequest(String params) throws Exception {
        URL obj = new URL("http://sms-area.org/stubs/handler_api.php?api_key=" + API_KEY + params);
        HttpURLConnection connection = (HttpURLConnection) obj.openConnection();

        connection.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuffer response = new StringBuffer();

        for(String line = in.readLine(); line != null; line = in.readLine()) {
            response.append(line);
        }
        in.close();

        return response.toString();
    }
}
