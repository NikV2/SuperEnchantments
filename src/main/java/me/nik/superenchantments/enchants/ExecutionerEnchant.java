package me.nik.superenchantments.enchants;

import me.nik.superenchantments.SuperEnchantments;
import me.nik.superenchantments.enchants.categories.CustomEnchant;
import me.nik.superenchantments.utils.MiscUtils;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

public class ExecutionerEnchant extends CustomEnchant {

    private double threshold;
    private double bonus;

    public ExecutionerEnchant(SuperEnchantments plugin) {
        super(plugin, "executioner", "Executioner", "&a", "SWORD", "AXE");

        setDescription("&7Deals bonus damage to targets", "&7that are low on health");

        addOption("health_threshold", 30.0D, "The bonus applies when the target has this percent of its health or less");
        addOption("bonus_damage", 20.0D, "The percent of the hit that is added as bonus damage");
    }

    @Override
    protected void onLoad() {
        threshold = getDouble("health_threshold");
        bonus = Math.max(0.0D, getDouble("bonus_damage")) / 100.0D;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onAttack(EntityDamageByEntityEvent e) {

        if (!isEnabled()) return;

        if (!(e.getDamager() instanceof Player) || !(e.getEntity() instanceof LivingEntity) || e.getEntity() instanceof ArmorStand || !MiscUtils.isMelee(e)) return;

        Player player = (Player) e.getDamager();

        ItemStack tool = player.getInventory().getItemInMainHand();

        if (!appliesTo(tool.getType()) || !hasEnchant(tool)) return;

        LivingEntity target = (LivingEntity) e.getEntity();

        if (target.getHealth() / target.getMaxHealth() * 100.0D > threshold) return;

        e.setDamage(e.getDamage() * (1.0D + bonus));
    }
}