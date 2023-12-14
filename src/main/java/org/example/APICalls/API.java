package org.example.APICalls;

import com.google.gson.Gson;
import io.github.cdimascio.dotenv.Dotenv;
import org.example.discordObjects.User;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class API {

    /**
     * Given the user's ID and the server's ID, get the nickname of the user within the server. If no username for
     * them is defined in the server, return either their global name, or username (in that order of preference)
     *
     * @param userId   The user's ID
     * @param serverId The server's ID
     * @return The user's nickname/global name/username
     */
    public static @NotNull String getServerNickname(@NotNull String userId, @NotNull String serverId) {

        try {
            URL url = new URL("https://discord.com/api/v9/users/" + userId + "/profile?with_mutual_guilds=true");

            HttpURLConnection conn = createDiscordConn(url);

            String response = makeRequest(conn);

            User user = new Gson().fromJson(response, User.class);

            return user.getNickname(serverId);
        } catch (IOException e) {
            System.err.println("An error occurred when trying to send off an HTTP request in getServerNickname: " + e.getMessage());
            return "";
        }
    }

    /**
     * Given a URL, set the GET method on it and add the Discord token from .env for authorisation. Then return it as a
     * new
     * HttpURLConnection
     *
     * @param url The URL to connect to
     * @return An HttpURLConnection to said URL
     */
    public static @NotNull HttpURLConnection createDiscordConn(@NotNull URL url) throws IOException {
        // Load the environment variables
        Dotenv dotenv = Dotenv.configure().load();

        try {
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            conn.setDoOutput(true);

            // Set request headers
            conn.setRequestProperty("Authorization", dotenv.get("DISCORD_TOKEN"));

            return conn;
        } catch (IOException e) {
            throw new IOException("An error occurred when configuring a URL in createDiscordConn: " + e.getMessage());
        }
    }

    /**
     * Given a URL, set the POST method on it and add the PushSafer token from .env for authorisation. Then return it
     * as a new HttpURLConnection
     *
     * @param url The URL to connect to
     * @return An HttpURLConnection to said URL
     */
    public static @NotNull HttpURLConnection createPushConn(@NotNull URL url) throws Exception {

        try {
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");

            conn.setDoOutput(true);
            conn.setRequestProperty("Content-type", "application/x-www-form-urlencoded");

            return conn;
        } catch (IOException e) {
            throw new Exception("An error occurred when configuring a URL in createPushConn: " + e.getMessage());
        }
    }

    /**
     * Make the request on a given HttpURLConnection, and return the response
     *
     * @param conn The connection to make the request from
     * @return The response from the request
     */
    public static @NotNull String makeRequest(@NotNull HttpURLConnection conn) {

        // Read the response as JSon
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();

            String responseLine;
            
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }

            return String.valueOf(response);
        } catch (IOException e) {
            System.err.println("An error occurred when calling makeRequest: " + e.getMessage());
            return "";
        }
    }

}
