package me.nik.superenchantments;

import me.nik.superenchantments.commands.CommandManager;
import me.nik.superenchantments.enchants.AegisEnchant;
import me.nik.superenchantments.enchants.BlastEnchant;
import me.nik.superenchantments.enchants.ChargeEnchant;
import me.nik.superenchantments.enchants.CurseEnchant;
import me.nik.superenchantments.enchants.DodgeEnchant;
import me.nik.superenchantments.enchants.ExecutionerEnchant;
import me.nik.superenchantments.enchants.FrostbiteEnchant;
import me.nik.superenchantments.enchants.GlowEnchant;
import me.nik.superenchantments.enchants.JuggernautEnchant;
import me.nik.superenchantments.enchants.LifestealEnchant;
import me.nik.superenchantments.enchants.LumberjackEnchant;
import me.nik.superenchantments.enchants.PenetrationEnchant;
import me.nik.superenchantments.enchants.PiercingSoulEnchant;
import me.nik.superenchantments.enchants.RegenerationEnchant;
import me.nik.superenchantments.enchants.RejuvenationEnchant;
import me.nik.superenchantments.enchants.SmithingEnchant;
import me.nik.superenchantments.enchants.StrikeEnchant;
import me.nik.superenchantments.enchants.SwiftEnchant;
import me.nik.superenchantments.enchants.TelepathyEnchant;
import me.nik.superenchantments.enchants.ThunderEnchant;
import me.nik.superenchantments.enchants.WindburstEnchant;
import me.nik.superenchantments.files.Config;
import me.nik.superenchantments.files.Enchantments;
import me.nik.superenchantments.files.Lang;
import me.nik.superenchantments.listeners.AnvilListener;
import me.nik.superenchantments.listeners.GuiListener;
import me.nik.superenchantments.managers.EnchantManager;
import me.nik.superenchantments.tasks.UpdateTask;
import me.nik.superenchantments.utils.ChatUtils;
import me.nik.superenchantments.utils.MillisTest;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public class SuperEnchantments extends JavaPlugin {

    private final String[] STARTUP_MESSAGE = new String[]{
            " ",
            ChatColor.AQUA + "Super Enchantments v" + this.getDescription().getVersion(),
            " ",
            ChatColor.WHITE + "  Author: Nik",
            " "
    };

    private final Config config = new Config(this);
    private final Lang lang = new Lang();
    private final Enchantments enchantments = new Enchantments(this);
    private final UpdateTask updateTask = new UpdateTask();

    private EnchantManager enchantManager;

    private static SuperEnchantments instance;

    @Override
    public void onEnable() {

        this.getServer().getConsoleSender().sendMessage(STARTUP_MESSAGE);

        instance = this;

        MillisTest test = new MillisTest();

        test.reset();
        this.config.setup();
        this.enchantments.setup();
        this.lang.setup(this);
        this.lang.addDefaults();
        this.lang.get().options().copyDefaults(true);
        this.lang.save();
        ChatUtils.log("Files have been loaded in " + test.getMillis() + "ms");

        test.reset();
        this.enchantManager = new EnchantManager(this);
        this.enchantManager.initialize();

        this.enchantManager.register(
                new BlastEnchant(this),
                new GlowEnchant(this),
                new CurseEnchant(this),
                new SwiftEnchant(this),
                new DodgeEnchant(this),
                new LifestealEnchant(this),
                new StrikeEnchant(this),
                new SmithingEnchant(this),
                new ThunderEnchant(this),
                new RejuvenationEnchant(this),
                new TelepathyEnchant(this),
                new LumberjackEnchant(this),
                new AegisEnchant(this),
                new ChargeEnchant(this),
                new ExecutionerEnchant(this),
                new FrostbiteEnchant(this),
                new JuggernautEnchant(this),
                new PenetrationEnchant(this),
                new PiercingSoulEnchant(this),
                new RegenerationEnchant(this),
                new WindburstEnchant(this)
        );
        ChatUtils.log(this.enchantManager.getAll().size() + " enchants have been loaded in " + test.getMillis() + "ms");

        test.reset();
        this.updateTask.runTaskTimer(this, 20L, 20L);
        ChatUtils.log("Tasks have been loaded in " + test.getMillis() + "ms");

        test.reset();
        getCommand("superenchantments").setExecutor(new CommandManager(this));
        ChatUtils.log("Commands have been loaded in " + test.getMillis() + "ms");

        test.reset();
        Bukkit.getPluginManager().registerEvents(new AnvilListener(this), this);
        Bukkit.getPluginManager().registerEvents(new GuiListener(), this);
        ChatUtils.log("Listeners have been loaded in " + test.getMillis() + "ms");
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);

        Bukkit.getScheduler().cancelTasks(this);

        this.updateTask.clear();
        this.enchantManager.shutdown();

        this.config.reset();
        this.lang.reload();
        this.lang.save();

        instance = null;
    }

    public EnchantManager getEnchantManager() {
        return enchantManager;
    }

    public Enchantments getEnchantments() {
        return enchantments;
    }

    public UpdateTask getUpdateTask() {
        return updateTask;
    }

    public Config getConfiguration() {
        return config;
    }

    public Lang getLang() {
        return lang;
    }

    public static SuperEnchantments getInstance() {
        return instance;
    }
}
