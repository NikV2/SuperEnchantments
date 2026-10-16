package me.nik.superenchantments.enchants;

import me.nik.superenchantments.SuperEnchantments;
import me.nik.superenchantments.enchants.categories.CustomEnchant;
import me.nik.superenchantments.utils.MiscUtils;
import me.nik.superenchantments.utils.RandomUtils;
import me.nik.superenchantments.utils.TaskUtils;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

import java.util.concurrent.ThreadLocalRandom;

public class WindburstEnchant extends CustomEnchant {

    private final NamespacedKey key;

    private double chance;
    private double upward;
    private double knockback;
    private boolean launchMode;
    private boolean pushMode;
    private int particleCount;
    private float soundVolume;
    private String sound;
    private Particle particle;

    public WindburstEnchant(SuperEnchantments plugin) {
        super(plugin, "windburst", "Windburst", "&a", "SWORD", "AXE", "BOW", "CROSSBOW");

        key = new NamespacedKey(plugin, "windburst");

        setDescription("&7Launches the target upward", "&7or knocks it back with a burst of wind");

        addOption("chance", 20.0D, "The chance of the enchant applying");
        addOption("mode", "RANDOM", "UPWARD, KNOCKBACK, BOTH or RANDOM", "RANDOM picks one of the two every time");
        addOption("upward_strength", 0.7D, "How hard the target is launched upward");
        addOption("knockback_strength", 1.0D, "How hard the target is pushed away");
        addOption("particle", "CLOUD", "NONE to disable", "Particle names depend on the server version");
        addOption("particle_count", 15, "The amount of particles");
        addOption("sound", "entity.phantom.flap", "NONE to disable");
        addOption("sound_volume", 0.6D, "The volume of the sound");
    }

    @Override
    protected void onLoad() {
        chance = getDouble("chance");
        upward = getDouble("upward_strength");
        knockback = getDouble("knockback_strength");
        particle = MiscUtils.getParticle(getString("particle"));
        particleCount = Math.max(1, getInt("particle_count"));
        soundVolume = (float) getDouble("sound_volume");

        String name = getString("sound");

        sound = name.equalsIgnoreCase("NONE") ? null : name;

        String mode = getString("mode");

        launchMode = mode.equalsIgnoreCase("UPWARD") || mode.equalsIgnoreCase("BOTH");
        pushMode = mode.equalsIgnoreCase("KNOCKBACK") || mode.equalsIgnoreCase("BOTH");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onShoot(EntityShootBowEvent e) {

        if (!isEnabled()) return;

        ItemStack bow = e.getBow();

        if (bow == null || !appliesTo(bow.getType()) || !hasEnchant(bow)) return;

        e.getProjectile().getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onAttack(EntityDamageByEntityEvent e) {

        if (!isEnabled() || !(e.getEntity() instanceof LivingEntity) || e.getEntity() instanceof ArmorStand) return;

        Entity damager = e.getDamager();

        Entity source;

        if (damager instanceof Projectile) {

            if (!damager.getPersistentDataContainer().has(key, PersistentDataType.BYTE)) return;

            ProjectileSource shooter = ((Projectile) damager).getShooter();

            if (!(shooter instanceof Entity)) return;

            source = (Entity) shooter;

        } else if (damager instanceof Player) {

            if (!MiscUtils.isMelee(e)) return;

            ItemStack tool = ((Player) damager).getInventory().getItemInMainHand();

            if (!appliesTo(tool.getType()) || !hasEnchant(tool)) return;

            source = damager;

        } else {
            return;
        }

        if (source.equals(e.getEntity()) || !RandomUtils.chance(chance)) return;

        LivingEntity target = (LivingEntity) e.getEntity();

        boolean launch = launchMode;
        boolean push = pushMode;

        if (!launch && !push) {
            launch = ThreadLocalRandom.current().nextBoolean();
            push = !launch;
        }

        Vector velocity = new Vector(0, 0, 0);

        Vector direction = target.getLocation().toVector().subtract(source.getLocation().toVector()).setY(0);

        if (push && direction.lengthSquared() > 0.0D) velocity.add(direction.normalize().multiply(knockback));

        if (launch) velocity.setY(upward);

        TaskUtils.task(() -> target.setVelocity(target.getVelocity().add(velocity)));

        Location center = target.getLocation().add(0, target.getHeight() / 2, 0);

        if (particle != null) target.getWorld().spawnParticle(particle, center, particleCount, 0.4, 0.4, 0.4, 0.05);

        if (sound != null) target.getWorld().playSound(center, sound, soundVolume, 1.0F);
    }
}