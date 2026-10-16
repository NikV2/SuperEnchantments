package me.nik.superenchantments.enchants;

import me.nik.superenchantments.SuperEnchantments;
import me.nik.superenchantments.enchants.categories.ArmorEnchant;
import me.nik.superenchantments.utils.ChatUtils;
import me.nik.superenchantments.utils.MiscUtils;
import me.nik.superenchantments.utils.RandomUtils;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class AegisEnchant extends ArmorEnchant {

    private static final int SEGMENTS = 10;

    private final Map<UUID, Shield> shields = new HashMap<>();
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    private double chance;
    private double absorption;
    private long durationMillis;
    private long cooldownMillis;
    private int particleCount;
    private float soundVolume;
    private String sound;
    private String breakSound;
    private String display;
    private String breakMessage;
    private String expireMessage;
    private Particle particle;

    public AegisEnchant(SuperEnchantments plugin) {
        super(plugin, "aegis", "Aegis", "&a");

        setDescription("&7A chance to surround you with a magical shield", "&7that absorbs the incoming damage");

        addOption("chance", 20.0D, "The chance of the shield appearing when you take damage");
        addOption("absorption", 8.0D, "The damage the shield can absorb");
        addOption("duration", 10, "The seconds the shield lasts");
        addOption("cooldown", 40, "The seconds before a new shield can appear", "It starts when the shield appears");
        addOption("display", "&b&lAegis &7[%bar%&7] &f%amount%&7/&f%max% &8- &f%time%s", "Shown in the action bar while the shield is active", "Placeholders: %bar%, %amount%, %max%, %time%", "Leave it empty to disable");
        addOption("break_message", "&c&lYour Aegis shield broke", "Shown when the shield is used up", "Leave it empty to disable");
        addOption("expire_message", "&7Your Aegis shield faded", "Shown when the shield runs out of time", "Leave it empty to disable");
        addOption("particle", "END_ROD", "NONE to disable", "Particle names depend on the server version");
        addOption("particle_count", 20, "The amount of particles");
        addOption("sound", "block.enchantment_table.use", "NONE to disable", "Use the namespaced sound key");
        addOption("break_sound", "item.shield.break", "NONE to disable", "Played when the shield ends");
        addOption("sound_volume", 0.6D, "The volume of the sounds");

        plugin.getUpdateTask().add(this::updatePlayer);
    }

    @Override
    protected void onLoad() {
        chance = getDouble("chance");
        absorption = Math.max(0.0D, getDouble("absorption"));
        durationMillis = Math.max(1, getInt("duration")) * 1000L;
        cooldownMillis = Math.max(0, getInt("cooldown")) * 1000L;
        display = getString("display");
        breakMessage = getString("break_message");
        expireMessage = getString("expire_message");
        particle = MiscUtils.getParticle(getString("particle"));
        particleCount = Math.max(1, getInt("particle_count"));
        soundVolume = (float) getDouble("sound_volume");
        sound = sound("sound");
        breakSound = sound("break_sound");
    }

    private String sound(String key) {

        String name = getString(key);

        return name.equalsIgnoreCase("NONE") ? null : name;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent e) {

        if (!isEnabled() || !(e.getEntity() instanceof Player)) return;

        Player player = (Player) e.getEntity();

        UUID uuid = player.getUniqueId();

        long now = System.currentTimeMillis();

        Shield shield = shields.get(uuid);

        if (shield != null && now >= shield.expires) {
            shields.remove(uuid);
            end(player, expireMessage);
            shield = null;
        }

        if (shield == null) {

            Long ready = cooldowns.get(uuid);

            if (ready != null && now < ready) return;

            if (countPieces(player) == 0 || !RandomUtils.chance(chance)) return;

            shield = new Shield(absorption, now + durationMillis);

            shields.put(uuid, shield);
            cooldowns.put(uuid, now + cooldownMillis);

            playEffects(player);
        }

        double damage = e.getFinalDamage();

        if (damage <= 0.0D) return;

        double absorbed = Math.min(shield.remaining, damage);

        e.setDamage(e.getDamage() * ((damage - absorbed) / damage));

        shield.remaining -= absorbed;

        if (shield.remaining <= 0.0D) {
            shields.remove(uuid);
            end(player, breakMessage);
            return;
        }

        sendShield(player, shield);
    }

    private void updatePlayer(Player player) {

        if (shields.isEmpty()) return;

        Shield shield = shields.get(player.getUniqueId());

        if (shield == null) return;

        if (System.currentTimeMillis() >= shield.expires) {
            shields.remove(player.getUniqueId());
            end(player, expireMessage);
            return;
        }

        sendShield(player, shield);
    }

    private void sendShield(Player player, Shield shield) {

        if (display.isEmpty()) return;

        int filled = (int) Math.ceil(shield.remaining / Math.max(absorption, 0.0001D) * SEGMENTS);

        StringBuilder bar = new StringBuilder("&b");

        for (int i = 0; i < SEGMENTS; i++) {

            if (i == filled) bar.append("&8");

            bar.append('|');
        }

        long seconds = Math.max(0L, (shield.expires - System.currentTimeMillis() + 999L) / 1000L);

        String text = display
                .replace("%bar%", bar)
                .replace("%amount%", String.format(Locale.ROOT, "%.1f", shield.remaining))
                .replace("%max%", String.format(Locale.ROOT, "%.1f", absorption))
                .replace("%time%", String.valueOf(seconds));

        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatUtils.format(text)));
    }

    private void end(Player player, String message) {

        if (!message.isEmpty()) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatUtils.format(message)));
        }

        if (breakSound != null) player.playSound(player.getLocation(), breakSound, soundVolume, 1.0F);
    }

    private void playEffects(Player player) {

        Location center = player.getLocation().add(0, player.getHeight() / 2, 0);

        if (particle != null) player.getWorld().spawnParticle(particle, center, particleCount, 0.5, 0.7, 0.5, 0.02);

        if (sound != null) player.getWorld().playSound(center, sound, soundVolume, 1.0F);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        shields.remove(e.getPlayer().getUniqueId());
        cooldowns.remove(e.getPlayer().getUniqueId());
    }

    private static class Shield {

        private final long expires;
        private double remaining;

        private Shield(double remaining, long expires) {
            this.remaining = remaining;
            this.expires = expires;
        }
    }
}