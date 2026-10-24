package me.nik.superenchantments.enchants;

import me.nik.superenchantments.SuperEnchantments;
import me.nik.superenchantments.enchants.categories.CustomEnchant;
import me.nik.superenchantments.enums.Rarity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

public class StrikeEnchant extends CustomEnchant {

    private double multiplier;

    public StrikeEnchant(SuperEnchantments plugin) {
        super(plugin, "strike", "Strike", "&a", Rarity.COMMON, "SWORD", "AXE");

        setDescription("&7Deals more damage", "&7the more health you are missing");

        addOption("multiplier", 0.5, "The multiplier of the missing health", "Max: 1.0");
    }

    @Override
    protected void onLoad() {
        multiplier = getDouble("multiplier");
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onAttack(EntityDamageByEntityEvent e) {

        if (!isEnabled()) return;

        Entity damager = e.getDamager();

        if (!(damager instanceof Player)) return;

        Player player = (Player) damager;

        ItemStack tool = player.getInventory().getItemInMainHand();

        if (!appliesTo(tool.getType()) || !hasEnchant(tool)) return;

        double missingHealth = (player.getMaxHealth() - player.getHealth()) * multiplier;

        e.setDamage(e.getDamage() + missingHealth);
    }
}
