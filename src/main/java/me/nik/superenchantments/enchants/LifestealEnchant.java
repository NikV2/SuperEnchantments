package me.nik.superenchantments.enchants;

import me.nik.superenchantments.SuperEnchantments;
import me.nik.superenchantments.enchants.categories.CustomEnchant;
import me.nik.superenchantments.enums.Rarity;
import me.nik.superenchantments.utils.RandomUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

public class LifestealEnchant extends CustomEnchant {

    private double chance;

    public LifestealEnchant(SuperEnchantments plugin) {
        super(plugin, "lifesteal", "LifeSteal", "&a", Rarity.LEGENDARY, "SWORD", "AXE", "TRIDENT", "SPEAR", "MACE");

        setDescription("&7A chance to steal health", "&7from the things you hit");

        addOption("chance", 50.0D, "The chance of the enchant applying");
    }

    @Override
    protected void onLoad() {
        chance = getDouble("chance");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onAttack(EntityDamageByEntityEvent e) {

        if (!isEnabled()) return;

        Entity damager = e.getDamager();

        if (!(damager instanceof Player)) return;

        Player player = (Player) damager;

        ItemStack tool = player.getInventory().getItemInMainHand();

        if (!appliesTo(tool.getType()) || !hasEnchant(tool)) return;

        if (!RandomUtils.chance(chance)) return;

        double stolen = e.getFinalDamage();

        if (e.getEntity() instanceof LivingEntity) {
            stolen = Math.min(stolen, ((LivingEntity) e.getEntity()).getHealth());
        }

        if (stolen <= 0.0D) return;

        player.setHealth(Math.min(player.getMaxHealth(), player.getHealth() + stolen));
    }
}
