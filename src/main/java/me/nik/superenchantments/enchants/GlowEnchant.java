package me.nik.superenchantments.enchants;

import me.nik.superenchantments.SuperEnchantments;
import me.nik.superenchantments.enchants.categories.ArmorEffectEnchant;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

public class GlowEnchant extends ArmorEffectEnchant {

    public GlowEnchant(SuperEnchantments plugin) {
        super(plugin, "glow", "Glow", "&a", PotionEffectType.NIGHT_VISION, "HELMET");

        setDescription("&7Gives you night vision", "&7while the helmet is worn");
    }

    @Override
    protected ItemStack getItem(Player player) {
        return player.getInventory().getHelmet();
    }
}
