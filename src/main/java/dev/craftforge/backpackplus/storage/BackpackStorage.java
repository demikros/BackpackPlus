package dev.craftforge.backpackplus.storage;

import dev.craftforge.backpackplus.backpack.Backpack;
import dev.craftforge.backpackplus.util.InventorySerializer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public final class BackpackStorage {

    private final File dataFolder;
    private final JavaPlugin plugin;

    public BackpackStorage(final JavaPlugin plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "data");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
    }

    public Map<Integer, Backpack> load(final UUID uuid) {
        final File file = fileFor(uuid);
        final Map<Integer, Backpack> pages = new HashMap<>();

        if (!file.exists()) {
            return pages;
        }

        final YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);

        for (final String key : yaml.getKeys(false)) {
            if (!key.startsWith("page.")) {
                continue;
            }

            final String pageStr = key.substring("page.".length());
            int pageIndex;
            try {
                pageIndex = Integer.parseInt(pageStr);
            } catch (final NumberFormatException ignored) {
                continue;
            }

            final int rows = yaml.getInt(key + ".rows", 3);
            final String data = yaml.getString(key + ".contents", null);

            final Backpack backpack = new Backpack(uuid, pageIndex, rows);

            if (data != null && !data.isEmpty()) {
                try {
                    final var contents = InventorySerializer.fromBase64(data, rows * 9);
                    backpack.setContents(contents);
                    backpack.clearDirty();
                } catch (final IOException | ClassNotFoundException e) {
                    plugin.getLogger().log(Level.WARNING, "Failed to deserialize backpack page " + pageIndex + " for " + uuid, e);
                }
            }

            pages.put(pageIndex, backpack);
        }

        return pages;
    }

    public void save(final UUID uuid, final Map<Integer, Backpack> pages) {
        final File file = fileFor(uuid);
        final YamlConfiguration yaml = new YamlConfiguration();

        for (final Map.Entry<Integer, Backpack> entry : pages.entrySet()) {
            final int pageIndex = entry.getKey();
            final Backpack backpack = entry.getValue();
            final String prefix = "page." + pageIndex;

            yaml.set(prefix + ".rows", backpack.getRows());

            try {
                yaml.set(prefix + ".contents", InventorySerializer.toBase64(backpack.getContents()));
            } catch (final IOException e) {
                plugin.getLogger().log(Level.WARNING, "Failed to serialize backpack page " + pageIndex + " for " + uuid, e);
            }
        }

        try {
            yaml.save(file);
        } catch (final IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save backpack data for " + uuid, e);
        }
    }

    public void saveAll(final Map<UUID, Map<Integer, Backpack>> cache) {
        for (final Map.Entry<UUID, Map<Integer, Backpack>> entry : cache.entrySet()) {
            save(entry.getKey(), entry.getValue());
        }
    }

    private File fileFor(final UUID uuid) {
        return new File(dataFolder, uuid.toString() + ".yml");
    }
}
