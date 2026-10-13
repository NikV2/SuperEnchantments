package me.nik.superenchantments.enchants;

import me.nik.superenchantments.SuperEnchantments;
import me.nik.superenchantments.utils.MiscUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Container;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class BlastEnchant extends CustomEnchant {

    private final Set<UUID> blasting = new HashSet<>();

    private int radius;
    private boolean sneakDisables;
    private boolean dropItems;
    private boolean dropXp;
    private boolean skipContainers;
    private boolean damageEnabled;
    private int perBlock;
    private boolean respectUnbreaking;
    private int particleCount;
    private float soundVolume;
    private float soundPitch;
    private String sound;
    private Particle particle;
    private Enchantment unbreaking;
    private Set<String> disabledWorlds = new HashSet<>();
    private Set<GameMode> disabledGameModes = EnumSet.noneOf(GameMode.class);
    private Set<Material> blockedBlocks = EnumSet.noneOf(Material.class);

    public BlastEnchant(SuperEnchantments plugin) {
        super(plugin, "blast", "Blast", "&a", "PICKAXE");

        setDescription("&7Mines the blocks around the one you break", "&7and digs deeper the higher the radius is");

        addOption("radius", 3, "How many extra 3x3 layers are mined forward", "The width and height never grow, only the depth does");
        addOption("sneak_disables", true, "Holding sneak turns Blast off");
        addOption("disabled_worlds", new ArrayList<String>(), "Blast does not work in these worlds", "Example: [world_nether, creative]");
        addOption("disabled_gamemodes", Arrays.asList("CREATIVE", "SPECTATOR"), "Blast does not work in these gamemodes");
        addOption("drop_items", true, "false = the extra blocks just vanish");
        addOption("drop_xp", true, "Drop experience from the extra blocks");
        addOption("skip_containers", true, "Never break chests, furnaces, hoppers and so on");
        addOption("blocked_blocks", Arrays.asList(
                "BEDROCK",
                "BARRIER",
                "SPAWNER",
                "END_PORTAL",
                "END_PORTAL_FRAME",
                "COMMAND_BLOCK",
                "CHAIN_COMMAND_BLOCK",
                "REPEATING_COMMAND_BLOCK",
                "STRUCTURE_BLOCK",
                "JIGSAW"
        ), "Blocks that Blast never breaks");
        addOption("tool_damage.enabled", true, "Should the extra blocks wear the tool down?", "Blast stops when the tool is nearly broken so the vanilla break happens on the block you actually mined");
        addOption("tool_damage.per-block", 1, "Durability lost per extra block");
        addOption("tool_damage.respect-unbreaking", true, "Should the Unbreaking enchant reduce the damage?");
        addOption("effects.particle", "CLOUD", "NONE to disable", "Particle names depend on the server version");
        addOption("effects.particle_count", 5, "The amount of particles");
        addOption("effects.sound", "entity.generic.explode", "NONE to disable", "Use the namespaced sound key");
        addOption("effects.sound_volume", 0.4, "The volume of the sound");
        addOption("effects.sound_pitch", 1.2, "The pitch of the sound");
    }

    @Override
    protected void onLoad() {

        radius = Math.max(1, getInt("radius"));
        sneakDisables = getBoolean("sneak_disables");
        dropItems = getBoolean("drop_items");
        dropXp = getBoolean("drop_xp");
        skipContainers = getBoolean("skip_containers");
        damageEnabled = getBoolean("tool_damage.enabled");
        perBlock = Math.max(0, getInt("tool_damage.per-block"));
        respectUnbreaking = getBoolean("tool_damage.respect-unbreaking");
        particleCount = Math.max(1, getInt("effects.particle_count"));
        soundVolume = (float) getDouble("effects.sound_volume");
        soundPitch = (float) getDouble("effects.sound_pitch");

        unbreaking = Enchantment.getByKey(NamespacedKey.minecraft("unbreaking"));

        disabledWorlds = new HashSet<>(getStringList("disabled_worlds"));

        disabledGameModes = EnumSet.noneOf(GameMode.class);

        for (String name : getStringList("disabled_gamemodes")) {

            try {
                disabledGameModes.add(GameMode.valueOf(name.toUpperCase()));
            } catch (IllegalArgumentException ignored) {
            }
        }

        blockedBlocks = EnumSet.noneOf(Material.class);

        for (String name : getStringList("blocked_blocks")) {

            Material material = Material.matchMaterial(name);

            if (material != null) blockedBlocks.add(material);
        }

        String particleName = getString("effects.particle");

        particle = null;

        if (!particleName.equalsIgnoreCase("NONE")) {

            try {
                particle = Particle.valueOf(particleName.toUpperCase());
            } catch (IllegalArgumentException ignored) {
            }
        }

        String soundName = getString("effects.sound");

        sound = soundName.equalsIgnoreCase("NONE") ? null : soundName;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e) {

        if (!isEnabled()) return;

        Player player = e.getPlayer();

        if (blasting.contains(player.getUniqueId())) return;

        if (disabledGameModes.contains(player.getGameMode())) return;

        if (sneakDisables && player.isSneaking()) return;

        ItemStack tool = player.getInventory().getItemInMainHand();

        if (!appliesTo(tool.getType())) return;

        if (!hasEnchant(tool)) return;

        if (!disabledWorlds.isEmpty() && disabledWorlds.contains(player.getWorld().getName())) return;

        Block origin = e.getBlock();

        BlockFace miningDirection = getMiningDirection(player);

        TelepathyEnchant telepathy = TelepathyEnchant.get(plugin, tool);

        boolean brokeAny = false;
        boolean collected = false;

        blasting.add(player.getUniqueId());

        try {

            outer:
            for (int depth = 0; depth <= radius; depth++) {

                for (int x = -1; x <= 1; x++) {

                    for (int y = -1; y <= 1; y++) {

                        if (depth == 0 && x == 0 && y == 0) continue;

                        Block target = getBlastBlock(origin, miningDirection, depth, x, y);

                        if (target.equals(origin)) continue;

                        if (!canBlast(target)) continue;

                        BlockBreakEvent extra = new BlockBreakEvent(target, player);

                        Bukkit.getPluginManager().callEvent(extra);

                        if (extra.isCancelled()) continue;

                        if (damageEnabled && perBlock > 0) {

                            if (!MiscUtils.damageTool(tool, perBlock, respectUnbreaking)) break outer;
                        }

                        int exp = extra.getExpToDrop();

                        if (dropItems && extra.isDropItems()) {

                            if (telepathy != null && (skipContainers || !(target.getState() instanceof Container))) {

                                collected |= telepathy.collect(player, target.getDrops(tool, player), target.getLocation().add(0.5, 0.5, 0.5));

                                MiscUtils.playBreakEffect(target);

                                target.setType(Material.AIR);

                            } else {
                                target.breakNaturally(tool);
                            }

                        } else {
                            target.setType(Material.AIR);
                        }

                        if (dropXp && exp > 0) {
                            Location center = target.getLocation().add(0.5, 0.5, 0.5);
                            target.getWorld().spawn(center, ExperienceOrb.class, orb -> orb.setExperience(exp));
                        }

                        brokeAny = true;
                    }
                }
            }

        } finally {
            blasting.remove(player.getUniqueId());
        }

        if (brokeAny) {
            player.getInventory().setItemInMainHand(tool);
            playEffects(origin);

            if (collected) telepathy.playSound(player);
        }
    }

    private Block getBlastBlock(Block origin, BlockFace direction, int depth, int x, int y) {

        if (direction == BlockFace.EAST || direction == BlockFace.WEST) {
            return origin.getRelative(direction.getModX() * depth, y, x);
        }

        if (direction == BlockFace.UP || direction == BlockFace.DOWN) {
            return origin.getRelative(x, direction.getModY() * depth, y);
        }

        return origin.getRelative(x, y, direction.getModZ() * depth);
    }

    private BlockFace getMiningDirection(Player player) {

        Vector direction = player.getEyeLocation().getDirection().normalize();

        double x = Math.abs(direction.getX());
        double y = Math.abs(direction.getY());
        double z = Math.abs(direction.getZ());

        if (y > x && y > z) {
            return direction.getY() > 0 ? BlockFace.UP : BlockFace.DOWN;
        }

        if (x > z) {
            return direction.getX() > 0 ? BlockFace.EAST : BlockFace.WEST;
        }

        return direction.getZ() > 0 ? BlockFace.SOUTH : BlockFace.NORTH;
    }

    private boolean canBlast(Block block) {

        Material type = block.getType();

        if (type.isAir()) return false;

        if (block.isLiquid()) return false;

        if (type.getHardness() < 0) return false;

        if (blockedBlocks.contains(type)) return false;

        return !(skipContainers && block.getState() instanceof Container);
    }

    private void playEffects(Block origin) {

        if (particle == null && sound == null) return;

        Location loc = origin.getLocation().add(0.5, 0.5, 0.5);

        if (particle != null) origin.getWorld().spawnParticle(particle, loc, particleCount, 0.4, 0.4, 0.4, 0.02);

        if (sound != null) origin.getWorld().playSound(loc, sound, soundVolume, soundPitch);
    }
}
