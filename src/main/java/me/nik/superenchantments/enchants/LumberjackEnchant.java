package me.nik.superenchantments.enchants;

import me.nik.superenchantments.SuperEnchantments;
import me.nik.superenchantments.utils.MiscUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Leaves;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class LumberjackEnchant extends CustomEnchant {

    private static final int SCAN_LIMIT = 200;

    private final Set<UUID> felling = new HashSet<>();

    private int maxBlocks;
    private int maxDistance;
    private boolean naturalOnly;
    private boolean sneakDisables;
    private boolean dropItems;
    private boolean damageEnabled;
    private int perBlock;
    private boolean respectUnbreaking;
    private Set<String> disabledWorlds = new HashSet<>();
    private Set<GameMode> disabledGameModes = EnumSet.noneOf(GameMode.class);

    public LumberjackEnchant(SuperEnchantments plugin) {
        super(plugin, "lumberjack", "Lumberjack", "&a", "AXE");

        setDescription("&7Chops connected logs at once");

        addOption("max_blocks", 100, "The most logs that get broken at once, including the one you mine");
        addOption("max_distance", 5, "How far from the mined log the other logs can be");
        addOption("natural_trees_only", true, "Only works on logs that touch natural leaves", "This keeps log builds safe");
        addOption("sneak_disables", true, "Holding sneak turns Lumberjack off");
        addOption("disabled_worlds", new ArrayList<String>(), "Lumberjack does not work in these worlds", "Example: [world_nether, creative]");
        addOption("disabled_gamemodes", Arrays.asList("CREATIVE", "SPECTATOR"), "Lumberjack does not work in these gamemodes");
        addOption("drop_items", true, "false = the extra logs just vanish");
        addOption("tool_damage.enabled", true, "Should the extra logs wear the tool down?", "Lumberjack stops when the tool is nearly broken so the vanilla break happens on the log you actually mined");
        addOption("tool_damage.per-block", 1, "Durability lost per extra log");
        addOption("tool_damage.respect-unbreaking", true, "Should the Unbreaking enchant reduce the damage?");
    }

    @Override
    protected void onLoad() {

        maxBlocks = Math.max(1, getInt("max_blocks"));
        maxDistance = Math.max(1, getInt("max_distance"));
        naturalOnly = getBoolean("natural_trees_only");
        sneakDisables = getBoolean("sneak_disables");
        dropItems = getBoolean("drop_items");
        damageEnabled = getBoolean("tool_damage.enabled");
        perBlock = Math.max(0, getInt("tool_damage.per-block"));
        respectUnbreaking = getBoolean("tool_damage.respect-unbreaking");

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

        if (felling.contains(player.getUniqueId())) return;

        if (disabledGameModes.contains(player.getGameMode())) return;

        if (sneakDisables && player.isSneaking()) return;

        Block origin = e.getBlock();

        Material type = origin.getType();

        if (!Tag.LOGS.isTagged(type)) return;

        ItemStack tool = player.getInventory().getItemInMainHand();

        if (!appliesTo(tool.getType()) || !hasEnchant(tool)) return;

        if (!disabledWorlds.isEmpty() && disabledWorlds.contains(player.getWorld().getName())) return;

        List<Block> logs = findLogs(origin, type);

        if (logs.isEmpty()) return;

        TelepathyEnchant telepathy = TelepathyEnchant.get(plugin, tool);

        boolean brokeAny = false;
        boolean collected = false;

        felling.add(player.getUniqueId());

        try {

            for (Block log : logs) {

                if (log.getType() != type) continue;

                BlockBreakEvent extra = new BlockBreakEvent(log, player);

                Bukkit.getPluginManager().callEvent(extra);

                if (extra.isCancelled()) continue;

                if (damageEnabled && perBlock > 0) {

                    if (!MiscUtils.damageTool(tool, perBlock, respectUnbreaking)) break;
                }

                if (dropItems && extra.isDropItems()) {

                    if (telepathy != null) {

                        collected |= telepathy.collect(player, log.getDrops(tool, player), log.getLocation().add(0.5, 0.5, 0.5));

                        MiscUtils.playBreakEffect(log);

                        log.setType(Material.AIR);

                    } else {
                        log.breakNaturally(tool);
                    }

                } else {
                    log.setType(Material.AIR);
                }

                brokeAny = true;
            }

        } finally {
            felling.remove(player.getUniqueId());
        }

        if (!brokeAny) return;

        player.getInventory().setItemInMainHand(tool);

        if (collected) telepathy.playSound(player);
    }

    private List<Block> findLogs(Block origin, Material type) {

        List<Block> logs = new ArrayList<>();

        Set<Block> visited = new HashSet<>();

        Deque<Block> queue = new ArrayDeque<>();

        visited.add(origin);
        queue.add(origin);

        boolean natural = !naturalOnly;

        while (!queue.isEmpty() && visited.size() < SCAN_LIMIT) {

            Block current = queue.poll();

            if (!natural && hasNaturalLeaves(current)) natural = true;

            if (natural && logs.size() >= maxBlocks - 1) break;

            for (int x = -1; x <= 1; x++) {

                for (int y = -1; y <= 1; y++) {

                    for (int z = -1; z <= 1; z++) {

                        Block next = current.getRelative(x, y, z);

                        if (next.getType() != type || !visited.add(next)) continue;

                        if (Math.abs(next.getX() - origin.getX()) > maxDistance || Math.abs(next.getZ() - origin.getZ()) > maxDistance) continue;

                        queue.add(next);

                        if (logs.size() < maxBlocks - 1) logs.add(next);
                    }
                }
            }
        }

        return natural ? logs : Collections.<Block>emptyList();
    }

    private boolean hasNaturalLeaves(Block block) {

        for (int x = -1; x <= 1; x++) {

            for (int y = -1; y <= 1; y++) {

                for (int z = -1; z <= 1; z++) {

                    if (x == 0 && y == 0 && z == 0) continue;

                    Block neighbour = block.getRelative(x, y, z);

                    Material type = neighbour.getType();

                    if (type.name().endsWith("WART_BLOCK")) return true;

                    if (!Tag.LEAVES.isTagged(type)) continue;

                    BlockData data = neighbour.getBlockData();

                    if (data instanceof Leaves && !((Leaves) data).isPersistent()) return true;
                }
            }
        }

        return false;
    }
}