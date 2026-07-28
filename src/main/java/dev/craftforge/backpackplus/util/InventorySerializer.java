package dev.craftforge.backpackplus.util;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

public final class InventorySerializer {

    private InventorySerializer() {}

    public static String toBase64(final ItemStack[] contents) throws IOException {
        try (final ByteArrayOutputStream baos = new ByteArrayOutputStream();
             final BukkitObjectOutputStream boos = new BukkitObjectOutputStream(baos)) {
            boos.writeInt(contents.length);
            for (final ItemStack item : contents) {
                boos.writeObject(item);
            }
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        }
    }

    public static ItemStack[] fromBase64(final String data, final int expectedSize) throws IOException, ClassNotFoundException {
        final byte[] bytes = Base64.getDecoder().decode(data);
        try (final ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
             final BukkitObjectInputStream bois = new BukkitObjectInputStream(bais)) {
            final int size = bois.readInt();
            final ItemStack[] contents = new ItemStack[size];
            for (int i = 0; i < size; i++) {
                contents[i] = (ItemStack) bois.readObject();
            }
            return contents;
        }
    }
}
