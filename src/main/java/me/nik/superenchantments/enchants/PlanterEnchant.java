package me.nik.superenchantments.enchants;

import me.nik.superenchantments.SuperEnchantments;
import me.nik.superenchantments.enchants.categories.CustomEnchant;
import me.nik.superenchantments.enums.Rarity;
import me.nik.superenchantments.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PlanterEnchant extends CustomEnchant {

    private static final String[][] CROPS = {
            {"WHEAT_SEEDS", "WHEAT"},
            {"CARROT", "CARROTS"},
            {"POTATO", "POTATOES"},
            {"BEETROOT_SEEDS", "BEETROOTS"},
            {"MELON_SEEDS", "MELON_STEM"},
            {"PUMPKIN_SEEDS", "PUMPKIN_STEM"},
            {"NETHER_WART", "NETHER_WART"},
            {"TORCHFLOWER_SEEDS", "TORCHFLOWER_CROP"},
            {"PITCHER_POD", "PITCHER_CROP"}
    };

    private int radius;
    private float soundVolume;
    private String sound;
    private Map<Material, Material> plantable = new EnumMap<>(Material.class);
    private Set<String> disabledWorlds = new HashSet<>();
    private Set<GameMode> disabledGameModes = EnumSet.noneOf(GameMode.class);

    public PlanterEnchant(SuperEnchantments plugin) {
        super(plugin, "planter", "Planter", "&a", Rarity.COMMON, "HOE");

        setDescription("&7Right-click farmland to plant seeds", "&7from your inventory in a whole area");

        addOption("radius", 1, "How far around the clicked block seeds are planted", "1 is a 3x3 area, 2 is 5x5");
        addOption("seeds", Arrays.asList("WHEAT_SEEDS", "CARROT", "POTATO", "BEETROOT_SEEDS"), "The items that can be planted from your inventory", "Also supported: MELON_SEEDS, PUMPKIN_SEEDS, NETHER_WART (on soul sand), TORCHFLOWER_SEEDS, PITCHER_POD");
        addOption("disabled_worlds", new ArrayList<String>(), "Planter does not work in these worlds", "Example: [world_nether, creative]");
        addOption("disabled_gamemodes", Arrays.asList("SPECTATOR"), "Planter does not work in these gamemodes", "Seeds are not used up in creative");
        addOption("sound", "item.crop.plant", "NONE to disable");
        addOption("sound_volume", 0.6D, "The volume of the sound");
    }

    @Override
    protected void onLoad() {

        radius = Math.max(1, getInt("radius"));
        soundVolume = (float) getDouble("sound_volume");

        String name = getString("sound");

        sound = name.equalsIgnoreCase("NONE") ? null : name;

        Map<Material, Material> all = new EnumMap<>(Material.class);

        for (String[] pair : CROPS) {

            Material seed = Material.matchMaterial(pair[0]);
            Material crop = Material.matchMaterial(pair[1]);

            if (seed != null && crop != null) all.put(seed, crop);
        }

        plantable = new EnumMap<>(Material.class);

        for (String seedName : getStringList("seeds")) {

            Material seed = Material.matchMaterial(seedName);

            if (seed == null || !all.containsKey(seed)) {
                ChatUtils.log("The Planter seed " + seedName + " can not be planted");
                continue;
            }

            plantable.put(seed, all.get(seed));
        }

        disabledWorlds = new HashSet<>(getStringList("disabled_worlds"));

        disabledGameModes = EnumSet.noneOf(GameMode.class);

        for (String gameMode : getStringList("disabled_gamemodes")) {

            try {
                disabledGameModes.add(GameMode.valueOf(gameMode.toUpperCase()));
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent e) {

        if (!isEnabled() || e.getHand() != EquipmentSlot.HAND || e.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Block clicked = e.getClickedBlock();

        if (clicked.getType() != Material.FARMLAND && clicked.getType() != Material.SOUL_SAND) return;

        ItemStack tool = e.getItem();

        if (tool == null || !appliesTo(tool.getType()) || !hasEnchant(tool)) return;

        Player player = e.getPlayer();

        if (disabledGameModes.contains(player.getGameMode())) return;

        if (!disabledWorlds.isEmpty() && disabledWorlds.contains(player.getWorld().getName())) return;

        PlayerInventory inventory = player.getInventory();

        boolean consume = player.getGameMode() != GameMode.CREATIVE;

        boolean planted = false;

        for (int x = -radius; x <= radius; x++) {

            for (int z = -radius; z <= radius; z++) {

                Block ground = clicked.getRelative(x, 0, z);

                Material groundType = ground.getType();

                if (groundType != Material.FARMLAND && groundType != Material.SOUL_SAND) continue;

                Block above = ground.getRelative(BlockFace.UP);

                if (!above.getType().isAir()) continue;

                int slot = findSeed(inventory, groundType == Material.SOUL_SAND);

                if (slot == -1) continue;

                ItemStack seed = inventory.getItem(slot);

                BlockState replaced = above.getState();

                above.setType(plantable.get(seed.getType()));

                BlockPlaceEvent place = new BlockPlaceEvent(above, replaced, ground, seed, player, true, EquipmentSlot.HAND);

                Bukkit.getPluginManager().callEvent(place);

                if (place.isCancelled() || !place.canBuild()) {
                    replaced.update(true, false);
                    continue;
                }

                if (consume) {

                    if (seed.getAmount() <= 1) {
                        inventory.setItem(slot, null);
                    } else {
                        seed.setAmount(seed.getAmount() - 1);
                        inventory.setItem(slot, seed);
                    }
                }

                planted = true;
            }
        }

        if (planted && sound != null) player.playSound(player.getLocation(), sound, soundVolume, 1.0F);
    }

    private int findSeed(PlayerInventory inventory, boolean soulSand) {

        ItemStack[] contents = inventory.getStorageContents();

        for (int i = 0; i < contents.length; i++) {

            ItemStack item = contents[i];

            if (item == null) continue;

            Material crop = plantable.get(item.getType());

            if (crop == null) continue;

            if ((crop == Material.NETHER_WART) == soulSand) return i;
        }

        return -1;
    }
}