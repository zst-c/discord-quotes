package org.example.discordObjects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static org.example.APICalls.API.getServerNickname;

/**
 * This is an Author object, which contains the author of a message
 */
public class Author {
    private final String id;
    private final String username;
    private final String global_name;

    public Author(@NotNull String id, @NotNull String username, @Nullable String globalName) {
        this.id = id;
        this.username = username;
        this.global_name = globalName;
    }

    /**
     * Get the user's global name, or username (in that order of preference)
     *
     * @return The user's global name/username
     */
    public @NotNull String getName() {
        return Objects.requireNonNullElse(global_name, username);
    }

    /**
     * Given a server's ID, get the nickname of the user within the server. If no username for
     * them is defined in the server, return either their global name, or username (in that order of preference)
     *
     * @param serverId The server's ID
     * @return The user's nickname/global name/username
     */
    public @NotNull String getName(@NotNull String serverId) {
        try {
            return getServerNickname(id, serverId);
        } catch (Exception e) {
            return this.getName();
        }
    }
}
