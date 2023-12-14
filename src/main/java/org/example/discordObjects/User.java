package org.example.discordObjects;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is a User object, which represents a user and their mutual guilds with the requesting user
 */
public class User {

    private final Author user;
    private final List<GuildName> mutual_guilds;

    public User(Author user, List<GuildName> mutualGuilds) {
        this.user = user;

        mutual_guilds = mutualGuilds;
    }

    /**
     * Given a server's ID, get the nickname of the user within the server. If no username for
     * them is defined in the server, return either their global name, or username (in that order of preference)
     *
     * @param serverId The server's ID
     * @return The user's nickname/global name/username
     */
    public @NotNull String getNickname(@NotNull String serverId) {

        Map<String, String> namesMap = new HashMap<>();
        mutual_guilds.forEach(u -> namesMap.put(u.id, u.nick));

        // Check if the server is one they are in, and one where they have a custom nickname.
        // If not, return their global name or username
        if (namesMap.containsKey(serverId) && namesMap.get(serverId) != null) {
            return namesMap.get(serverId);
        } else {
            return user.getName();
        }
    }

    public static class GuildName {

        private final String id;
        private final String nick;

        private GuildName(String id, String nick) {
            this.id = id;
            this.nick = nick;
        }
    }
}
