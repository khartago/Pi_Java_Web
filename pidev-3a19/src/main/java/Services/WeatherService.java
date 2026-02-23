package Services;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class WeatherService {

    private static final String API_KEY = "0779acbb9aee496ae9653db6b1f6a910";

    public static double getTemperature(String city) {
        try {
            String urlString =
                    "https://api.openweathermap.org/data/2.5/weather?q="
                            + city
                            + "&units=metric&appid="
                            + API_KEY;

            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(conn.getInputStream()));

            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            reader.close();

            JSONObject jsonObject = new JSONObject(response.toString());
            return jsonObject.getJSONObject("main").getDouble("temp");

        } catch (Exception e) {
            System.out.println("Weather API not ready yet.");
            return -1; // return -1 if API not active
        }
    }
}