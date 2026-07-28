package dev.craftforge.backpackplus.listener;

import dev.craftforge.backpackplus.backpack.Backpack;
import dev.craftforge.backpackplus.backpack.BackpackHolder;
import dev.craftforge.backpackplus.backpack.BackpackService;
import dev.craftforge.backpackplus.util.Messages;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class BackpackListener implements Listener {

    private final BackpackService service;
    private final Messages messages;
    private final JavaPlugin plugin;
    private Set<Material> blacklistedMaterials = new HashSet<>();

    public BackpackListener(final BackpackService service, final Messages messages, final JavaPlugin plugin) {
        this.service = service;
        this.messages = messages;
        this.plugin = plugin;
        reloadBlacklist();
    }

    public void reloadBlacklist() {
        blacklistedMaterials = buildBlacklist(plugin.getConfig());
    }

    private Set<Material> buildBlacklist(final FileConfiguration config) {
        final Set<Material> result = new HashSet<>();
        final List<String> names = config.getStringList("blacklisted-materials");
        for (final String name : names) {
            final Material mat = Material.matchMaterial(name);
            if (mat != null) {
                result.add(mat);
            }
        }
        return result;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClose(final InventoryCloseEvent event) {
        final InventoryHolder holder = event.getInventory().getHolder();
        if (!(holder instanceof final BackpackHolder backpackHolder)) {
            return;
        }

        final Backpack backpack = backpackHolder.getBackpack();
        backpack.setContents(event.getInventory().getContents());
        service.save(backpack);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryClick(final InventoryClickEvent event) {
        final InventoryHolder holder = event.getInventory().getHolder();
        if (!(holder instanceof BackpackHolder)) {
            return;
        }

        if (!(event.getWhoClicked() instanceof final Player player)) {
            return;
        }

        final ItemStack cursor = event.getCursor();
        final ItemStack current = event.getCurrentItem();

        if (isBlacklisted(cursor)) {
            event.setCancelled(true);
            messages.send(player, "blacklisted-material");
            return;
        }

        if (isBlacklisted(current) && event.getClickedInventory() != event.getInventory()) {
            return;
        }

        if (isBackpackItem(cursor)) {
            event.setCancelled(true);
            messages.send(player, "no-nesting");
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryDrag(final InventoryDragEvent event) {
        final InventoryHolder holder = event.getInventory().getHolder();
        if (!(holder instanceof BackpackHolder)) {
            return;
        }

        if (!(event.getWhoClicked() instanceof final Player player)) {
            return;
        }

        final ItemStack dragged = event.getOldCursor();

        if (isBlacklisted(dragged)) {
            event.setCancelled(true);
            messages.send(player, "blacklisted-material");
            return;
        }

        if (isBackpackItem(dragged)) {
            event.setCancelled(true);
            messages.send(player, "no-nesting");
        }
    }

    private boolean isBlacklisted(final ItemStack item) {
        return item != null && item.getType() != Material.AIR && blacklistedMaterials.contains(item.getType());
    }

    private boolean isBackpackItem(final ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return false;
        }
        if (!item.hasItemMeta()) {
            return false;
        }
        final var meta = item.getItemMeta();
        return meta != null
                && meta.getPersistentDataContainer().has(
                new org.bukkit.NamespacedKey(plugin, "backpack-item"),
                org.bukkit.persistence.PersistentDataType.BYTE);
    }
}
