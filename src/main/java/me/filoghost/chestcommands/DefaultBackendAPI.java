/*
 * Copyright (C) filoghost and contributors
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package me.filoghost.chestcommands;

import me.filoghost.chestcommands.api.ConfigurableIcon;
import me.filoghost.chestcommands.api.Menu;
import me.filoghost.chestcommands.api.PlaceholderReplacer;
import me.filoghost.chestcommands.api.StaticIcon;
import me.filoghost.chestcommands.api.internal.BackendAPI;
import me.filoghost.chestcommands.icon.APIConfigurableIcon;
import me.filoghost.chestcommands.icon.APIStaticIcon;
import me.filoghost.chestcommands.menu.APIMenu;
import me.filoghost.chestcommands.menu.InternalMenu;
import me.filoghost.chestcommands.menu.MenuManager;
import me.filoghost.chestcommands.placeholder.PlaceholderManager;
import me.filoghost.fcommons.Preconditions;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

import org.jetbrains.annotations.NotNull;

public class DefaultBackendAPI extends BackendAPI {

    @Override
    public boolean isInternalMenuLoaded(@NotNull String menuFileName) {
        Preconditions.notNull(menuFileName, "menuFileName");

        return MenuManager.getMenuByFileName(menuFileName) != null;
    }

    @Override
    public boolean openInternalMenu(@NotNull ServerPlayerEntity player, @NotNull String menuFileName) {
        Preconditions.notNull(player, "player");
        Preconditions.notNull(menuFileName, "menuFileName");

        InternalMenu menu = MenuManager.getMenuByFileName(menuFileName);

        if (menu != null) {
            menu.open(player);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public @NotNull ConfigurableIcon createConfigurableIcon(@NotNull Item material) {
        return new APIConfigurableIcon(material);
    }

    @Override
    public @NotNull Menu createMenu(@NotNull String title, int rows) {
        return new APIMenu(title, rows);
    }

    @Override
    public @NotNull StaticIcon createStaticIcon(@NotNull ItemStack itemStack) {
        return new APIStaticIcon(itemStack);
    }

    @Override
    public void registerPlaceholder(@NotNull String identifier,
                                    @NotNull PlaceholderReplacer placeholderReplacer) {
        PlaceholderManager.registerPluginPlaceholder(identifier, placeholderReplacer);
    }

    @Override
    public boolean unregisterPlaceholder(@NotNull String identifier) {
        return PlaceholderManager.unregisterPluginPlaceholder(identifier);
    }

}
