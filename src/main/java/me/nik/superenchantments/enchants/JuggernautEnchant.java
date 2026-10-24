package me.nik.superenchantments.enchants;

import me.nik.superenchantments.SuperEnchantments;
import me.nik.superenchantments.enchants.categories.ArmorEnchant;
import me.nik.superenchantments.enums.Rarity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerVelocityEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class JuggernautEnchant extends ArmorEnchant {

    private final Map<UUID, Knockback> knockbacks = new HashMap<>();

    private double damagePerPiece;
    private double maxDamage;
    private double knockbackPerPiece;
    private double maxKnockback;

    public JuggernautEnchant(SuperEnchantments plugin) {
        super(plugin, "juggernaut", "Juggernaut", "&a", Rarity.RARE);

        setDescription("&7Reduces the knockback you take", "&7and some of the damage");

        addOption("damage_reduction", 3.0D, "The percent of damage reduced for every enchanted armor piece");
        addOption("max_damage_reduction", 12.0D, "The maximum percent of damage that can be reduced");
        addOption("knockback_reduction", 7.5D, "The percent of knockback reduced for every enchanted armor piece");
        addOption("max_knockback_reduction", 30.0D, "The maximum percent of knockback that can be reduced");
    }

    @Override
    protected void onLoad() {
        damagePerPiece = percent("damage_reduction");
        maxDamage = percent("max_damage_reduction");
        knockbackPerPiece = percent("knockback_reduction");
        maxKnockback = percent("max_knockback_reduction");
    }

    private double percent(String key) {
        return Math.max(0.0D, Math.min(100.0D, getDouble(key))) / 100.0D;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent e) {

        if (!isEnabled() || !(e.getEntity() instanceof Player)) return;

        Player player = (Player) e.getEntity();

        int pieces = countPieces(player);

        if (pieces == 0) return;

        e.setDamage(e.getDamage() * (1.0D - Math.min(maxDamage, damagePerPiece * pieces)));

        if (e instanceof EntityDamageByEntityEvent) {
            knockbacks.put(player.getUniqueId(), new Knockback(Math.min(maxKnockback, knockbackPerPiece * pieces)));
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onVelocity(PlayerVelocityEvent e) {

        Knockback knockback = knockbacks.remove(e.getPlayer().getUniqueId());

        if (knockback == null || System.currentTimeMillis() - knockback.time > 250L) return;

        e.setVelocity(e.getPlayer().getVelocity().multiply(1.0D - knockback.factor));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        knockbacks.remove(e.getPlayer().getUniqueId());
    }

    private static class Knockback {

        private final double factor;
        private final long time = System.currentTimeMillis();

        private Knockback(double factor) {
            this.factor = factor;
        }
    }
}