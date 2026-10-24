package me.nik.superenchantments.enchants;

import me.nik.superenchantments.SuperEnchantments;
import me.nik.superenchantments.enchants.categories.CustomEnchant;
import me.nik.superenchantments.enums.Rarity;
import me.nik.superenchantments.utils.MiscUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class PiercingSoulEnchant extends CustomEnchant {

    private final NamespacedKey key;

    private double maxPenetration;

    public PiercingSoulEnchant(SuperEnchantments plugin) {
        super(plugin, "piercingsoul", "Piercing Soul", "&a", Rarity.LEGENDARY, "BOW", "CROSSBOW");

        key = new NamespacedKey(plugin, "piercing_soul");

        setDescription("&7Your arrows ignore part of the armor", "&7and defenses of whatever they hit");

        addOption("max_penetration", 20.0D, "The maximum percent of armor and other damage reduction an arrow ignores", "A fully drawn bow reaches the maximum, a weaker shot ignores less");
    }

    @Override
    protected void onLoad() {
        maxPenetration = Math.max(0.0D, Math.min(100.0D, getDouble("max_penetration"))) / 100.0D;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onShoot(EntityShootBowEvent e) {

        if (!isEnabled()) return;

        ItemStack bow = e.getBow();

        if (bow == null || !appliesTo(bow.getType()) || !hasEnchant(bow)) return;

        double force = Math.min(1.0D, e.getForce());

        e.getProjectile().getPersistentDataContainer().set(key, PersistentDataType.DOUBLE, maxPenetration * force);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onAttack(EntityDamageByEntityEvent e) {

        if (!isEnabled() || !(e.getDamager() instanceof Projectile)) return;

        Double penetration = e.getDamager().getPersistentDataContainer().get(key, PersistentDataType.DOUBLE);

        if (penetration == null) return;

        MiscUtils.ignoreMitigation(e, penetration);
    }
}