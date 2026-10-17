package me.nik.superenchantments.enchants;

import me.nik.superenchantments.SuperEnchantments;
import me.nik.superenchantments.enchants.categories.ArmorEffectEnchant;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

public class SwiftEnchant extends ArmorEffectEnchant {

    private int amplifier;

    public SwiftEnchant(SuperEnchantments plugin) {
        super(plugin, "swift", "Swift", "&a", PotionEffectType.SPEED, "BOOTS");

        setDescription("&7Gives you speed", "&7while the boots are worn");

        addOption("amplifier", 1, "The effect amplifier");
    }

    @Override
    protected void onLoad() {
        amplifier = getInt("amplifier");
    }

    @Override
    protected ItemStack getItem(Player player) {
        return player.getInventory().getBoots();
    }

    @Override
    protected int getAmplifier() {
        return amplifier;
    }
}
