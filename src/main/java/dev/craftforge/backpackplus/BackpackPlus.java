package dev.craftforge.backpackplus;

import dev.craftforge.backpackplus.backpack.BackpackHolder;
import dev.craftforge.backpackplus.backpack.BackpackService;
import dev.craftforge.backpackplus.command.BackpackCommand;
import dev.craftforge.backpackplus.command.EnderChestCommand;
import dev.craftforge.backpackplus.listener.BackpackListener;
import dev.craftforge.backpackplus.listener.ConnectionListener;
import dev.craftforge.backpackplus.storage.BackpackStorage;
import dev.craftforge.backpackplus.util.Messages;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public final class BackpackPlus extends JavaPlugin {

    private BackpackStorage storage;
    private BackpackService service;
    private Messages messages;
    private BukkitTask flushTask;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        storage = new BackpackStorage(this);
        messages = new Messages(getConfig());
        service = new BackpackService(storage, this, messages);

        final BackpackListener backpackListener = new BackpackListener(service, messages, this);
        final ConnectionListener connectionListener = new ConnectionListener(service);

        getServer().getPluginManager().registerEvents(backpackListener, this);
        getServer().getPluginManager().registerEvents(connectionListener, this);

        final BackpackCommand backpackCommand = new BackpackCommand(service, messages, this, backpackListener);
        getCommand("backpack").setExecutor(backpackCommand);
        getCommand("backpack").setTabCompleter(backpackCommand);

        final EnderChestCommand enderChestCommand = new EnderChestCommand(messages, this);
        getCommand("enderchest").setExecutor(enderChestCommand);
        getCommand("enderchest").setTabCompleter(enderChestCommand);

        final long flushInterval = getConfig().getLong("flush-interval-seconds", 300) * 20L;
        flushTask = Bukkit.getScheduler().runTaskTimerAsynchronously(this, service::flushDirty, flushInterval, flushInterval);

        getLogger().info("BackpackPlus enabled.");
    }

    @Override
    public void onDisable() {
        if (flushTask != null) {
            flushTask.cancel();
        }

        for (final Player player : Bukkit.getOnlinePlayers()) {
            final Inventory topInventory = player.getOpenInventory().getTopInventory();
            final InventoryHolder holder = topInventory.getHolder();
            if (holder instanceof final BackpackHolder backpackHolder) {
                backpackHolder.getBackpack().setContents(topInventory.getContents());
                player.closeInventory();
            }
        }

        service.saveAllSync();
        getLogger().info("BackpackPlus disabled, all data saved.");
    }
}
