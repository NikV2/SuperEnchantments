package me.nik.superenchantments.enums;

import me.nik.superenchantments.SuperEnchantments;
import me.nik.superenchantments.utils.ChatUtils;

public enum MsgType {
    PREFIX(ChatUtils.format(SuperEnchantments.getInstance().getLang().get().getString("prefix"))),
    NO_PERMISSION(PREFIX.getMessage() + ChatUtils.format(SuperEnchantments.getInstance().getLang().get().getString("no_perm"))),
    RELOADED(PREFIX.getMessage() + ChatUtils.format(SuperEnchantments.getInstance().getLang().get().getString("reloaded"))),
    CONSOLE_COMMANDS(PREFIX.getMessage() + ChatUtils.format(SuperEnchantments.getInstance().getLang().get().getString("console_commands"))),
    PLAYER_NOT_FOUND(PREFIX.getMessage() + ChatUtils.format(SuperEnchantments.getInstance().getLang().get().getString("player_not_found"))),
    UNKNOWN_ENCHANT(PREFIX.getMessage() + ChatUtils.format(SuperEnchantments.getInstance().getLang().get().getString("unknown_enchant"))),
    ENCHANT_DISABLED(PREFIX.getMessage() + ChatUtils.format(SuperEnchantments.getInstance().getLang().get().getString("enchant_disabled"))),
    HOLD_ITEM(PREFIX.getMessage() + ChatUtils.format(SuperEnchantments.getInstance().getLang().get().getString("hold_item"))),
    CANNOT_APPLY(PREFIX.getMessage() + ChatUtils.format(SuperEnchantments.getInstance().getLang().get().getString("cannot_apply"))),
    APPLIED(PREFIX.getMessage() + ChatUtils.format(SuperEnchantments.getInstance().getLang().get().getString("applied"))),
    REMOVED(PREFIX.getMessage() + ChatUtils.format(SuperEnchantments.getInstance().getLang().get().getString("removed"))),
    NOT_PRESENT(PREFIX.getMessage() + ChatUtils.format(SuperEnchantments.getInstance().getLang().get().getString("not_present"))),
    BOOK_GIVEN(PREFIX.getMessage() + ChatUtils.format(SuperEnchantments.getInstance().getLang().get().getString("book_given"))),
    LIST_HEADER(PREFIX.getMessage() + ChatUtils.format(SuperEnchantments.getInstance().getLang().get().getString("list_header"))),
    ENCHANTER_DISABLED(PREFIX.getMessage() + ChatUtils.format(SuperEnchantments.getInstance().getLang().get().getString("enchanter_disabled"))),
    DISENCHANTER_DISABLED(PREFIX.getMessage() + ChatUtils.format(SuperEnchantments.getInstance().getLang().get().getString("disenchanter_disabled"))),
    NOT_ENOUGH_EXP(PREFIX.getMessage() + ChatUtils.format(SuperEnchantments.getInstance().getLang().get().getString("not_enough_exp"))),
    INVENTORY_FULL(PREFIX.getMessage() + ChatUtils.format(SuperEnchantments.getInstance().getLang().get().getString("inventory_full"))),
    OFFER_EMPTY(PREFIX.getMessage() + ChatUtils.format(SuperEnchantments.getInstance().getLang().get().getString("offer_empty"))),
    ENCHANTER_BOUGHT(PREFIX.getMessage() + ChatUtils.format(SuperEnchantments.getInstance().getLang().get().getString("enchanter_bought"))),
    HOLD_SINGLE_ITEM(PREFIX.getMessage() + ChatUtils.format(SuperEnchantments.getInstance().getLang().get().getString("hold_single_item"))),
    ENCHANT_MISSING(PREFIX.getMessage() + ChatUtils.format(SuperEnchantments.getInstance().getLang().get().getString("enchant_missing"))),
    ENCHANT_REMOVED(PREFIX.getMessage() + ChatUtils.format(SuperEnchantments.getInstance().getLang().get().getString("enchant_removed"))),
    LIST_ENTRY(ChatUtils.format(SuperEnchantments.getInstance().getLang().get().getString("list_entry")));

    private final String message;

    MsgType(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
