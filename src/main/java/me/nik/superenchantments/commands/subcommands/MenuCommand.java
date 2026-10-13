package me.nik.superenchantments.commands.subcommands;

import me.nik.superenchantments.SuperEnchantments;
import me.nik.superenchantments.commands.SubCommand;
import me.nik.superenchantments.enums.Permissions;
import me.nik.superenchantments.gui.PlayerMenu;
import me.nik.superenchantments.gui.menus.EnchantsGUI;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class MenuCommand extends SubCommand {

    private final SuperEnchantments plugin;

    public MenuCommand(SuperEnchantments plugin) {
        this.plugin = plugin;
    }

    @Override
    protected String getName() {
        return "menu";
    }

    @Override
    protected String getDescription() {
        return "Open the enchantments menu";
    }

    @Override
    protected String getSyntax() {
        return "/superenchantments menu";
    }

    @Override
    protected String getPermission() {
        return Permissions.MENU.getPermission();
    }

    @Override
    protected int maxArguments() {
        return 1;
    }

    @Override
    protected boolean canConsoleExecute() {
        return false;
    }

    @Override
    protected void perform(CommandSender sender, String[] args) {
        new EnchantsGUI(new PlayerMenu((Player) sender), plugin).open();
    }

    @Override
    protected List<String> getSubcommandArguments(CommandSender sender, String[] args) {
        return null;
    }
}
