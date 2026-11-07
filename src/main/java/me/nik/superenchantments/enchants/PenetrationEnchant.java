package me.nik.superenchantments.enchants;

import me.nik.superenchantments.SuperEnchantments;
import me.nik.superenchantments.enchants.categories.CustomEnchant;
import me.nik.superenchantments.enums.Rarity;
import me.nik.superenchantments.utils.MiscUtils;
import me.nik.superenchantments.utils.RandomUtils;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

public class PenetrationEnchant extends CustomEnchant {

    private double chance;
    private double extraDamage;
    private double minCharge;
    private double armorIgnore;
    private int particleCount;
    private Particle particle;

    public PenetrationEnchant(SuperEnchantments plugin) {
        super(plugin, "penetration", "Penetration", "&a", Rarity.LEGENDARY, "SWORD");

        setDescription("&7A chance to deal extra damage", "&7that ignores part of the target's armor");

        addOption("chance", 20.0D, "The chance of the enchant applying");
        addOption("extra_damage", 2.0D, "The extra damage dealt when the enchant applies");
        addOption("armor_ignore", 30.0D, "The percent of the armor and other damage reduction that gets ignored", "100 ignores all of it");
        addOption("min_charge", 90.0D, "The percent of the attack charge that is needed for the enchant to apply", "100 means only fully charged hits, 0 disables the check");
        addOption("particle", "CRIT", "NONE to disable", "Particle names depend on the server version");
        addOption("particle_count", 15, "The amount of particles");
    }

    @Override
    protected void onLoad() {
        chance = getDouble("chance");
        extraDamage = getDouble("extra_damage");
        armorIgnore = Math.max(0.0D, Math.min(100.0D, getDouble("armor_ignore"))) / 100.0D;
        particleCount = Math.max(1, getInt("particle_count"));
        minCharge = Math.max(0.0D, Math.min(100.0D, getDouble("min_charge"))) / 100.0D;
        particle = MiscUtils.getParticle(getString("particle"));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onAttack(EntityDamageByEntityEvent e) {

        if (!isEnabled()) return;

        if (!(e.getDamager() instanceof Player) || !(e.getEntity() instanceof LivingEntity) || !MiscUtils.isMelee(e)) return;

        Player player = (Player) e.getDamager();

        ItemStack tool = player.getInventory().getItemInMainHand();

        if (!appliesTo(tool.getType()) || !hasEnchant(tool) || MiscUtils.getAttackCharge(player) < minCharge) return;

        if (!RandomUtils.chance(chance)) return;

        e.setDamage(e.getDamage() + extraDamage);

        MiscUtils.ignoreMitigation(e, armorIgnore);

        if (particle == null) return;

        Entity target = e.getEntity();

        target.getWorld().spawnParticle(particle, target.getLocation().add(0, target.getHeight() / 2, 0), particleCount, 0.3, 0.3, 0.3, 0.05);
    }
}