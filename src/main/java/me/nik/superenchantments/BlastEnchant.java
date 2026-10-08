package me.nik.superenchantments;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Container;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Blast:
 *
 * Mines a fixed 3x3 area around the block being broken,
 * then extends that 3x3 area forward based on radius.
 *
 * radius: 1
 * = original 3x3 + 1 additional 3x3 layer
 *
 * radius: 2
 * = original 3x3 + 2 additional 3x3 layers
 *
 * radius: 3
 * = original 3x3 + 3 additional 3x3 layers
 *
 * The width and height NEVER increase with radius.
 */
public final class BlastEnchant extends CustomEnchant implements Listener {

    /**
     * Players currently being processed by Blast.
     *
     * This prevents our artificial BlockBreakEvents
     * from triggering Blast recursively.
     */
    private final Set<UUID> blasting = new HashSet<>();

    public BlastEnchant(SuperEnchantments plugin) {
        super(plugin, "blast");
    }

    @EventHandler(
            priority = EventPriority.HIGH,
            ignoreCancelled = true
    )
    public void onBreak(BlockBreakEvent event) {

        if (!isEnabled()) {
            return;
        }

        Player player = event.getPlayer();

        /*
         * Ignore BlockBreakEvents created by Blast itself.
         */
        if (blasting.contains(player.getUniqueId())) {
            return;
        }

        /*
         * Permission check.
         */
        if (!player.hasPermission("superenchantments.use")) {
            return;
        }

        ConfigurationSection cfg = config();

        /*
         * Disabled gamemodes.
         */
        if (cfg.getStringList("disabled-gamemodes")
                .contains(player.getGameMode().name())) {
            return;
        }

        /*
         * Disabled worlds.
         */
        if (cfg.getStringList("disabled-worlds")
                .contains(player.getWorld().getName())) {
            return;
        }

        /*
         * Sneaking can disable Blast.
         */
        if (cfg.getBoolean("sneak-disables", true)
                && player.isSneaking()) {
            return;
        }

        /*
         * Get the tool.
         */
        ItemStack tool =
                player.getInventory().getItemInMainHand();

        /*
         * Make sure the enchantment applies to this item.
         */
        if (!appliesTo(tool.getType())) {
            return;
        }

        if (!hasEnchant(tool)) {
            return;
        }

        /*
         * Radius controls DEPTH only.
         *
         * It does NOT control the width/height.
         */
        int radius =
                Math.max(
                        1,
                        cfg.getInt("radius", 1)
                );

        /*
         * This is the block the player actually broke.
         */
        Block origin = event.getBlock();

        /*
         * Determine the direction the player is mining.
         *
         * This is calculated before Blast changes anything.
         */
        BlockFace miningDirection =
                getMiningDirection(player);

        /*
         * Tool damage settings.
         */
        boolean damageEnabled =
                cfg.getBoolean(
                        "tool-damage.enabled",
                        true
                );

        int perBlock =
                Math.max(
                        0,
                        cfg.getInt(
                                "tool-damage.per-block",
                                1
                        )
                );

        boolean unbreaking =
                cfg.getBoolean(
                        "tool-damage.respect-unbreaking",
                        true
                );

        /*
         * Drop settings.
         */
        boolean dropItems =
                cfg.getBoolean(
                        "drop-items",
                        true
                );

        boolean dropXp =
                cfg.getBoolean(
                        "drop-xp",
                        true
                );

        boolean brokeAny = false;

        /*
         * Mark the player as being processed.
         *
         * This prevents the artificial BlockBreakEvents
         * from recursively triggering Blast.
         */
        blasting.add(player.getUniqueId());

        try {

            /*
             * depth = 0
             *
             * This represents the FIRST 3x3 layer,
             * centered around the original block.
             *
             * depth = 1
             * is the first 3x3 layer forward.
             *
             * depth = 2
             * is the second 3x3 layer forward.
             *
             * etc.
             */
            outer:
            for (int depth = 0;
                 depth <= radius;
                 depth++) {

                /*
                 * Fixed 3x3 cross-section.
                 *
                 * These values NEVER use radius.
                 */
                for (int x = -1;
                     x <= 1;
                     x++) {

                    for (int y = -1;
                         y <= 1;
                         y++) {

                        /*
                         * The center of depth 0 is the
                         * block the player already broke.
                         *
                         * Vanilla handles that block, so
                         * we skip it.
                         */
                        if (depth == 0
                                && x == 0
                                && y == 0) {

                            continue;
                        }

                        /*
                         * Calculate the actual target block.
                         */
                        Block target =
                                getBlastBlock(
                                        origin,
                                        miningDirection,
                                        depth,
                                        x,
                                        y
                                );

                        /*
                         * Safety check.
                         */
                        if (target.equals(origin)) {
                            continue;
                        }

                        /*
                         * Check whether Blast is allowed
                         * to break this block.
                         */
                        if (!canBlast(target, cfg)) {
                            continue;
                        }

                        /*
                         * Create a real BlockBreakEvent.
                         *
                         * This allows protection plugins to
                         * cancel individual blocks.
                         */
                        BlockBreakEvent extra =
                                new BlockBreakEvent(
                                        target,
                                        player
                                );

                        Bukkit.getPluginManager()
                                .callEvent(extra);

                        /*
                         * Protection plugin cancelled it.
                         */
                        if (extra.isCancelled()) {
                            continue;
                        }

                        /*
                         * Damage the tool.
                         */
                        if (damageEnabled
                                && perBlock > 0) {

                            if (!damageTool(
                                    tool,
                                    perBlock,
                                    unbreaking
                            )) {

                                /*
                                 * Tool is too damaged.
                                 */
                                break outer;
                            }
                        }

                        /*
                         * Save XP before breaking.
                         */
                        int exp =
                                extra.getExpToDrop();

                        Location center =
                                target.getLocation()
                                        .add(
                                                0.5,
                                                0.5,
                                                0.5
                                        );

                        /*
                         * Break the block.
                         */
                        if (dropItems
                                && extra.isDropItems()) {

                            target.breakNaturally(tool);

                        } else {

                            target.setType(
                                    Material.AIR
                            );
                        }

                        /*
                         * Drop XP.
                         */
                        if (dropXp
                                && exp > 0) {

                            target.getWorld()
                                    .spawn(
                                            center,
                                            ExperienceOrb.class,
                                            orb -> orb.setExperience(exp)
                                    );
                        }

                        brokeAny = true;
                    }
                }
            }

        } finally {

            /*
             * Always remove the player from the
             * active Blast set.
             */
            blasting.remove(
                    player.getUniqueId()
            );
        }

        /*
         * Update the player's tool after Blast.
         */
        if (brokeAny) {

            player.getInventory()
                    .setItemInMainHand(tool);

            playEffects(
                    origin,
                    cfg
            );
        }
    }

