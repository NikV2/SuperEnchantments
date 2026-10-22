package me.nik.superenchantments.commands.subcommands;

import me.nik.superenchantments.SuperEnchantments;
import me.nik.superenchantments.commands.SubCommand;
import me.nik.superenchantments.enums.MsgType;
import me.nik.superenchantments.enums.Permissions;
import me.nik.superenchantments.files.Config;
import me.nik.superenchantments.gui.PlayerMenu;
import me.nik.superenchantments.gui.menus.DisenchanterGUI;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class DisenchantCommand extends SubCommand {

    private final SuperEnchantments plugin;

    public DisenchantCommand(SuperEnchantments plugin) {
        this.plugin = plugin;
    }

    @Override
    protected String getName() {
        return "disenchant";
    }

    @Override
    protected String getDescription() {
        return "Remove custom enchants from the item in your hand";
    }

    @Override
    protected String getSyntax() {
        return "/superenchantments disenchant";
    }

    @Override
    protected String getPermission() {
        return Permissions.DISENCHANT.getPermission();
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

        if (!Config.Setting.DISENCHANTER_ENABLED.getBoolean()) {
            sender.sendMessage(MsgType.DISENCHANTER_DISABLED.getMessage());
            return;
        }

        new DisenchanterGUI(new PlayerMenu((Player) sender), plugin).open();
    }

    @Override
    protected List<String> getSubcommandArguments(CommandSender sender, String[] args) {
        return null;
    }
}