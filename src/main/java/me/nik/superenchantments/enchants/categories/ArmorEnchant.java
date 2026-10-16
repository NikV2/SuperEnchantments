package me.nik.superenchantments.enchants.categories;

import me.nik.superenchantments.SuperEnchantments;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public abstract class ArmorEnchant extends CustomEnchant {

    protected ArmorEnchant(SuperEnchantments plugin, String id, String displayName, String loreColor) {
        super(plugin, id, displayName, loreColor, "HELMET", "CHESTPLATE", "LEGGINGS", "BOOTS");
    }

    protected int countPieces(Player player) {

        int pieces = 0;

        for (ItemStack item : player.getInventory().getArmorContents()) {

            if (item == null || item.getType() == Material.AIR) continue;

            if (appliesTo(item.getType()) && hasEnchant(item)) pieces++;
        }

        return pieces;
    }
}