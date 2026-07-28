package dev.craftforge.backpackplus.listener;

import dev.craftforge.backpackplus.backpack.BackpackService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class ConnectionListener implements Listener {

    private final BackpackService service;

    public ConnectionListener(final BackpackService service) {
        this.service = service;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onAsyncPreLogin(final AsyncPlayerPreLoginEvent event) {
        if (event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) {
            return;
        }
        service.loadAsync(event.getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(final PlayerQuitEvent event) {
        service.flushAndEvict(event.getPlayer().getUniqueId());
    }
}
