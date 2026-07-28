package dev.craftforge.backpackplus.backpack;

import dev.craftforge.backpackplus.storage.BackpackStorage;
import dev.craftforge.backpackplus.util.Messages;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class BackpackService {

    private final ConcurrentHashMap<UUID, Map<Integer, Backpack>> cache = new ConcurrentHashMap<>();
    private final BackpackStorage storage;
    private final JavaPlugin plugin;
    private final Messages messages;

    public BackpackService(final BackpackStorage storage, final JavaPlugin plugin, final Messages messages) {
        this.storage = storage;
        this.plugin = plugin;
        this.messages = messages;
    }

    public int rowsFor(final Player player) {
        final int maxRows = plugin.getConfig().getInt("max-rows", 6);
        final int defaultRows = plugin.getConfig().getInt("default-rows", 3);

        int highest = 0;
        for (int i = maxRows; i >= 1; i--) {
            if (player.hasPermission("backpackplus.rows." + i)) {
                highest = i;
                break;
            }
        }

        return highest == 0 ? Math.min(defaultRows, maxRows) : Math.min(highest, maxRows);
    }

    public int pagesFor(final Player player) {
        final int maxPages = plugin.getConfig().getInt("max-pages", 5);
        final int defaultPages = plugin.getConfig().getInt("default-pages", 1);

        int highest = 0;
        for (int i = maxPages; i >= 1; i--) {
            if (player.hasPermission("backpackplus.pages." + i)) {
                highest = i;
                break;
            }
        }

        return highest == 0 ? Math.min(defaultPages, maxPages) : Math.min(highest, maxPages);
    }

    public void open(final Player viewer, final UUID ownerUuid, final int page) {
        ensureLoaded(ownerUuid);

        final Map<Integer, Backpack> pages = cache.get(ownerUuid);
        if (pages == null) {
            return;
        }

        final Player owner = Bukkit.getPlayer(ownerUuid);
        final int resolvedRows = owner != null ? rowsFor(owner) : plugin.getConfig().getInt("default-rows", 3);

        final Backpack backpack = pages.computeIfAbsent(page, p -> new Backpack(ownerUuid, p, resolvedRows));

        final int currentRows = backpack.getRows();
        if (currentRows != resolvedRows) {
            handleResize(viewer, owner, backpack, resolvedRows);
        }

        final String titleTemplate = plugin.getConfig().getString("backpack-title", "Backpack | Page {page}");
        final String rawTitle = titleTemplate.replace("{page}", String.valueOf(page));
        final Component title = net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize(rawTitle);

        final BackpackHolder holder = new BackpackHolder(ownerUuid, page, backpack);
        final Inventory inventory = Bukkit.createInventory(holder, resolvedRows * 9, title);
        holder.setInventory(inventory);

        final ItemStack[] stored = backpack.getContents();
        final ItemStack[] toPlace = new ItemStack[resolvedRows * 9];
        System.arraycopy(stored, 0, toPlace, 0, Math.min(stored.length, toPlace.length));
        inventory.setContents(toPlace);

        viewer.openInventory(inventory);
    }

    private void handleResize(final Player viewer, final Player owner, final Backpack backpack, final int newRows) {
        final int oldSize = backpack.size();
        final int newSize = newRows * 9;

        if (newSize >= oldSize) {
            backpack.resizeTo(newRows);
            return;
        }

        final ItemStack[] oldContents = backpack.getContents();
        final List<ItemStack> overflow = new ArrayList<>();

        for (int i = newSize; i < oldContents.length; i++) {
            if (oldContents[i] != null) {
                overflow.add(oldContents[i]);
            }
        }

        backpack.resizeTo(newRows);

        if (!overflow.isEmpty()) {
            final boolean dropOverflow = plugin.getConfig().getBoolean("drop-overflow", true);
            final Player target = owner != null ? owner : viewer;

            if (dropOverflow) {
                final Location loc = target.getLocation();
                for (final ItemStack item : overflow) {
                    target.getWorld().dropItemNaturally(loc, item);
                }
                target.sendMessage(messages.get("overflow-dropped"));
            } else {
                for (final ItemStack item : overflow) {
                    final Map<Integer, ItemStack> leftover = target.getInventory().addItem(item);
                    leftover.values().forEach(dropped -> target.getWorld().dropItemNaturally(target.getLocation(), dropped));
                }
                target.sendMessage(messages.get("overflow-dropped"));
            }
        }
    }

    public void save(final Backpack backpack) {
        final Map<Integer, Backpack> pages = cache.get(backpack.getOwnerUuid());
        if (pages != null) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () ->
                    storage.save(backpack.getOwnerUuid(), pages));
        }
    }

    public void loadAsync(final UUID uuid) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            final Map<Integer, Backpack> loaded = storage.load(uuid);
            cache.put(uuid, new ConcurrentHashMap<>(loaded));
        });
    }

    public void flushDirty() {
        for (final Map.Entry<UUID, Map<Integer, Backpack>> entry : cache.entrySet()) {
            boolean anyDirty = false;
            for (final Backpack backpack : entry.getValue().values()) {
                if (backpack.isDirty()) {
                    anyDirty = true;
                    break;
                }
            }
            if (anyDirty) {
                final UUID uuid = entry.getKey();
                final Map<Integer, Backpack> pages = entry.getValue();
                storage.save(uuid, pages);
                pages.values().forEach(Backpack::clearDirty);
            }
        }
    }

    public void flushAndEvict(final UUID uuid) {
        final Map<Integer, Backpack> pages = cache.remove(uuid);
        if (pages != null) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> storage.save(uuid, pages));
        }
    }

    public void saveAllSync() {
        storage.saveAll(cache);
    }

    private void ensureLoaded(final UUID uuid) {
        cache.computeIfAbsent(uuid, id -> new ConcurrentHashMap<>(storage.load(id)));
    }

    public ConcurrentHashMap<UUID, Map<Integer, Backpack>> getCache() {
        return cache;
    }

    public Map<Integer, Backpack> getPagesFor(final UUID uuid) {
        return cache.get(uuid);
    }
}
