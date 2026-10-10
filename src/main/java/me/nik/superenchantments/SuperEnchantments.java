package me.nik.superenchantments;

import me.nik.superenchantments.commands.CommandManager;
import me.nik.superenchantments.enchants.BlastEnchant;
import me.nik.superenchantments.files.Config;
import me.nik.superenchantments.files.Lang;
import me.nik.superenchantments.listeners.AnvilListener;
import me.nik.superenchantments.managers.EnchantManager;
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

    private EnchantManager enchantManager;

    private static SuperEnchantments instance;

    @Override
    public void onEnable() {

        this.getServer().getConsoleSender().sendMessage(STARTUP_MESSAGE);

        instance = this;

        MillisTest test = new MillisTest();

        test.reset();
        this.config.setup();
        this.lang.setup(this);
        this.lang.addDefaults();
        this.lang.get().options().copyDefaults(true);
        this.lang.save();
        ChatUtils.log("Files have been loaded in " + test.getMillis() + "ms");

        test.reset();
        this.enchantManager = new EnchantManager(this);
        this.enchantManager.initialize();

        this.enchantManager.register(new BlastEnchant(this));
        ChatUtils.log(this.enchantManager.getAll().size() + " enchants have been loaded in " + test.getMillis() + "ms");

        test.reset();
        getCommand("superenchantments").setExecutor(new CommandManager(this));
        ChatUtils.log("Commands have been loaded in " + test.getMillis() + "ms");

        test.reset();
        Bukkit.getPluginManager().registerEvents(new AnvilListener(this), this);
        ChatUtils.log("Listeners have been loaded in " + test.getMillis() + "ms");
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);

        Bukkit.getScheduler().cancelTasks(this);

        this.enchantManager.shutdown();

        this.config.reset();
        this.lang.reload();
        this.lang.save();

        instance = null;
    }

    public EnchantManager getEnchantManager() {
        return enchantManager;
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