    /**
     * Gets a block inside the Blast area.
     *
     * The cross-section is ALWAYS 3x3.
     *
     * depth controls how far forward the layer is.
     */
    private Block getBlastBlock(
            Block origin,
            BlockFace direction,
            int depth,
            int x,
            int y
    ) {

        /*
         * EAST / WEST
         *
         * Forward direction = X
         *
         * 3x3 plane = Y/Z
         */
        if (direction == BlockFace.EAST
                || direction == BlockFace.WEST) {

            return origin.getRelative(
                    direction.getModX() * depth,
                    y,
                    x
            );
        }

        /*
         * UP / DOWN
         *
         * Forward direction = Y
         *
         * 3x3 plane = X/Z
         */
        if (direction == BlockFace.UP
                || direction == BlockFace.DOWN) {

            return origin.getRelative(
                    x,
                    direction.getModY() * depth,
                    y
            );
        }

        /*
         * NORTH / SOUTH
         *
         * Forward direction = Z
         *
         * 3x3 plane = X/Y
         */
        return origin.getRelative(
                x,
                y,
                direction.getModZ() * depth
        );
    }

    /**
     * Determines which direction the player is mining.
     *
     * This uses the player's eye direction rather than
     * relying on a ray trace hitting the block.
     *
     * This makes the first block reliable.
     */
    private BlockFace getMiningDirection(
            Player player
    ) {

        Vector direction =
                player.getEyeLocation()
                        .getDirection()
                        .normalize();

        double x =
                Math.abs(direction.getX());

        double y =
                Math.abs(direction.getY());

        double z =
                Math.abs(direction.getZ());

        /*
         * Vertical.
         */
        if (y > x && y > z) {

            return direction.getY() > 0
                    ? BlockFace.UP
                    : BlockFace.DOWN;
        }

        /*
         * X axis.
         */
        if (x > z) {

            return direction.getX() > 0
                    ? BlockFace.EAST
                    : BlockFace.WEST;
        }

        /*
         * Z axis.
         */
        return direction.getZ() > 0
                ? BlockFace.SOUTH
                : BlockFace.NORTH;
    }

