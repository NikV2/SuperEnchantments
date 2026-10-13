package me.nik.superenchantments.enchants;

import me.nik.superenchantments.SuperEnchantments;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

public class StrikeEnchant extends CustomEnchant {

    public StrikeEnchant(SuperEnchantments plugin) {
        super(plugin, "strike", "Strike", "&a", "SWORD", "AXE");

        setDescription("&7Deals more damage", "&7the more health you are missing");
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onAttack(EntityDamageByEntityEvent e) {

        if (!isEnabled()) return;

        Entity damager = e.getDamager();

        if (!(damager instanceof Player)) return;

        Player player = (Player) damager;

        ItemStack tool = player.getInventory().getItemInMainHand();

        if (!appliesTo(tool.getType()) || !hasEnchant(tool)) return;

        double missingHealth = player.getMaxHealth() - player.getHealth();

        e.setDamage(e.getDamage() + missingHealth);
    }
}
