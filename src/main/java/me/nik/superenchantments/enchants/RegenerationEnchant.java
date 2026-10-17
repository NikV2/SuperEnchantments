package me.nik.superenchantments.enchants;

import me.nik.superenchantments.SuperEnchantments;
import me.nik.superenchantments.enchants.categories.ArmorEnchant;
import me.nik.superenchantments.utils.RandomUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RegenerationEnchant extends ArmorEnchant {

    private final Map<UUID, Long> cooldowns = new HashMap<>();

    private double chance;
    private int duration;
    private int amplifier;
    private long cooldownMillis;

    public RegenerationEnchant(SuperEnchantments plugin) {
        super(plugin, "regeneration", "Regeneration", "&a");

        setDescription("&7Taking damage has a chance to trigger", "&7a short burst of regeneration");

        addOption("chance", 25.0D, "The chance of the enchant applying");
        addOption("duration", 5, "The seconds the regeneration lasts");
        addOption("amplifier", 4, "The effect amplifier", "1 is Regeneration II");
        addOption("cooldown", 20, "The seconds before the enchant can apply again");
    }

    @Override
    protected void onLoad() {
        chance = getDouble("chance");
        duration = Math.max(1, getInt("duration")) * 20;
        amplifier = Math.max(0, getInt("amplifier"));
        cooldownMillis = Math.max(0, getInt("cooldown")) * 1000L;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent e) {

        if (!isEnabled() || !(e.getEntity() instanceof Player)) return;

        Player player = (Player) e.getEntity();

        if (player.getHealth() - e.getFinalDamage() <= 0.0D) return;

        long now = System.currentTimeMillis();

        Long ready = cooldowns.get(player.getUniqueId());

        if (ready != null && now < ready) return;

        if (countPieces(player) == 0 || !RandomUtils.chance(chance)) return;

        cooldowns.put(player.getUniqueId(), now + cooldownMillis);

        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, duration, amplifier));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        cooldowns.remove(e.getPlayer().getUniqueId());
    }
}