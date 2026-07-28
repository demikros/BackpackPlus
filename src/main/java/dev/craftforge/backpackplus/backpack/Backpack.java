package dev.craftforge.backpackplus.backpack;

import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public final class Backpack {

    private final UUID ownerUuid;
    private final int page;
    private int rows;
    private ItemStack[] contents;
    private final AtomicBoolean dirty = new AtomicBoolean(false);
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public Backpack(final UUID ownerUuid, final int page, final int rows) {
        this.ownerUuid = ownerUuid;
        this.page = page;
        this.rows = rows;
        this.contents = new ItemStack[rows * 9];
    }

    public UUID getOwnerUuid() {
        return ownerUuid;
    }

    public int getPage() {
        return page;
    }

    public int getRows() {
        lock.readLock().lock();
        try {
            return rows;
        } finally {
            lock.readLock().unlock();
        }
    }

    public int size() {
        lock.readLock().lock();
        try {
            return rows * 9;
        } finally {
            lock.readLock().unlock();
        }
    }

    public ItemStack[] getContents() {
        lock.readLock().lock();
        try {
            return Arrays.copyOf(contents, contents.length);
        } finally {
            lock.readLock().unlock();
        }
    }

    public void setContents(final ItemStack[] newContents) {
        lock.writeLock().lock();
        try {
            this.contents = Arrays.copyOf(newContents, newContents.length);
            dirty.set(true);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void resizeTo(final int newRows) {
        lock.writeLock().lock();
        try {
            final int newSize = newRows * 9;
            this.contents = Arrays.copyOf(contents, newSize);
            this.rows = newRows;
            dirty.set(true);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public boolean isDirty() {
        return dirty.get();
    }

    public void clearDirty() {
        dirty.set(false);
    }

    public void markDirty() {
        dirty.set(true);
    }
}
