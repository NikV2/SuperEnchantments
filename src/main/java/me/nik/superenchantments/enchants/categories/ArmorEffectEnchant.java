package me.nik.superenchantments.enchants.categories;

import me.nik.superenchantments.SuperEnchantments;
import me.nik.superenchantments.enums.Rarity;
import me.nik.superenchantments.utils.TaskUtils;
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

public abstract class ArmorEffectEnchant extends CustomEnchant {

    private final Set<UUID> managedPlayers = new HashSet<>();

    private final PotionEffectType effect;

    protected ArmorEffectEnchant(SuperEnchantments plugin, String id, String displayName, String loreColor, Rarity rarity, PotionEffectType effect, String... appliesTo) {
        super(plugin, id, displayName, loreColor, rarity, appliesTo);
        this.effect = effect;

        plugin.getUpdateTask().add(this::updatePlayer);
    }

    protected abstract ItemStack getItem(Player player);

    protected int getAmplifier() {
        return 0;
    }

    private boolean isWearing(Player player) {

        if (!isEnabled()) return false;

        ItemStack item = getItem(player);

        if (item == null || item.getType() == Material.AIR) return false;

        return appliesTo(item.getType()) && hasEnchant(item);
    }

    private void updatePlayer(Player player) {

        UUID uuid = player.getUniqueId();

        if (!isWearing(player)) {
            removeEffect(player);
            return;
        }

        PotionEffect current = player.getPotionEffect(effect);

        if (!managedPlayers.contains(uuid) && current != null) return;

        int amplifier = getAmplifier();

        if (current != null && current.getAmplifier() == amplifier && current.getDuration() > 300) return;

        player.addPotionEffect(new PotionEffect(effect, 600, amplifier, false, false), true);

        managedPlayers.add(uuid);
    }

    private void removeEffect(Player player) {

        if (managedPlayers.remove(player.getUniqueId())) player.removePotionEffect(effect);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent e) {
        updatePlayer(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRespawn(PlayerRespawnEvent e) {
        TaskUtils.task(() -> updatePlayer(e.getPlayer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldChange(PlayerChangedWorldEvent e) {
        updatePlayer(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(PlayerDeathEvent e) {
        removeEffect(e.getEntity());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        managedPlayers.remove(e.getPlayer().getUniqueId());
    }
}
