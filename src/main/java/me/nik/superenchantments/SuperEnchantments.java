package me.nik.superenchantments;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public final class SuperEnchantments extends JavaPlugin {

    private static SuperEnchantments instance;
    private EnchantManager enchantManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        this.enchantManager = new EnchantManager(this);

        // ---- Register enchants here. Add new ones below Blast. ----
        enchantManager.register(new BlastEnchant(this));
        // -----------------------------------------------------------

        getServer().getPluginManager().registerEvents(new AnvilListener(this), this);

        SuperEnchantmentsCommand command = new SuperEnchantmentsCommand(this);
        getCommand("superenchantments").setExecutor(command);
        getCommand("superenchantments").setTabCompleter(command);
    }

    public static SuperEnchantments getInstance() {
        return instance;
    }

    public EnchantManager getEnchantManager() {
        return enchantManager;
    }

    /** Looks up messages.<key>, applies the prefix and {placeholders}, and sends it. */
    public void send(CommandSender to, String key, Object... replacements) {
        String raw = getConfig().getString("messages." + key, key);
        for (int i = 0; i + 1 < replacements.length; i += 2) {
            raw = raw.replace("{" + replacements[i] + "}", String.valueOf(replacements[i + 1]));
        }
        to.sendMessage(Text.color(getConfig().getString("messages.prefix", "") + raw));
    }
}
