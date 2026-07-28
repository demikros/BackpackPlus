package dev.craftforge.backpackplus.command;

import dev.craftforge.backpackplus.backpack.BackpackService;
import dev.craftforge.backpackplus.backpack.BackpackHolder;
import dev.craftforge.backpackplus.listener.BackpackListener;
import dev.craftforge.backpackplus.util.InventorySorter;
import dev.craftforge.backpackplus.util.Messages;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public final class BackpackCommand implements CommandExecutor, TabCompleter {

    private final BackpackService service;
    private final Messages messages;
    private final JavaPlugin plugin;
    private final BackpackListener backpackListener;

    public BackpackCommand(final BackpackService service, final Messages messages, final JavaPlugin plugin, final BackpackListener backpackListener) {
        this.service = service;
        this.messages = messages;
        this.plugin = plugin;
        this.backpackListener = backpackListener;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!(sender instanceof final Player player)) {
            sender.sendMessage(messages.get("no-permission"));
            return true;
        }

        if (args.length == 0) {
            if (!player.hasPermission("backpackplus.use")) {
                messages.send(player, "no-permission");
                return true;
            }
            service.open(player, player.getUniqueId(), 1);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "open" -> handleOpen(player, args);
            case "sort" -> handleSort(player);
            case "pages" -> handlePages(player);
            case "reload" -> handleReload(player);
            default -> handlePageArg(player, args[0]);
        }

        return true;
    }

    private void handleOpen(final Player viewer, final String[] args) {
        if (!viewer.hasPermission("backpackplus.admin")) {
            messages.send(viewer, "no-permission");
            return;
        }

        if (args.length < 2) {
            messages.send(viewer, "usage-backpack");
            return;
        }

        final String targetName = args[1];
        UUID targetUuid = null;

        final Player online = Bukkit.getPlayer(targetName);
        if (online != null) {
            targetUuid = online.getUniqueId();
        } else {
            final OfflinePlayer offline = Bukkit.getOfflinePlayer(targetName);
            if (offline.hasPlayedBefore()) {
                targetUuid = offline.getUniqueId();
            }
        }

        if (targetUuid == null) {
            messages.send(viewer, "player-not-found", "{player}", targetName);
            return;
        }

        int page = 1;
        if (args.length >= 3) {
            try {
                page = Integer.parseInt(args[2]);
            } catch (final NumberFormatException ignored) {
                messages.send(viewer, "invalid-page", "{max}", "?");
                return;
            }
        }

        service.open(viewer, targetUuid, page);
    }

    private void handleSort(final Player player) {
        if (!player.hasPermission("backpackplus.use")) {
            messages.send(player, "no-permission");
            return;
        }

        final Inventory openInv = player.getOpenInventory().getTopInventory();
        final InventoryHolder holder = openInv.getHolder();

        if (!(holder instanceof final BackpackHolder backpackHolder)) {
            messages.send(player, "usage-backpack");
            return;
        }

        final ItemStack[] sorted = InventorySorter.sort(openInv.getContents());
        openInv.setContents(sorted);
        backpackHolder.getBackpack().setContents(sorted);
        messages.send(player, "backpack-sorted");
    }

    private void handlePages(final Player player) {
        if (!player.hasPermission("backpackplus.use")) {
            messages.send(player, "no-permission");
            return;
        }

        final int pages = service.pagesFor(player);
        final int rows = service.rowsFor(player);
        messages.send(player, "pages-info",
                "{pages}", String.valueOf(pages),
                "{rows}", String.valueOf(rows));
    }

    private void handleReload(final Player player) {
        if (!player.hasPermission("backpackplus.admin")) {
            messages.send(player, "no-permission");
            return;
        }

        plugin.reloadConfig();
        messages.reload(plugin.getConfig());
        backpackListener.reloadBlacklist();
        messages.send(player, "config-reloaded");
    }

    private void handlePageArg(final Player player, final String pageArg) {
        if (!player.hasPermission("backpackplus.use")) {
            messages.send(player, "no-permission");
            return;
        }

        int page;
        try {
            page = Integer.parseInt(pageArg);
        } catch (final NumberFormatException ignored) {
            messages.send(player, "usage-backpack");
            return;
        }

        final int maxPages = service.pagesFor(player);
        if (page < 1 || page > maxPages) {
            messages.send(player, "invalid-page", "{max}", String.valueOf(maxPages));
            return;
        }

        service.open(player, player.getUniqueId(), page);
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
        final List<String> suggestions = new ArrayList<>();

        if (!(sender instanceof final Player player)) {
            return suggestions;
        }

        if (args.length == 1) {
            final List<String> subs = new ArrayList<>(Arrays.asList("sort", "pages"));
            if (player.hasPermission("backpackplus.admin")) {
                subs.add("open");
                subs.add("reload");
            }
            final int maxPages = service.pagesFor(player);
            for (int i = 1; i <= maxPages; i++) {
                subs.add(String.valueOf(i));
            }
            final String partial = args[0].toLowerCase();
            for (final String sub : subs) {
                if (sub.startsWith(partial)) {
                    suggestions.add(sub);
                }
            }
            return suggestions;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("open") && player.hasPermission("backpackplus.admin")) {
            final String partial = args[1].toLowerCase();
            for (final Player online : Bukkit.getOnlinePlayers()) {
                if (online.getName().toLowerCase().startsWith(partial)) {
                    suggestions.add(online.getName());
                }
            }
            return suggestions;
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("open") && player.hasPermission("backpackplus.admin")) {
            final int maxPages = plugin.getConfig().getInt("max-pages", 5);
            for (int i = 1; i <= maxPages; i++) {
                suggestions.add(String.valueOf(i));
            }
            return suggestions;
        }

        return suggestions;
    }
}
