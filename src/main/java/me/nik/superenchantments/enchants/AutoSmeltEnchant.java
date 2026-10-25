package me.nik.superenchantments.enchants;

import me.nik.superenchantments.SuperEnchantments;
import me.nik.superenchantments.enchants.categories.CustomEnchant;
import me.nik.superenchantments.enums.Rarity;
import me.nik.superenchantments.utils.MiscUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class AutoSmeltEnchant extends CustomEnchant {

    private final Map<Material, Smelting> recipes = new HashMap<>();

    private boolean recipesLoaded;
    private boolean skipSilkTouch;
    private boolean giveXp;
    private int particleCount;
    private Particle particle;
    private Enchantment silkTouch;
    private Set<Material> blocks = EnumSet.noneOf(Material.class);

    public AutoSmeltEnchant(SuperEnchantments plugin) {
        super(plugin, "autosmelt", "Auto Smelt", "&a", Rarity.RARE, "PICKAXE");

        setDescription("&7Ores you mine drop their", "&7smelted version");

        addOption("blocks", Arrays.asList(
                "IRON_ORE",
                "GOLD_ORE",
                "COPPER_ORE",
                "DEEPSLATE_IRON_ORE",
                "DEEPSLATE_GOLD_ORE",
                "DEEPSLATE_COPPER_ORE",
                "ANCIENT_DEBRIS"
        ), "The blocks whose drops get smelted", "The smelted result is the one from the furnace recipe of the drop");
        addOption("skip_silk_touch", true, "Should tools with Silk Touch keep the ore as it is?");
        addOption("give_xp", true, "Should the player get the experience that smelting gives?");
        addOption("particle", "FLAME", "NONE to disable", "Particle names depend on the server version");
        addOption("particle_count", 6, "The amount of particles");
    }

    @Override
    protected void onLoad() {

        skipSilkTouch = getBoolean("skip_silk_touch");
        giveXp = getBoolean("give_xp");
        particle = MiscUtils.getParticle(getString("particle"));
        particleCount = Math.max(1, getInt("particle_count"));
        silkTouch = Enchantment.getByKey(NamespacedKey.minecraft("silk_touch"));

        blocks = EnumSet.noneOf(Material.class);

        for (String name : getStringList("blocks")) {

            Material material = Material.matchMaterial(name);

            if (material != null) blocks.add(material);
        }

        recipes.clear();
        recipesLoaded = false;
    }

    public static AutoSmeltEnchant get(SuperEnchantments plugin, ItemStack tool) {

        CustomEnchant enchant = plugin.getEnchantManager().get("autosmelt");

        if (!(enchant instanceof AutoSmeltEnchant) || !enchant.isEnabled()) return null;

        if (!enchant.appliesTo(tool.getType()) || !enchant.hasEnchant(tool)) return null;

        return (AutoSmeltEnchant) enchant;
    }

    public boolean applies(Material block, ItemStack tool) {

        if (!blocks.contains(block)) return false;

        return !(skipSilkTouch && silkTouch != null && tool.containsEnchantment(silkTouch));
    }

    public ItemStack smelt(Player player, ItemStack drop) {

        Smelting smelting = getSmelting(drop.getType());

        if (smelting == null) return drop;

        ItemStack result = smelting.result.clone();

        result.setAmount(smelting.result.getAmount() * drop.getAmount());

        if (giveXp) giveExperience(player, smelting.experience * drop.getAmount());

        return result;
    }

    private void giveExperience(Player player, float experience) {

        int points = (int) experience;

        if (ThreadLocalRandom.current().nextFloat() < experience - points) points++;

        if (points > 0) player.giveExp(points);
    }

    private Smelting getSmelting(Material material) {

        if (!recipesLoaded) loadRecipes();

        return recipes.get(material);
    }

    private void loadRecipes() {

        recipesLoaded = true;

        Iterator<Recipe> iterator = Bukkit.recipeIterator();

        while (iterator.hasNext()) {

            Recipe recipe = iterator.next();

            if (!(recipe instanceof FurnaceRecipe)) continue;

            FurnaceRecipe furnace = (FurnaceRecipe) recipe;

            Smelting smelting = new Smelting(furnace.getResult(), furnace.getExperience());

            RecipeChoice choice = furnace.getInputChoice();

            if (choice instanceof RecipeChoice.MaterialChoice) {

                for (Material material : ((RecipeChoice.MaterialChoice) choice).getChoices()) recipes.putIfAbsent(material, smelting);

            } else {
                recipes.putIfAbsent(furnace.getInput().getType(), smelting);
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onDrop(BlockDropItemEvent e) {

        if (!isEnabled()) return;

        Player player = e.getPlayer();

        ItemStack tool = player.getInventory().getItemInMainHand();

        if (!appliesTo(tool.getType()) || !hasEnchant(tool) || !applies(e.getBlockState().getType(), tool)) return;

        boolean smelted = false;

        for (Item item : e.getItems()) {

            ItemStack drop = item.getItemStack();

            ItemStack result = smelt(player, drop);

            if (result == drop) continue;

            item.setItemStack(result);

            smelted = true;
        }

        if (!smelted || particle == null) return;

        Location location = e.getBlock().getLocation().add(0.5, 0.5, 0.5);

        e.getBlock().getWorld().spawnParticle(particle, location, particleCount, 0.3, 0.3, 0.3, 0.02);
    }

    private static class Smelting {

        private final ItemStack result;
        private final float experience;

        private Smelting(ItemStack result, float experience) {
            this.result = result;
            this.experience = experience;
        }
    }
}