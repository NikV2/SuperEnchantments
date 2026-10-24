package me.nik.superenchantments.enchants.categories;

import me.nik.superenchantments.SuperEnchantments;
import me.nik.superenchantments.enums.Rarity;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public abstract class ArmorEnchant extends CustomEnchant {

    protected ArmorEnchant(SuperEnchantments plugin, String id, String displayName, String loreColor, Rarity rarity) {
        super(plugin, id, displayName, loreColor, rarity, "HELMET", "CHESTPLATE", "LEGGINGS", "BOOTS");
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