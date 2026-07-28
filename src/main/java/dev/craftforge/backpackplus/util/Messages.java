package dev.craftforge.backpackplus.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public final class Messages {

    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private FileConfiguration config;

    public Messages(final FileConfiguration config) {
        this.config = config;
    }

    public void reload(final FileConfiguration newConfig) {
        this.config = newConfig;
    }

    public Component get(final String key) {
        final String raw = config.getString("messages." + key, "<red>Missing message: " + key + "</red>");
        return miniMessage.deserialize(raw);
    }

    public Component get(final String key, final String... replacements) {
        String raw = config.getString("messages." + key, "<red>Missing message: " + key + "</red>");
        for (int i = 0; i + 1 < replacements.length; i += 2) {
            raw = raw.replace(replacements[i], replacements[i + 1]);
        }
        return miniMessage.deserialize(raw);
    }

    public Component parseTitle(final String templateKey, final String... replacements) {
        String raw = config.getString(templateKey, "");
        for (int i = 0; i + 1 < replacements.length; i += 2) {
            raw = raw.replace(replacements[i], replacements[i + 1]);
        }
        return miniMessage.deserialize(raw);
    }

    public void send(final Player player, final String key) {
        player.sendMessage(get(key));
    }

    public void send(final Player player, final String key, final String... replacements) {
        player.sendMessage(get(key, replacements));
    }
}
