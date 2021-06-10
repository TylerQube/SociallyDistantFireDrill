package me.cuube.firedrill.utility;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class ItemBuilder {
    private ItemStack itemStack;

    public ItemBuilder(Material mat) {
        itemStack = new ItemStack(mat);
    }

    public ItemStack setName(String name) {
        ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName(name);
        itemStack.setItemMeta(meta);

        return itemStack;
    }

    public ItemStack setLore(List<String> lore) {
        ItemMeta meta = itemStack.getItemMeta();
        meta.setLore(lore);
        itemStack.setItemMeta(meta);

        return this.itemStack;
    }

    public ItemStack setAmount(int amount) {
        itemStack.setAmount(amount);
        return this.itemStack;
    }

    public ItemStack toItemStack() {
        return this.itemStack;
    }
}
