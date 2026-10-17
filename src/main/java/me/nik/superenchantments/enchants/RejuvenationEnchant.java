package me.nik.superenchantments.enchants;

import me.nik.superenchantments.SuperEnchantments;
import me.nik.superenchantments.enchants.categories.CustomEnchant;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RejuvenationEnchant extends CustomEnchant {

    private final Map<UUID, Long> lastCombat = new HashMap<>();
    private final Map<UUID, Long> nextRestore = new HashMap<>();

    private boolean health;
    private boolean hunger;
    private double healthAmount;
    private int hungerAmount;
    private long intervalMillis;
    private long combatMillis;

    public RejuvenationEnchant(SuperEnchantments plugin) {
        super(plugin, "rejuvenation", "Rejuvenation", "&a", "HELMET", "CHESTPLATE", "LEGGINGS", "BOOTS");

        setDescription("&7Slowly restores health and hunger", "&7while you are out of combat");

        addOption("restore", "BOTH", "What gets restored", "HEALTH, HUNGER or BOTH");
        addOption("health_amount", 2.0D, "The health restored each time", "1.0 is half a heart");
        addOption("hunger_amount", 2, "The hunger restored each time", "1 is half a drumstick");
        addOption("interval", 3, "The seconds between each restore");
        addOption("combat_timeout", 10, "The seconds without fighting before the enchant starts working");

        plugin.getUpdateTask().add(this::updatePlayer);
    }

    @Override
    protected void onLoad() {

        String restore = getString("restore");

        health = restore.equalsIgnoreCase("HEALTH") || restore.equalsIgnoreCase("BOTH");
        hunger = restore.equalsIgnoreCase("HUNGER") || restore.equalsIgnoreCase("BOTH");

        healthAmount = getDouble("health_amount");
        hungerAmount = getInt("hunger_amount");
        intervalMillis = Math.max(1, getInt("interval")) * 1000L;
        combatMillis = Math.max(0, getInt("combat_timeout")) * 1000L;
    }

    private void updatePlayer(Player player) {

        if (!isEnabled() || player.isDead()) return;

        boolean needsHealth = health && player.getHealth() < player.getMaxHealth();
        boolean needsHunger = hunger && player.getFoodLevel() < 20;

        if (!needsHealth && !needsHunger) return;

        UUID uuid = player.getUniqueId();

        long now = System.currentTimeMillis();

        Long combat = lastCombat.get(uuid);

        if (combat != null && now - combat < combatMillis) return;

        Long next = nextRestore.get(uuid);

        if (next != null && now < next) return;

        if (!isWearing(player)) return;

        nextRestore.put(uuid, now + intervalMillis);

        if (needsHealth) player.setHealth(Math.min(player.getMaxHealth(), player.getHealth() + healthAmount));

        if (needsHunger) player.setFoodLevel(Math.min(20, player.getFoodLevel() + hungerAmount));
    }

    private boolean isWearing(Player player) {

        for (ItemStack item : player.getInventory().getArmorContents()) {

            if (item == null || item.getType() == Material.AIR) continue;

            if (appliesTo(item.getType()) && hasEnchant(item)) return true;
        }

        return false;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent e) {

        if (!isEnabled()) return;

        long now = System.currentTimeMillis();

        if (e.getEntity() instanceof Player) lastCombat.put(e.getEntity().getUniqueId(), now);

        Entity damager = e.getDamager();

        if (damager instanceof Projectile && ((Projectile) damager).getShooter() instanceof Entity) {
            damager = (Entity) ((Projectile) damager).getShooter();
        }

        if (damager instanceof Player) lastCombat.put(damager.getUniqueId(), now);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        lastCombat.remove(e.getPlayer().getUniqueId());
        nextRestore.remove(e.getPlayer().getUniqueId());
    }
}