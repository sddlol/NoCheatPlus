/*
 * This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 */
package fr.neatmonster.nocheatplus.command.admin;

import java.io.File;
import java.util.Arrays;
import java.util.Locale;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import fr.neatmonster.nocheatplus.command.BaseCommand;
import fr.neatmonster.nocheatplus.config.ConfPaths;
import fr.neatmonster.nocheatplus.config.ConfigFile;
import fr.neatmonster.nocheatplus.config.ConfigManager;
import fr.neatmonster.nocheatplus.permissions.Permissions;

/**
 * First-start setup wizard command.
 *
 * Usage:
 * /ncp setup
 * /ncp setup status
 * /ncp setup <survival|pvp|minigame|anarchy>
 * /ncp setup reset
 */
public class SetupCommand extends BaseCommand {

    private static final String[] PROFILES = new String[]{"survival", "pvp", "minigame", "anarchy"};

    public SetupCommand(final JavaPlugin plugin) {
        super(plugin, "setup", Permissions.COMMAND_SETUP, new String[]{"wizard", "profile"});
        this.usage = TAG + "Usage: /ncp setup <survival|pvp|minigame|anarchy|status|reset>";
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (args.length == 1 || (args.length == 2 && "status".equalsIgnoreCase(args[1]))) {
            sendStatus(sender);
            return true;
        }
        if (args.length != 2) {
            return false;
        }

        final String choice = args[1].trim().toLowerCase(Locale.ROOT);
        if ("reset".equals(choice)) {
            final ConfigFile config = ConfigManager.getConfigFile();
            config.set(ConfPaths.SETUP_COMPLETED, false);
            config.set(ConfPaths.SETUP_PROFILE, "unconfigured");
            if (!saveGlobalConfig(sender, config)) {
                return true;
            }
            sender.sendMessage(TAG + ChatColor.YELLOW + "Setup state reset. Run /ncp setup <profile> and then reload.");
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "ncp reload");
            return true;
        }

        if (!Arrays.asList(PROFILES).contains(choice)) {
            sender.sendMessage(TAG + ChatColor.RED + "Unknown profile: " + choice);
            sender.sendMessage(TAG + ChatColor.GRAY + "Available: " + ChatColor.WHITE + "survival, pvp, minigame, anarchy");
            return true;
        }

        final ConfigFile config = ConfigManager.getConfigFile();
        applyProfile(config, choice);
        config.set(ConfPaths.SETUP_COMPLETED, true);
        config.set(ConfPaths.SETUP_PROFILE, choice);

        if (!saveGlobalConfig(sender, config)) {
            return true;
        }

