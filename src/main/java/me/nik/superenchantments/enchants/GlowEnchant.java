package me.nik.superenchantments.enchants;

import me.nik.superenchantments.SuperEnchantments;
import me.nik.superenchantments.enums.Permissions;
import me.nik.superenchantments.tasks.UpdateTask;
import me.nik.superenchantments.utils.TaskUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class GlowEnchant extends CustomEnchant {

    private final Set<UUID> managedPlayers = new HashSet<>();

    public GlowEnchant(SuperEnchantments plugin) {
        super(plugin, "glow", "Glow", "&a", "HELMET");

        // Keep Night Vision synchronized with the player's helmet.
        UpdateTask.add(() -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                updatePlayer(player);
            }
        });
    }

    private boolean hasGlowHelmet(Player player) {
        if (!isEnabled()) {
            return false;
        }

        ItemStack helmet = player.getInventory().getHelmet();

        if (helmet == null || helmet.getType() == Material.AIR) {
            return false;
        }

        return appliesTo(helmet.getType()) && hasEnchant(helmet);
    }

    private void updatePlayer(Player player) {
        UUID uuid = player.getUniqueId();

        if (!hasGlowHelmet(player)) {
            removeGlow(player);
            return;
        }

        PotionEffect current = player.getPotionEffect(PotionEffectType.NIGHT_VISION);

        // Don't overwrite an existing effect that we don't manage.
        if (!managedPlayers.contains(uuid) && current != null) {
            return;
        }

        // Refresh before it expires to prevent flickering.
        player.addPotionEffect(
                new PotionEffect(
                        PotionEffectType.NIGHT_VISION,
                        60,
                        0,
                        false,
                        false),
                true
        );

        managedPlayers.add(uuid);
    }

    /**
     * Removes the Night Vision effect managed by this enchantment.
     */
    private void removeGlow(Player player) {
        UUID uuid = player.getUniqueId();

        if (managedPlayers.remove(uuid)) {
            player.removePotionEffect(PotionEffectType.NIGHT_VISION);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        updatePlayer(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRespawn(PlayerRespawnEvent e) {
        // Run after the respawn process has restored the player's state.
        TaskUtils.task(() -> updatePlayer(e.getPlayer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldChange(PlayerChangedWorldEvent e) {
        updatePlayer(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(PlayerDeathEvent e) {
        removeGlow(e.getEntity());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        // Don't retain player UUIDs after they leave.
        managedPlayers.remove(e.getPlayer().getUniqueId());
    }
}