package me.nik.superenchantments.enums;

public enum Permissions {
    ADMIN("superenchantments.admin"),
    MENU("superenchantments.menu");

    private final String permission;

    Permissions(String permission) {
        this.permission = permission;
    }

    public String getPermission() {
        return permission;
    }
}
