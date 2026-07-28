package dev.craftforge.backpackplus.backpack;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.UUID;

public final class BackpackHolder implements InventoryHolder {

    private final UUID ownerUuid;
    private final int page;
    private final Backpack backpack;
    private Inventory inventory;

    public BackpackHolder(final UUID ownerUuid, final int page, final Backpack backpack) {
        this.ownerUuid = ownerUuid;
        this.page = page;
        this.backpack = backpack;
    }

    public UUID getOwnerUuid() {
        return ownerUuid;
    }

    public int getPage() {
        return page;
    }

    public Backpack getBackpack() {
        return backpack;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void setInventory(final Inventory inventory) {
        this.inventory = inventory;
    }
}
