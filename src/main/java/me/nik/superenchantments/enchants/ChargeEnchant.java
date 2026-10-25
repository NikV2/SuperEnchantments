package me.nik.superenchantments.enchants;

import me.nik.superenchantments.SuperEnchantments;
import me.nik.superenchantments.enchants.categories.CustomEnchant;
import me.nik.superenchantments.enums.Rarity;
import me.nik.superenchantments.utils.ChatUtils;
import me.nik.superenchantments.utils.MiscUtils;
import me.nik.superenchantments.utils.TaskUtils;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChargeEnchant extends CustomEnchant {

    private final Map<UUID, Integer> energy = new HashMap<>();
    private final Map<UUID, Long> lastHit = new HashMap<>();

    private int hits;
    private long timeoutMillis;
    private double releaseDamage;
    private double releaseKnockback;
    private String display;
    private String readyDisplay;
    private String releaseMessage;
    private String barFilled;
    private String barEmpty;
    private int particleCount;
    private float soundVolume;
    private String sound;
    private Particle particle;

    public ChargeEnchant(SuperEnchantments plugin) {
        super(plugin, "charge", "Charge", "&a", Rarity.RARE, "SWORD", "AXE", "TRIDENT");

        setDescription("&7Consecutive hits build up rage", "&7and the next hit after a full charge", "&7releases a powerful blast");

        addOption("hits_required", 5, "The consecutive hits needed to fully charge", "The hit after that releases the effect");
        addOption("combo_timeout", 4, "The seconds without hitting before the charge is lost");
        addOption("release_damage", 6.0D, "The extra damage dealt when the charge is released");
        addOption("release_knockback", 1.2D, "How hard the target is pushed away when released", "0 to disable");
        addOption("display", "&bCharge &7[%bar%&7] &f%charge%&7/&f%max%", "Shown in the action bar while charging", "Placeholders: %bar%, %charge%, %max%", "Leave it empty to disable");
        addOption("ready_display", "&bCharge &7[%bar%&7] &a&lREADY", "Shown in the action bar when the charge is full", "Placeholders: %bar%, %charge%, %max%", "Leave it empty to disable");
        addOption("release_message", "&b&lCharge released!", "Shown in the action bar when the charge is released", "Leave it empty to disable");
        addOption("bar_filled", "&b\u25A0", "The part of the bar for every hit of charge");
        addOption("bar_empty", "&8\u25A1", "The part of the bar for every missing hit of charge");
        addOption("particle", "SWEEP_ATTACK", "NONE to disable", "Particle names depend on the server version");
        addOption("particle_count", 3, "The amount of particles");
        addOption("sound", "entity.generic.explode", "NONE to disable");
        addOption("sound_volume", 0.6D, "The volume of the sound");
    }

    @Override
    protected void onLoad() {
        hits = Math.max(1, getInt("hits_required"));
        timeoutMillis = Math.max(1, getInt("combo_timeout")) * 1000L;
        releaseDamage = getDouble("release_damage");
        releaseKnockback = getDouble("release_knockback");
        display = getString("display");
        readyDisplay = getString("ready_display");
        releaseMessage = getString("release_message");
        barFilled = getString("bar_filled");
        barEmpty = getString("bar_empty");
        particle = MiscUtils.getParticle(getString("particle"));
        particleCount = Math.max(1, getInt("particle_count"));
        soundVolume = (float) getDouble("sound_volume");

        String name = getString("sound");

        sound = name.equalsIgnoreCase("NONE") ? null : name;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onAttack(EntityDamageByEntityEvent e) {

        if (!isEnabled()) return;

        if (!(e.getDamager() instanceof Player) || !(e.getEntity() instanceof LivingEntity) || e.getEntity() instanceof ArmorStand || !MiscUtils.isMelee(e)) return;

        Player player = (Player) e.getDamager();

        ItemStack tool = player.getInventory().getItemInMainHand();

        if (!appliesTo(tool.getType()) || !hasEnchant(tool)) return;

        UUID uuid = player.getUniqueId();

        long now = System.currentTimeMillis();

        Long last = lastHit.put(uuid, now);

        int current = last == null || now - last > timeoutMillis ? 0 : energy.getOrDefault(uuid, 0);

        if (current >= hits) {
            energy.put(uuid, 0);
            release(player, e);
            return;
        }

        energy.put(uuid, current + 1);

        sendMeter(player, current + 1);
    }

    private void release(Player player, EntityDamageByEntityEvent e) {

        LivingEntity target = (LivingEntity) e.getEntity();

        e.setDamage(e.getDamage() + releaseDamage);

        if (releaseKnockback > 0.0D) {

            Vector push = target.getLocation().toVector().subtract(player.getLocation().toVector()).setY(0);

            if (push.lengthSquared() > 0.0D) {

                Vector velocity = push.normalize().multiply(releaseKnockback).setY(0.3D);

                TaskUtils.task(() -> target.setVelocity(velocity));
            }
        }

        Location center = target.getLocation().add(0, target.getHeight() / 2, 0);

        if (particle != null) target.getWorld().spawnParticle(particle, center, particleCount, 0.4, 0.4, 0.4, 0.1);

        if (sound != null) target.getWorld().playSound(center, sound, soundVolume, 1.0F);

        if (!releaseMessage.isEmpty()) send(player, releaseMessage);
    }

    private void sendMeter(Player player, int filled) {

        String format = filled >= hits ? readyDisplay : display;

        if (format.isEmpty()) return;

        StringBuilder bar = new StringBuilder();

        for (int i = 0; i < hits; i++) bar.append(i < filled ? barFilled : barEmpty);

        send(player, format
                .replace("%bar%", bar)
                .replace("%charge%", String.valueOf(filled))
                .replace("%max%", String.valueOf(hits)));
    }

    private void send(Player player, String text) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatUtils.format(text)));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        energy.remove(e.getPlayer().getUniqueId());
        lastHit.remove(e.getPlayer().getUniqueId());
    }
}