package me.nik.superenchantments.commands.subcommands;

import me.nik.superenchantments.SuperEnchantments;
import me.nik.superenchantments.commands.SubCommand;
import me.nik.superenchantments.enums.MsgType;
import me.nik.superenchantments.enums.Permissions;
import me.nik.superenchantments.files.Config;
import me.nik.superenchantments.gui.PlayerMenu;
import me.nik.superenchantments.gui.menus.EnchanterGUI;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class EnchanterCommand extends SubCommand {

    private final SuperEnchantments plugin;

    public EnchanterCommand(SuperEnchantments plugin) {
        this.plugin = plugin;
    }

    @Override
    protected String getName() {
        return "enchanter";
    }

    @Override
    protected String getDescription() {
        return "Spend experience on a random enchant book";
    }

    @Override
    protected String getSyntax() {
        return "/superenchantments enchanter";
    }

    @Override
    protected String getPermission() {
        return Permissions.ENCHANTER.getPermission();
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

        if (!Config.Setting.ENCHANTER_ENABLED.getBoolean()) {
            sender.sendMessage(MsgType.ENCHANTER_DISABLED.getMessage());
            return;
        }

        new EnchanterGUI(new PlayerMenu((Player) sender), plugin).open();
    }

    @Override
    protected List<String> getSubcommandArguments(CommandSender sender, String[] args) {
        return null;
    }
}