        sender.sendMessage(TAG + ChatColor.GREEN + "Applied setup profile: " + choice + ChatColor.GRAY + ". Reloading configuration...");
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "ncp reload");
        return true;
    }

    private void sendStatus(final CommandSender sender) {
        final ConfigFile config = ConfigManager.getConfigFile();
        final boolean completed = config.getBoolean(ConfPaths.SETUP_COMPLETED, false);
        final String profile = config.getString(ConfPaths.SETUP_PROFILE, "unconfigured");
        sender.sendMessage(TAG + ChatColor.GRAY + "Setup completed: " + (completed ? ChatColor.GREEN + "true" : ChatColor.RED + "false"));
        sender.sendMessage(TAG + ChatColor.GRAY + "Selected profile: " + ChatColor.YELLOW + profile);
        sender.sendMessage(TAG + ChatColor.GRAY + "Available profiles: " + ChatColor.WHITE + "survival, pvp, minigame, anarchy");
    }

    private boolean saveGlobalConfig(final CommandSender sender, final ConfigFile config) {
        try {
            final File target = new File(access.getDataFolder(), "config.yml");
            config.save(target);
            return true;
        } catch (final Exception ex) {
            sender.sendMessage(TAG + ChatColor.RED + "Failed to save config.yml: " + ex.getClass().getSimpleName());
            return false;
        }
    }

    private void applyProfile(final ConfigFile config, final String profile) {
        // Common baseline.
        config.set(ConfPaths.COMBINED_EVIDENCE_DEBUG_ACTIVE, true);
        config.set(ConfPaths.COMBINED_EVIDENCE_DEBUG_SNAPSHOT_ACTIVE, true);

        if ("survival".equals(profile)) {
            config.set(ConfPaths.COMBINED_EVIDENCE_PROFILE, "balanced");
            config.set(ConfPaths.COMBINED_EVIDENCE_OVERRIDES_BLOCKPLACE_SCAFFOLD, "strict");
            config.set(ConfPaths.COMBINED_EVIDENCE_OVERRIDES_NET_WRONGTURN, "strict");
            config.set(ConfPaths.COMBINED_EVIDENCE_DEBUG_MININTERVALMS, 2500);
            config.set(ConfPaths.COMBINED_EVIDENCE_GUARDRAILS_ENABLED, true);
            config.set(ConfPaths.COMBINED_EVIDENCE_GUARDRAILS_MIN_REPEAT_WINDOW_MS, 450);
            config.set(ConfPaths.COMBINED_EVIDENCE_GUARDRAILS_REQUIRE_REPEAT_FOR_STAGE3, true);
            return;
        }

        if ("pvp".equals(profile)) {
            config.set(ConfPaths.COMBINED_EVIDENCE_PROFILE, "strict");
            config.set(ConfPaths.COMBINED_EVIDENCE_OVERRIDES_MOVING_TIMER, "balanced");
            config.set(ConfPaths.COMBINED_EVIDENCE_OVERRIDES_NET_KEEPALIVEFREQUENCY, "balanced");
            config.set(ConfPaths.COMBINED_EVIDENCE_DEBUG_MININTERVALMS, 1500);
            config.set(ConfPaths.COMBINED_EVIDENCE_GUARDRAILS_ENABLED, true);
            config.set(ConfPaths.COMBINED_EVIDENCE_GUARDRAILS_MIN_REPEAT_WINDOW_MS, 300);
            config.set(ConfPaths.COMBINED_EVIDENCE_GUARDRAILS_REQUIRE_REPEAT_FOR_STAGE3, true);
            return;
        }

        if ("minigame".equals(profile)) {
            config.set(ConfPaths.COMBINED_EVIDENCE_PROFILE, "strict");
            config.set(ConfPaths.COMBINED_EVIDENCE_OVERRIDES_MOVING_TIMER, "balanced");
            config.set(ConfPaths.COMBINED_EVIDENCE_OVERRIDES_MOVING_VELOCITY, "strict");
            config.set(ConfPaths.COMBINED_EVIDENCE_OVERRIDES_NET_PACKETFREQUENCY, "strict");
            config.set(ConfPaths.COMBINED_EVIDENCE_DEBUG_MININTERVALMS, 1200);
            config.set(ConfPaths.COMBINED_EVIDENCE_GUARDRAILS_ENABLED, true);
            config.set(ConfPaths.COMBINED_EVIDENCE_GUARDRAILS_MIN_REPEAT_WINDOW_MS, 280);
            config.set(ConfPaths.COMBINED_EVIDENCE_GUARDRAILS_REQUIRE_REPEAT_FOR_STAGE3, true);
            return;
        }

        // anarchy
        config.set(ConfPaths.COMBINED_EVIDENCE_PROFILE, "strict");
        config.set(ConfPaths.COMBINED_EVIDENCE_OVERRIDES_NET_WRONGTURN, "strict");
        config.set(ConfPaths.COMBINED_EVIDENCE_DEBUG_MININTERVALMS, 1000);
        config.set(ConfPaths.COMBINED_EVIDENCE_GUARDRAILS_ENABLED, true);
        config.set(ConfPaths.COMBINED_EVIDENCE_GUARDRAILS_MIN_REPEAT_WINDOW_MS, 200);
        config.set(ConfPaths.COMBINED_EVIDENCE_GUARDRAILS_REQUIRE_REPEAT_FOR_STAGE3, false);
    }
}
