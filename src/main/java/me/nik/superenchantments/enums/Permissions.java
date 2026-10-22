package me.nik.superenchantments.enums;

public enum Permissions {
    ADMIN("superenchantments.admin"),
    MENU("superenchantments.menu"),
    ENCHANTER("superenchantments.enchanter"),
    DISENCHANT("superenchantments.disenchant");

    private final String permission;

    Permissions(String permission) {
        this.permission = permission;
    }

    public String getPermission() {
        return permission;
    }
}
