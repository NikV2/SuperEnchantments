package me.nik.superenchantments.enums;

public enum Permissions {
    ADMIN("superenchantments.admin");

    private final String permission;

    Permissions(String permission) {
        this.permission = permission;
    }

    public String getPermission() {
        return permission;
    }
}
