package org.example.discordObjects;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.example.apiCalls.API.getServerNickname;

/**
 * This is a Message object, which represents a message sent
 */
public class Message {

    private final String id;
    private String content;

    private final Author author;
    private final List<Mention> mentions;

    public Message(@NotNull String id, @NotNull String content, @NotNull Author author, @NotNull List<Mention> mentions) {
        this.id = id;
        this.content = content;
        this.author = author;
        this.mentions = mentions;
    }

    public @NotNull String getId() {
        return id;
    }

    public @NotNull String getContent() {
        return content;
    }

    public @NotNull Author getAuthor() {
        return author;
    }

    public @NotNull List<Mention> getMentions() {
        return mentions;
    }

    /**
     * Given a server's ID, 'clean' up the message's content, to fix the mentions from being <@ID> to being
     * the user's name in the server
     *
     * @param serverId The server's ID
     * @return A new message object representing this changed message
     */
    public Message fixContent(String serverId) {

        // This matches things of the form <@ID> or <@!ID>
        String reString = "<@!?\\d{18,19}>";
        Pattern regex = Pattern.compile(reString);

        // Check there are any matches
        if (regex.matcher(content).find()) {
            Map<String, String> mentionsMap = new HashMap<>();

            // Create a map from IDs to names. If these name can't be fetched from the server, just use their username.
            for (Mention mention : mentions
            ) {
                try {
                    mentionsMap.put(mention.getId(), getServerNickname(mention.getId(), serverId));
                } catch (Exception e) {
                    mentionsMap.put(mention.getId(), mention.getName());
                }
            }

            Matcher matcher = regex.matcher(content);

            // Perform the replacements
            while (matcher.find()) {
                String group = matcher.group();
                String id = group.replace("<@", "").replace("!", "").replace(">", "");
                content = content.replace(group, mentionsMap.get(id));
            }
        }

        return this;
    }
}
