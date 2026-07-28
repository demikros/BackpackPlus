package dev.craftforge.backpackplus.command;

import dev.craftforge.backpackplus.util.Messages;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public final class EnderChestCommand implements CommandExecutor, TabCompleter {

    private final Messages messages;
    private final JavaPlugin plugin;

    public EnderChestCommand(final Messages messages, final JavaPlugin plugin) {
        this.messages = messages;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!(sender instanceof final Player viewer)) {
            sender.sendMessage(messages.get("no-permission"));
            return true;
        }

        if (!viewer.hasPermission("backpackplus.enderchest")) {
            messages.send(viewer, "no-permission");
            return true;
        }

        if (args.length == 0) {
            openEnderChest(viewer, viewer);
            return true;
        }

        if (!viewer.hasPermission("backpackplus.enderchest.others")) {
            messages.send(viewer, "no-permission");
            return true;
        }

        final Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            messages.send(viewer, "player-not-found", "{player}", args[0]);
            return true;
        }

        openEnderChest(viewer, target);
        return true;
    }

    private void openEnderChest(final Player viewer, final Player owner) {
        final String titleTemplate = plugin.getConfig().getString("enderchest-title", "Ender Chest | {player}");
        final String rawTitle = titleTemplate.replace("{player}", owner.getName());
        final var title = MiniMessage.miniMessage().deserialize(rawTitle);

        final var inventory = Bukkit.createInventory(null, 27, title);
        inventory.setContents(owner.getEnderChest().getContents());

        viewer.openInventory(owner.getEnderChest());
        messages.send(viewer, "enderchest-opened", "{player}", owner.getName());
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
        final List<String> suggestions = new ArrayList<>();

        if (!(sender instanceof final Player viewer)) {
            return suggestions;
        }

        if (args.length == 1 && viewer.hasPermission("backpackplus.enderchest.others")) {
            final String partial = args[0].toLowerCase();
            for (final Player online : Bukkit.getOnlinePlayers()) {
                if (online.getName().toLowerCase().startsWith(partial)) {
                    suggestions.add(online.getName());
                }
            }
        }

        return suggestions;
    }
}
