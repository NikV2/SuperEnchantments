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

    private double chance;
    private int duration;
    private int amplifier;

    public CurseEnchant(SuperEnchantments plugin) {
        super(plugin, "curse", "Curse", "&a", "SWORD", "AXE");

        setDescription("&7A chance to inflict wither", "&7on the players you hit");

        addOption("chance", 40.0D, "The chance of the enchant applying");
        addOption("duration", 60, "The effect duration (In ticks)");
        addOption("amplifier", 2, "The effect amplifier");
    }

    @Override
    protected void onLoad() {
        chance = getDouble("chance");
        duration = getInt("duration");
        amplifier = getInt("amplifier");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onAttack(EntityDamageByEntityEvent e) {

        if (!isEnabled()) return;

        Entity target = e.getEntity();
        Entity damager = e.getDamager();

        if ((!(target instanceof Player)) || (!(damager instanceof Player))) return;

        Player player = (Player) damager;

        ItemStack tool = player.getInventory().getItemInMainHand();

        if (!appliesTo(tool.getType()) || !hasEnchant(tool)) return;

        if (RandomUtils.chance(chance)) {
            ((Player) target).addPotionEffect(new PotionEffect(PotionEffectType.WITHER, duration, amplifier));
        }
    }
}
