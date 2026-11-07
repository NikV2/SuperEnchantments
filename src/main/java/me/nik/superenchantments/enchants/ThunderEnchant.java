package me.nik.superenchantments.enchants;

import me.nik.superenchantments.SuperEnchantments;
import me.nik.superenchantments.enchants.categories.CustomEnchant;
import me.nik.superenchantments.enums.Rarity;
import me.nik.superenchantments.utils.MiscUtils;
import me.nik.superenchantments.utils.RandomUtils;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

public class ThunderEnchant extends CustomEnchant {

    private boolean striking;
    private boolean damagePlayers;
    private double chance;
    private double damage;
    private double radius;
    private double minCharge;

    public ThunderEnchant(SuperEnchantments plugin) {
        super(plugin, "thunder", "Thunder", "&a", Rarity.LEGENDARY, "SWORD", "AXE", "SPEAR", "TRIDENT");

        setDescription("&7A chance to call lightning on your target", "&7and shock everything around it");

        addOption("chance", 20.0D, "The chance of the enchant applying");
        addOption("damage", 4.0D, "The bonus damage dealt to the target and everything around it");
        addOption("radius", 3.0D, "The radius around the target that gets shocked");
        addOption("min_charge", 90.0D, "The percent of the attack charge that is needed for the enchant to apply", "100 means only fully charged hits, 0 disables the check");
        addOption("damage_players", true, "Should players take the bonus damage?");
    }

    @Override
    protected void onLoad() {
        chance = getDouble("chance");
        damage = getDouble("damage");
        radius = getDouble("radius");
        minCharge = Math.max(0.0D, Math.min(100.0D, getDouble("min_charge"))) / 100.0D;
        damagePlayers = getBoolean("damage_players");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onAttack(EntityDamageByEntityEvent e) {

        if (striking || !isEnabled()) return;

        if (!(e.getDamager() instanceof Player) || !(e.getEntity() instanceof LivingEntity)) return;

        Player player = (Player) e.getDamager();

        ItemStack tool = player.getInventory().getItemInMainHand();

        if (!appliesTo(tool.getType()) || !hasEnchant(tool) || MiscUtils.getAttackCharge(player) < minCharge) return;

        if (!RandomUtils.chance(chance)) return;

        LivingEntity target = (LivingEntity) e.getEntity();

        Location location = target.getLocation();

        location.getWorld().strikeLightningEffect(location);

        striking = true;

        try {

            shock(player, target);

            for (Entity nearby : target.getNearbyEntities(radius, radius, radius)) shock(player, nearby);

        } finally {
            striking = false;
        }
    }

    private void shock(Player attacker, Entity entity) {

        if (!(entity instanceof LivingEntity) || entity instanceof ArmorStand || entity.equals(attacker)) return;

        if (entity instanceof Player && !damagePlayers) return;

        if (entity instanceof Tameable && attacker.equals(((Tameable) entity).getOwner())) return;

        ((LivingEntity) entity).damage(damage, attacker);
    }
}