    /**
     * Checks whether a block can be broken by Blast.
     */
    private boolean canBlast(
            Block block,
            ConfigurationSection cfg
    ) {

        Material type =
                block.getType();

        /*
         * Air.
         */
        if (type.isAir()) {
            return false;
        }

        /*
         * Liquids.
         */
        if (block.isLiquid()) {
            return false;
        }

        /*
         * Unbreakable blocks.
         */
        if (type.getHardness() < 0) {
            return false;
        }

        /*
         * Configured blocked blocks.
         */
        if (cfg.getStringList(
                "blocked-blocks"
        ).contains(type.name())) {

            return false;
        }

        /*
         * Containers.
         */
        if (cfg.getBoolean(
                "skip-containers",
                true
        )
                && block.getState()
                instanceof Container) {

            return false;
        }

        return true;
    }

    /**
     * Applies durability damage to the tool.
     *
     * Returns false if the tool is too close
     * to breaking.
     */
    private boolean damageTool(
            ItemStack tool,
            int perBlock,
            boolean respectUnbreaking
    ) {

        int maxDurability =
                tool.getType()
                        .getMaxDurability();

        /*
         * Items without durability.
         */
        if (maxDurability <= 0) {
            return true;
        }

        ItemMeta meta =
                tool.getItemMeta();

        if (!(meta instanceof Damageable)) {
            return true;
        }

        Damageable damageable =
                (Damageable) meta;

        int amount = perBlock;

        /*
         * Respect Unbreaking.
         */
        if (respectUnbreaking) {

            Enchantment unbreakingEnchant =
                    Enchantment.getByKey(
                            NamespacedKey.minecraft(
                                    "unbreaking"
                            )
                    );

            int unbreaking =
                    unbreakingEnchant == null
                            ? 0
                            : tool.getEnchantmentLevel(
                            unbreakingEnchant
                    );

            if (unbreaking > 0) {

                int actual = 0;

                for (int i = 0;
                     i < perBlock;
                     i++) {

                    if (ThreadLocalRandom
                            .current()
                            .nextInt(
                                    unbreaking + 1
                            ) == 0) {

                        actual++;
                    }
                }

                amount = actual;
            }
        }

        /*
         * Leave enough durability for the
         * original vanilla block break.
         */
        if (damageable.getDamage()
                + amount
                >= maxDurability - 1) {

            return false;
        }

        damageable.setDamage(
                damageable.getDamage()
                        + amount
        );

        tool.setItemMeta(meta);

        return true;
    }

    /**
     * Plays Blast effects.
     */
    private void playEffects(
            Block origin,
            ConfigurationSection cfg
    ) {

        Location loc =
                origin.getLocation()
                        .add(
                                0.5,
                                0.5,
                                0.5
                        );

        /*
         * Particle.
         */
        String particleName =
                cfg.getString(
                        "effects.particle",
                        "NONE"
                );

        if (particleName != null
                && !particleName
                .equalsIgnoreCase("NONE")) {

            try {

                Particle particle =
                        Particle.valueOf(
                                particleName
                                        .toUpperCase()
                        );

                origin.getWorld()
                        .spawnParticle(
                                particle,
                                loc,
                                Math.max(
                                        1,
                                        cfg.getInt(
                                                "effects.particle-count",
                                                5
                                        )
                                ),
                                0.4,
                                0.4,
                                0.4,
                                0.02
                        );

            } catch (IllegalArgumentException ignored) {
                /*
                 * Particle doesn't exist on this version.
                 */
            }
        }

        /*
         * Sound.
         */
        String sound =
                cfg.getString(
                        "effects.sound",
                        "NONE"
                );

        if (sound != null
                && !sound.equalsIgnoreCase("NONE")) {

            origin.getWorld()
                    .playSound(
                            loc,
                            sound,
                            (float) cfg.getDouble(
                                    "effects.sound-volume",
                                    0.4
                            ),
                            (float) cfg.getDouble(
                                    "effects.sound-pitch",
                                    1.2
                            )
                    );
        }
    }
}