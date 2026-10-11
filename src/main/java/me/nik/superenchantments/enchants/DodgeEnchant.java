package me.nik.superenchantments.enchants;

import me.nik.superenchantments.SuperEnchantments;
import me.nik.superenchantments.utils.RandomUtils;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

public class DodgeEnchant extends CustomEnchant {
    public DodgeEnchant(SuperEnchantments plugin) {
        super(plugin, "dodge", "Dodge", "&a", "HELMET", "CHESTPLATE", "LEGGINGS", "BOOTS");

        addOption("chance", 20.0D, "The chance of the enchant applying");
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBossAttack(EntityDamageByEntityEvent e) {

        if (!isEnabled()) return;

        Entity target = e.getEntity();
        Entity damager = e.getDamager();

        if ((!(target instanceof Player)) || damager == null) return;

        Player targetPlayer = (Player) target;

        if (!hasDodgeArmor(targetPlayer)) return;

        if (RandomUtils.chance(getDouble("chance"))) {
            e.setDamage(0D);
        }
    }

    private boolean hasDodgeArmor(Player player) {

        for (ItemStack item : player.getInventory().getArmorContents()) {

            if (item == null || item.getType() == Material.AIR) continue;

            if (appliesTo(item.getType()) && hasEnchant(item)) {
                return true;
            }
        }

        return false;
    }
}