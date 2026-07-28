package dev.craftforge.backpackplus.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class InventorySorter {

    private InventorySorter() {}

    public static ItemStack[] sort(final ItemStack[] contents) {
        final Map<Material, List<ItemStack>> grouped = new HashMap<>();

        for (final ItemStack item : contents) {
            if (item == null || item.getType() == Material.AIR) {
                continue;
            }
            grouped.computeIfAbsent(item.getType(), k -> new ArrayList<>()).add(item.clone());
        }

        final List<ItemStack> merged = new ArrayList<>();

        grouped.entrySet().stream()
                .sorted(Comparator.comparing(e -> e.getKey().name()))
                .forEach(entry -> {
                    int totalAmount = entry.getValue().stream().mapToInt(ItemStack::getAmount).sum();
                    final int maxStack = entry.getKey().getMaxStackSize();
                    final ItemStack template = entry.getValue().get(0);

                    while (totalAmount > 0) {
                        final int stackSize = Math.min(totalAmount, maxStack);
                        final ItemStack stack = template.clone();
                        stack.setAmount(stackSize);
                        merged.add(stack);
                        totalAmount -= stackSize;
                    }
                });

        final ItemStack[] result = new ItemStack[contents.length];
        for (int i = 0; i < merged.size() && i < result.length; i++) {
            result[i] = merged.get(i);
        }
        return result;
    }
}
