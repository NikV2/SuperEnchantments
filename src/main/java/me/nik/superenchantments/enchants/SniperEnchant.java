package me.nik.superenchantments.enchants;

import me.nik.superenchantments.SuperEnchantments;
import me.nik.superenchantments.enchants.categories.CustomEnchant;
import me.nik.superenchantments.enums.Rarity;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class SniperEnchant extends CustomEnchant {

    private final NamespacedKey keyX;
    private final NamespacedKey keyZ;

    private double minDistance;
    private double bonusPerBlock;
    private double maxBonus;

    public SniperEnchant(SuperEnchantments plugin) {
        super(plugin, "sniper", "Sniper", "&a", Rarity.RARE, "BOW", "CROSSBOW");

        keyX = new NamespacedKey(plugin, "sniper_x");
        keyZ = new NamespacedKey(plugin, "sniper_z");

        setDescription("&7Your arrows deal more damage", "&7the further they travel");

        addOption("min_distance", 10.0D, "Arrows that travel less than this many blocks do not get a bonus");
        addOption("bonus_per_block", 2.0D, "The damage bonus in percent for every block the arrow travels after the minimum distance");
        addOption("max_bonus", 100.0D, "The maximum damage bonus in percent");
    }

    @Override
    protected void onLoad() {
        minDistance = Math.max(0.0D, getDouble("min_distance"));
        bonusPerBlock = Math.max(0.0D, getDouble("bonus_per_block")) / 100.0D;
        maxBonus = Math.max(0.0D, getDouble("max_bonus")) / 100.0D;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onShoot(EntityShootBowEvent e) {

        if (!isEnabled()) return;

        ItemStack bow = e.getBow();

        if (bow == null || !appliesTo(bow.getType()) || !hasEnchant(bow)) return;

        Entity projectile = e.getProjectile();

        Location origin = projectile.getLocation();

        PersistentDataContainer container = projectile.getPersistentDataContainer();

        container.set(keyX, PersistentDataType.DOUBLE, origin.getX());
        container.set(keyZ, PersistentDataType.DOUBLE, origin.getZ());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onAttack(EntityDamageByEntityEvent e) {

        if (!isEnabled() || !(e.getDamager() instanceof Projectile)) return;

        PersistentDataContainer container = e.getDamager().getPersistentDataContainer();

        Double x = container.get(keyX, PersistentDataType.DOUBLE);
        Double z = container.get(keyZ, PersistentDataType.DOUBLE);

        if (x == null || z == null) return;

        Location hit = e.getDamager().getLocation();

        double dx = hit.getX() - x;
        double dz = hit.getZ() - z;

        double distance = Math.sqrt(dx * dx + dz * dz);

        double bonus = Math.min(maxBonus, Math.max(0.0D, distance - minDistance) * bonusPerBlock);

        if (bonus <= 0.0D) return;

        e.setDamage(e.getDamage() * (1.0D + bonus));
    }
}