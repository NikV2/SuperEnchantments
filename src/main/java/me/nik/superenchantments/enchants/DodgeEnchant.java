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

    private double chance;

    public DodgeEnchant(SuperEnchantments plugin) {
        super(plugin, "dodge", "Dodge", "&a", "HELMET", "CHESTPLATE", "LEGGINGS", "BOOTS");

        setDescription("&7A chance to completely", "&7avoid the damage you take");

        addOption("chance", 30.0D, "The chance of the enchant applying");
    }

    @Override
    protected void onLoad() {
        chance = getDouble("chance");
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onAttack(EntityDamageByEntityEvent e) {

        if (!isEnabled()) return;

        Entity target = e.getEntity();

        if (!(target instanceof Player)) return;

        if (!hasDodgeArmor((Player) target)) return;

        if (RandomUtils.chance(chance)) {
            e.setDamage(0D);
        }
    }

    private boolean hasDodgeArmor(Player player) {

        for (ItemStack item : player.getInventory().getArmorContents()) {

            if (item == null || item.getType() == Material.AIR) continue;

            if (appliesTo(item.getType()) && hasEnchant(item)) return true;
        }

        return false;
    }
}
