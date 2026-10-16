package me.nik.superenchantments.enchants;

import me.nik.superenchantments.SuperEnchantments;
import me.nik.superenchantments.enchants.categories.CustomEnchant;
import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

public class SniperEnchant extends CustomEnchant {

    private double multiplier;

    public SniperEnchant(SuperEnchantments plugin) {
        super(plugin, "sniper", "Sniper", "&a", "BOW", "CROSSBOW");

        setDescription("&7Your arrows deal more damage", "&7the further they travel");

        addOption("distance_multiplier", 0.5, "The distance between you and the target is multiplied by this and added to the arrow damage", "The maximum is 1.0");
    }

    @Override
    protected void onLoad() {
        multiplier = Math.min(1.0, getDouble("distance_multiplier"));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onAttack(EntityDamageByEntityEvent e) {

        if (!isEnabled()) return;

        Entity damager = e.getDamager();

        if (!(damager instanceof Arrow)) return;

        Arrow arrow = (Arrow) damager;

        if (!(arrow.getShooter() instanceof Player)) return;

        Player player = (Player) arrow.getShooter();

        ItemStack tool = player.getInventory().getItemInMainHand();

        if (!appliesTo(tool.getType()) || !hasEnchant(tool)) return;

        Location from = player.getLocation();
        Location to = e.getEntity().getLocation();

        double x = from.getX() - to.getX();
        double z = from.getZ() - to.getZ();

        e.setDamage(e.getDamage() + Math.sqrt(x * x + z * z) * multiplier);
    }
}
