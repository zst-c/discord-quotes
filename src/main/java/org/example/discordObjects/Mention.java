package org.example.discordObjects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * This is a Mention object, which contains all mentions in a message
 */
public class Mention {

    private final String id;
    private final String username;
    private final String global_name;

    public Mention(@NotNull String id, @NotNull String username, @Nullable String globalName) {
        this.id = id;
        this.username = username;
        global_name = globalName;
    }

    public @NotNull String getId() {
        return id;
    }

    /**
     * Get the user's global name, or username (in that order of preference)
     *
     * @return The user's global name/username
     */
    public @NotNull String getName() {
        return Objects.requireNonNullElse(global_name, username);
    }
}
