package me.nik.superenchantments.enchants;

import me.nik.superenchantments.SuperEnchantments;
import me.nik.superenchantments.enchants.categories.CustomEnchant;
import me.nik.superenchantments.enums.Rarity;
import me.nik.superenchantments.utils.MiscUtils;
import me.nik.superenchantments.utils.RandomUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class FrostbiteEnchant extends CustomEnchant {

    private final NamespacedKey key;

    private double chance;
    private int duration;
    private int amplifier;
    private int particleCount;
    private Particle particle;
    private PotionEffectType slowness;

    public FrostbiteEnchant(SuperEnchantments plugin) {
        super(plugin, "frostbite", "Frostbite", "&a", Rarity.COMMON, "SWORD", "AXE", "BOW", "CROSSBOW");

        key = new NamespacedKey(plugin, "frostbite");

        setDescription("&7A chance to briefly slow your target", "&7and release a burst of frost");

        addOption("chance", 20.0D, "The chance of the enchant applying");
        addOption("duration", 3, "The seconds the slowness lasts");
        addOption("amplifier", 1, "The effect amplifier", "1 is Slowness II");
        addOption("particle", "SNOWFLAKE", "NONE to disable", "Particle names depend on the server version");
        addOption("particle_count", 20, "The amount of particles");
    }

    @Override
    protected void onLoad() {
        chance = getDouble("chance");
        duration = Math.max(1, getInt("duration")) * 20;
        amplifier = Math.max(0, getInt("amplifier"));
        particleCount = Math.max(1, getInt("particle_count"));
        slowness = PotionEffectType.getByName("SLOW");

        String name = getString("particle");

        particle = MiscUtils.getParticle(name);

        if (particle == null && !name.equalsIgnoreCase("NONE")) particle = MiscUtils.getParticle("CLOUD");
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

        if (!isEnabled() || slowness == null || !(e.getEntity() instanceof LivingEntity)) return;

        Entity damager = e.getDamager();

        if (damager instanceof Projectile) {

            if (!damager.getPersistentDataContainer().has(key, PersistentDataType.BYTE)) return;

        } else if (damager instanceof Player) {

            if (!MiscUtils.isMelee(e)) return;

            ItemStack tool = ((Player) damager).getInventory().getItemInMainHand();

            if (!appliesTo(tool.getType()) || !hasEnchant(tool)) return;

        } else {
            return;
        }

        if (!RandomUtils.chance(chance)) return;

        LivingEntity target = (LivingEntity) e.getEntity();

        target.addPotionEffect(new PotionEffect(slowness, duration, amplifier));

        if (particle != null) target.getWorld().spawnParticle(particle, target.getLocation().add(0, target.getHeight() / 2, 0), particleCount, 0.4, 0.5, 0.4, 0.02);
    }
}