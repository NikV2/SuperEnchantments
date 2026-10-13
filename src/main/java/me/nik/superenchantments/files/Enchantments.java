package me.nik.superenchantments.files;

import me.nik.superenchantments.SuperEnchantments;
import me.nik.superenchantments.files.commentedfiles.CommentedFileConfiguration;

import java.io.File;

public class Enchantments {

    private static final String[] HEADER = new String[]{
            "+----------------------------------------------------------------------------------------------+",
            "|                                                                                              |",
            "|                                        SuperEnchantments                                     |",
            "|                                                                                              |",
            "|                                       Enchantment Settings                                   |",
            "|                                                                                              |",
            "+----------------------------------------------------------------------------------------------+"
    };

    private final SuperEnchantments plugin;
    private CommentedFileConfiguration configuration;

    public Enchantments(SuperEnchantments plugin) {
        this.plugin = plugin;
    }

    public void setup() {

        File file = new File(plugin.getDataFolder(), "enchantments.yml");

        boolean exists = file.exists();

        configuration = CommentedFileConfiguration.loadConfiguration(plugin, file);

        if (!exists) configuration.addComments(HEADER);
    }

    public CommentedFileConfiguration getConfig() {
        return configuration;
    }

    public void save() {
        configuration.save();
    }
}
