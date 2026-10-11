package me.nik.superenchantments.enchants;

import me.nik.superenchantments.SuperEnchantments;
import me.nik.superenchantments.utils.RandomUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class CurseEnchant extends CustomEnchant {
    public CurseEnchant(SuperEnchantments plugin) {
        super(plugin, "curse", "Curse", "&a", "SWORD", "AXE");

        addOption("chance", 40.0D, "The chance of the enchant applying");
        addOption("duration", 5, "The effect duration");
        addOption("amplifier", 2, "The effect amplifier");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBossAttack(EntityDamageByEntityEvent e) {

        if (!isEnabled()) return;

        Entity target = e.getEntity();
        Entity damager = e.getDamager();

        if ((!(target instanceof Player)) || (!(damager instanceof Player))) return;

        Player player = (Player) damager;

        ItemStack tool = player.getInventory().getItemInMainHand();

        if (!appliesTo(tool.getType()) || !hasEnchant(tool)) return;

        Player targetPlayer = (Player) target;

        if (RandomUtils.chance(getDouble("chance"))) {
            targetPlayer.addPotionEffect(new PotionEffect(
                    PotionEffectType.WITHER, getInt("duration"), getInt("amplifier"))
            );
        }
    }
}