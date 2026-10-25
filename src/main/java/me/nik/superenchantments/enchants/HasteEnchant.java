package me.nik.superenchantments.enchants;

import me.nik.superenchantments.SuperEnchantments;
import me.nik.superenchantments.enchants.categories.EffectEnchant;
import me.nik.superenchantments.enums.Rarity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

public class HasteEnchant extends EffectEnchant {
    private int amplifier;

    public HasteEnchant(SuperEnchantments plugin) {
        super(plugin, "haste", "Haste", "&a", Rarity.RARE, PotionEffectType.HASTE, "PICKAXE", "SHOVEL");

        setDescription("&7Gives you haste", "&7while holding the item");

        addOption("amplifier", 1, "The effect amplifier");
    }

    @Override
    protected void onLoad() {
        amplifier = getInt("amplifier");
    }

    @Override
    protected ItemStack getItem(Player player) {
        return player.getInventory().getItemInMainHand();
    }

    @Override
    protected int getAmplifier() {
        return amplifier;
    }
}