package me.nik.superenchantments.enchants;

import me.nik.superenchantments.SuperEnchantments;
import me.nik.superenchantments.enchants.categories.CustomEnchant;
import me.nik.superenchantments.enums.Rarity;
import me.nik.superenchantments.utils.MiscUtils;
import me.nik.superenchantments.utils.TaskUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class HarvesterEnchant extends CustomEnchant {

    private final Set<UUID> harvesting = new HashSet<>();

    private int radius;
    private boolean replant;
    private boolean sneakDisables;
    private boolean damageEnabled;
    private int perBlock;
    private boolean respectUnbreaking;
    private Set<Material> crops = EnumSet.noneOf(Material.class);
    private Set<String> disabledWorlds = new HashSet<>();
    private Set<GameMode> disabledGameModes = EnumSet.noneOf(GameMode.class);

    public HarvesterEnchant(SuperEnchantments plugin) {
        super(plugin, "harvester", "Harvester", "&a", Rarity.COMMON, "HOE");

        setDescription("&7Harvests all the fully grown crops", "&7around the one you break");

        addOption("radius", 1, "How far around the broken crop the other crops are harvested", "1 is a 3x3 area, 2 is 5x5");
        addOption("crops", Arrays.asList("WHEAT", "CARROTS", "POTATOES", "BEETROOTS", "NETHER_WART"), "The crops that can be harvested", "Only fully grown crops are harvested");
        addOption("replant", false, "Should the harvested crops grow back from their first stage for free?");
        addOption("sneak_disables", true, "Holding sneak turns Harvester off");
        addOption("disabled_worlds", new ArrayList<String>(), "Harvester does not work in these worlds", "Example: [world_nether, creative]");
        addOption("disabled_gamemodes", Arrays.asList("CREATIVE", "SPECTATOR"), "Harvester does not work in these gamemodes");
        addOption("tool_damage.enabled", true, "Should the extra crops wear the tool down?", "Harvester stops when the tool is nearly broken so the vanilla break happens on the crop you actually broke");
        addOption("tool_damage.per-block", 1, "Durability lost per extra crop");
        addOption("tool_damage.respect-unbreaking", true, "Should the Unbreaking enchant reduce the damage?");
    }

    @Override
    protected void onLoad() {

        radius = Math.max(1, getInt("radius"));
        replant = getBoolean("replant");
        sneakDisables = getBoolean("sneak_disables");
        damageEnabled = getBoolean("tool_damage.enabled");
        perBlock = Math.max(0, getInt("tool_damage.per-block"));
        respectUnbreaking = getBoolean("tool_damage.respect-unbreaking");

        crops = EnumSet.noneOf(Material.class);

        for (String name : getStringList("crops")) {

            Material material = Material.matchMaterial(name);

            if (material != null) crops.add(material);
        }

        disabledWorlds = new HashSet<>(getStringList("disabled_worlds"));

        disabledGameModes = EnumSet.noneOf(GameMode.class);

        for (String name : getStringList("disabled_gamemodes")) {

            try {
                disabledGameModes.add(GameMode.valueOf(name.toUpperCase()));
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e) {

        if (!isEnabled()) return;

        Player player = e.getPlayer();

        if (harvesting.contains(player.getUniqueId())) return;

        if (disabledGameModes.contains(player.getGameMode())) return;

        if (sneakDisables && player.isSneaking()) return;

        Block origin = e.getBlock();

        Material type = origin.getType();

        if (!crops.contains(type) || !isGrown(origin)) return;

        ItemStack tool = player.getInventory().getItemInMainHand();

        if (!appliesTo(tool.getType()) || !hasEnchant(tool)) return;

        if (!disabledWorlds.isEmpty() && disabledWorlds.contains(player.getWorld().getName())) return;

        TelepathyEnchant telepathy = TelepathyEnchant.get(plugin, tool);

        boolean brokeAny = false;
        boolean collected = false;

        harvesting.add(player.getUniqueId());

        try {

            outer:
            for (int x = -radius; x <= radius; x++) {

                for (int z = -radius; z <= radius; z++) {

                    if (x == 0 && z == 0) continue;

                    Block target = origin.getRelative(x, 0, z);

                    Material targetType = target.getType();

                    if (!crops.contains(targetType) || !isGrown(target)) continue;

                    BlockBreakEvent extra = new BlockBreakEvent(target, player);

                    Bukkit.getPluginManager().callEvent(extra);

                    if (extra.isCancelled()) continue;

                    if (damageEnabled && perBlock > 0) {

                        if (!MiscUtils.damageTool(tool, perBlock, respectUnbreaking)) break outer;
                    }

                    if (extra.isDropItems()) {

                        if (telepathy != null) {

                            collected |= telepathy.collect(player, target.getDrops(tool, player), target.getLocation().add(0.5, 0.5, 0.5));

                            MiscUtils.playBreakEffect(target);

                            target.setType(Material.AIR);

                        } else {
                            target.breakNaturally(tool);
                        }

                    } else {
                        target.setType(Material.AIR);
                    }

                    if (replant) target.setType(targetType);

                    brokeAny = true;
                }
            }

        } finally {
            harvesting.remove(player.getUniqueId());
        }

        if (replant) {
            TaskUtils.task(() -> {
                if (origin.getType().isAir()) origin.setType(type);
            });
        }

        if (!brokeAny) return;

        player.getInventory().setItemInMainHand(tool);

        if (collected) telepathy.playSound(player);
    }

    private boolean isGrown(Block block) {

        BlockData data = block.getBlockData();

        if (!(data instanceof Ageable)) return false;

        Ageable ageable = (Ageable) data;

        return ageable.getAge() >= ageable.getMaximumAge();
    }
}