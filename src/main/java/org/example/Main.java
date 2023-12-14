package org.example;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.github.cdimascio.dotenv.Dotenv;
import org.example.discordObjects.Message;

import java.lang.reflect.Type;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static java.lang.Thread.sleep;
import static org.example.APICalls.API.*;


public class Main {
    private static final Dotenv ENV = Dotenv.configure().load();
    private static final String OPENING_QUOTE_REGEX = "[“\"]";
    private static final String CLOSING_QUOTE_REGEX = "[”\"]";
    private static final String INSIDE_QUOTE_REGEX = "[^“\"”]+";
    private static final String FULL_QUOTE_REGEX =
            ".*" + OPENING_QUOTE_REGEX + INSIDE_QUOTE_REGEX + CLOSING_QUOTE_REGEX + ".*";

    public static void main(String[] args) {

        String lastId = "";

        // Try to get as many messages as possible up to the limit
        List<Message> quotes = new ArrayList<>();

        // Get the maximum number of quotes we need
        int maxQuotes = Integer.parseInt(ENV.get("MAX_QUOTES"));

        // Keep getting more quotes until we have enough
        while (quotes.size() < maxQuotes) {

            try {
                // Sleep to avoid rate limits
                //noinspection BusyWait
                sleep(1000);
            } catch (InterruptedException e) {
                System.err.println("Thread was interrupted whilst trying to sleep: " +
                        e.getMessage() + "\nContinuing...");
            }

            // Get a list of the next 50 messages
            List<Message> nextMessages = getListOfMessages(lastId);

            // If the set of nextMessages is empty, it means that either we've been rate limited, or locked out
            // of the API, so we should gracefully just stop, and use whatever messages we already have.
            if (!nextMessages.isEmpty()) {
                lastId = nextMessages.get(nextMessages.size() - 1).getId();

                // Filter only messages that are quotes
                nextMessages =
                        nextMessages.parallelStream()
                                .filter(x -> x.getContent().matches(FULL_QUOTE_REGEX)).toList();

                // Add these to the list of quotes
                quotes.addAll(nextMessages);
            } else {
                break;
            }

            System.out.printf("%d quotes fetched so far.\n", quotes.size());
        }

        // Get a random quote from the list
        Message randQuote = quotes.get(new Random().nextInt(quotes.size()));

        // "Fix" the content - i.e., just replace any raw user IDs with their server nicknames
        randQuote = randQuote.fixContent(ENV.get("SERVER_ID"));

        // Try to send the quote as a push notification.
        // If this is true, then the message failed to send. In that case, try to send again and if it still
        // fails, just print the error and exit.
        if (pushMessage(randQuote, ENV.get("SERVER_ID"))) {
            if (pushMessage(randQuote, ENV.get("SERVER_ID"))) {
                System.err.println("Failed to send the push notification; tried twice.");
            } else {
                System.out.println("Successfully sent push notification on the second attempt.");
            }
        } else {
            System.out.println("Successfully sent push notification.");
        }
    }

    /**
     * Send a notification through PushSafer with the passed in quote message. Uses the key specified in .env
     *
     * @param message  The message with the quote in
     * @param serverId The ID of the server the message comes from
     * @return A boolean, true if an error occurred, false otherwise
     */
    private static boolean pushMessage(Message message, String serverId) {

        try {

            // Form the URL, which includes the quote formatting
            URL url = new URL(
                    String.format("https://www.pushsafer.com/api?k=" + ENV.get("PUSHSAFER_KEY") + "&t=%s&m=%s&i=127&c=#a600ff",
                            URLEncoder.encode(message.getContent(), StandardCharsets.UTF_8),
                            URLEncoder.encode(String.format("Quoted by: %s", message.getAuthor().getName(serverId)),
                                    StandardCharsets.UTF_8)
                    ));

            // Create the connection
            HttpURLConnection conn = createPushConn(url);

            // Make the request
            makeRequest(conn);

            // Return false to indicate success
            return false;

        } catch (Exception e) {
            System.err.println("An error occurred when calling pushMessage: " + e.getMessage());
            return true;
        }
    }

    /**
     * Get the most recent 50 messages in the channel specified in .env
     *
     * @param sinceId If this is not "", we will get all messages sent before a message with this given ID.
     *                Otherwise, get just the most recent messages.
     * @return A list of the 50 messages either before the message with the given ID, or just the most recent 50 in
     * the channel.
     */
    private static List<Message> getListOfMessages(String sinceId) {

        // Form the URL, different based on whether we have a previous message to get before or not
        URL url;

        try {
            if (!Objects.equals(sinceId, "")) {
                url = new URL("https://discord.com/api/v9/channels/" + ENV.get("CHANNEL_ID") + "/messages?before=" +
                        sinceId + "&limit=50");
            } else {
                url = new URL("https://discord.com/api/v9/channels/" + ENV.get("CHANNEL_ID") + "/messages?limit=50");
            }
        } catch (MalformedURLException e) {
            System.err.println("An error occurred when trying to form the URL in getListOfMessages: " + e.getMessage());
            return new ArrayList<>();
        }

        // If either of these throws an error, we'll just gracefully print out the error and return no messages
        try {
            // Create the connection
            HttpURLConnection conn = createDiscordConn(url);

            // Read the response as JSon
            String response = makeRequest(conn);

            // Parse the response as a list
            Type listType = new TypeToken<ArrayList<Message>>() {
            }.getType();
            return (new Gson().fromJson(response, listType));
        } catch (Exception e) {
            System.err.println("An error occurred when trying to make HTTP requests in getListOfMessages: " + e.getMessage());
            return new ArrayList<>();
        }

    }
}