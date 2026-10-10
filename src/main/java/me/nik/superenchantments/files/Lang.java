package me.nik.superenchantments.files;

import me.nik.superenchantments.utils.MiscUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class Lang {
    private File file;
    private FileConfiguration lang;

    public void setup(JavaPlugin plugin) {

        this.file = new File(plugin.getDataFolder(), "lang.yml");

        if (!this.file.exists()) {
            try {
                this.file.createNewFile();
            } catch (IOException ignored) {
            }
        }

        reload();
    }

    public FileConfiguration get() {
        return lang;
    }

    public void save() {
        try {
            this.lang.save(file);
        } catch (IOException ignored) {
        }
    }

    public void reload() {
        this.lang = MiscUtils.loadConfigurationUTF_8(file);
    }

    public void addDefaults() {
        get().addDefault("prefix", "&f&l[&bSuperEnchantments&f&l]&f»&r ");
        get().addDefault("no_perm", "&cYou do not have permission to do that!");
        get().addDefault("reloaded", "&fYou have successfully reloaded the plugin!");
        get().addDefault("console_commands", "&c&lYou cannot run this command through the console!");
        get().addDefault("player_not_found", "&cThat player is not online!");
        get().addDefault("unknown_enchant", "&cUnknown enchant: &f%enchant%");
        get().addDefault("enchant_disabled", "&c%enchant% is disabled in the config.");
        get().addDefault("hold_item", "&cHold an item first.");
        get().addDefault("cannot_apply", "&c%enchant% can't be applied to that item.");
        get().addDefault("applied", "&aApplied &f%enchant% &ato your item.");
        get().addDefault("removed", "&aRemoved &f%enchant% &afrom your item.");
        get().addDefault("not_present", "&cThat item doesn't have %enchant%.");
        get().addDefault("book_given", "&aGave &f%player% &aa &f%enchant% &abook.");
        get().addDefault("list_header", "&bRegistered enchants:");
        get().addDefault("list_entry", "&7- &f%enchant% &7(%state%&7)");
    }
}
