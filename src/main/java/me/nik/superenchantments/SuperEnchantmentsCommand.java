package me.nik.superenchantments;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class SuperEnchantmentsCommand implements CommandExecutor, TabCompleter {

    private static final List<String> SUBCOMMANDS = List.of("list", "enchant", "remove", "book", "reload");

    private final SuperEnchantments plugin;

    public SuperEnchantmentsCommand(SuperEnchantments plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("superenchantments.admin")) {
            plugin.send(sender, "no-permission");
            return true;
        }
        if (args.length == 0) {
            plugin.send(sender, "usage");
            return true;
        }

        EnchantManager manager = plugin.getEnchantManager();

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "reload" -> {
                plugin.reloadConfig();
                plugin.send(sender, "reloaded");
            }

            case "list" -> {
                plugin.send(sender, "list-header");
                for (CustomEnchant e : manager.getAll()) {
                    plugin.send(sender, "list-entry",
                            "enchant", e.getDisplayName(),
                            "state", e.isEnabled() ? "&aenabled" : "&cdisabled");
                }
            }

            case "enchant" -> {
                if (!(sender instanceof Player player)) {
                    plugin.send(sender, "player-only");
                    return true;
                }
                CustomEnchant enchant = resolveEnchant(sender, args);
                if (enchant == null) return true;

                ItemStack held = player.getInventory().getItemInMainHand();
                if (held.getType() == Material.AIR) {
                    plugin.send(sender, "hold-item");
                    return true;
                }
                if (!enchant.appliesTo(held.getType())) {
                    plugin.send(sender, "cannot-apply", "enchant", enchant.getDisplayName());
                    return true;
                }
                player.getInventory().setItemInMainHand(manager.apply(held, enchant));
                plugin.send(sender, "applied", "enchant", enchant.getDisplayName());
            }

            case "remove" -> {
                if (!(sender instanceof Player player)) {
                    plugin.send(sender, "player-only");
                    return true;
                }
                CustomEnchant enchant = resolveEnchant(sender, args);
                if (enchant == null) return true;

                ItemStack held = player.getInventory().getItemInMainHand();
                if (held.getType() == Material.AIR) {
                    plugin.send(sender, "hold-item");
                    return true;
                }
                if (!manager.has(held, enchant)) {
                    plugin.send(sender, "not-present", "enchant", enchant.getDisplayName());
                    return true;
                }
                player.getInventory().setItemInMainHand(manager.remove(held, enchant));
                plugin.send(sender, "removed", "enchant", enchant.getDisplayName());
            }

            case "book" -> {
                CustomEnchant enchant = resolveEnchant(sender, args);
                if (enchant == null) return true;

                Player target;
                if (args.length > 2) {
                    target = Bukkit.getPlayerExact(args[2]);
                    if (target == null) {
                        plugin.send(sender, "player-not-found", "player", args[2]);
                        return true;
                    }
                } else if (sender instanceof Player self) {
                    target = self;
                } else {
                    plugin.send(sender, "player-only");
                    return true;
                }

                target.getInventory().addItem(manager.createBook(enchant))
                        .values().forEach(drop -> target.getWorld().dropItem(target.getLocation(), drop));
                plugin.send(sender, "book-given",
                        "player", target.getName(),
                        "enchant", enchant.getDisplayName());
            }

            default -> plugin.send(sender, "usage");
        }
        return true;
    }

    /** Looks up args[1] as an enchant, messaging the sender on failure. */
    private CustomEnchant resolveEnchant(CommandSender sender, String[] args) {
        if (args.length < 2) {
            plugin.send(sender, "usage");
            return null;
        }
        CustomEnchant enchant = plugin.getEnchantManager().get(args[1]);
        if (enchant == null) {
            plugin.send(sender, "unknown-enchant", "enchant", args[1]);
            return null;
        }
        if (!enchant.isEnabled()) {
            plugin.send(sender, "enchant-disabled", "enchant", enchant.getDisplayName());
            return null;
        }
        return enchant;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        List<String> out = new ArrayList<>();
        if (!sender.hasPermission("superenchantments.admin")) return out;

        if (args.length == 1) {
            for (String s : SUBCOMMANDS) {
                if (s.startsWith(args[0].toLowerCase(Locale.ROOT))) out.add(s);
            }
            return out;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        boolean takesEnchant = sub.equals("enchant") || sub.equals("remove") || sub.equals("book");

        if (args.length == 2 && takesEnchant) {
            for (CustomEnchant e : plugin.getEnchantManager().getAll()) {
                if (e.getId().startsWith(args[1].toLowerCase(Locale.ROOT))) out.add(e.getId());
            }
        } else if (args.length == 3 && sub.equals("book")) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getName().toLowerCase(Locale.ROOT).startsWith(args[2].toLowerCase(Locale.ROOT))) {
                    out.add(p.getName());
                }
            }
        }
        return out;
    }
}
