/*
 * This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 */
package fr.neatmonster.nocheatplus.command.admin;

import java.util.Arrays;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import fr.neatmonster.nocheatplus.command.BaseCommand;
import fr.neatmonster.nocheatplus.permissions.Permissions;
import fr.neatmonster.nocheatplus.utilities.StringUtil;

/**
 * This command shows a list of all commands.
 */
public class CommandsCommand extends BaseCommand {

    private final String allCommandsEn;
    private final String allCommandsZh;

    public CommandsCommand(final JavaPlugin plugin) {
        super(plugin, "commands", Permissions.COMMAND_COMMANDS, new String[]{"cmds"});

        final String[] moreCommandsEn = new String[]{
                ChatColor.GOLD + "" + ChatColor.BOLD + "Console commands:",
                ChatColor.GRAY + "" + ChatColor.BOLD + "• " + ChatColor.RED + "" + ChatColor.ITALIC + "/<command> ban (playername) (reason)" + ChatColor.GRAY + " - Ban player.",
                ChatColor.GRAY + "" + ChatColor.BOLD + "• " + ChatColor.RED + "" + ChatColor.ITALIC + "/<command> kick (playername) (reason)" + ChatColor.GRAY + " - Kick player.",
                ChatColor.GRAY + "" + ChatColor.BOLD + "• " + ChatColor.RED + "" + ChatColor.ITALIC + "/<command> tell (playername) (message)" + ChatColor.GRAY + " - Tell a private message to the player.",
                ChatColor.GRAY + "" + ChatColor.BOLD + "• " + ChatColor.RED + "" + ChatColor.ITALIC + "/<command> delay (delay=ticks) (command to delay)" + ChatColor.GRAY + " - Delay a command execution. Time is in ticks.",
                ChatColor.GRAY + "" + ChatColor.BOLD + "• " + ChatColor.RED + "" + ChatColor.ITALIC + "/<command> denylogin (playername) (minutes) (reason)" + ChatColor.GRAY + " - Deny log-in for a player.",
                "",
                ChatColor.GOLD + "" + ChatColor.BOLD + "Auxiliary commands:",
                ChatColor.GRAY + "" + ChatColor.BOLD + "• " + ChatColor.RED + "" + ChatColor.ITALIC + "/<command> setup <survival|pvp|minigame|anarchy|status|reset>" + ChatColor.GRAY + " - First-start profile wizard.",
                ChatColor.GRAY + "" + ChatColor.BOLD + "• " + ChatColor.RED + "" + ChatColor.ITALIC + "/<command> lang <en|zh-cn|status>" + ChatColor.GRAY + " - Switch command output language.",
                ChatColor.GRAY + "" + ChatColor.BOLD + "• " + ChatColor.RED + "" + ChatColor.ITALIC + "/<command> log counters" + ChatColor.GRAY + " - Show some stats/debug counters summary.",
                ChatColor.GRAY + "" + ChatColor.BOLD + "• " + ChatColor.RED + "" + ChatColor.ITALIC + "/<command> reset counters" + ChatColor.GRAY + " - Reset some stats/debug counters.",
                ChatColor.GRAY + "" + ChatColor.BOLD + "• " + ChatColor.RED + "" + ChatColor.ITALIC + "/<command> debug player (playername) yes/no:(checktype)" + ChatColor.GRAY + " - Start/End a debug session for a specific check.",
                ChatColor.GRAY + "" + ChatColor.BOLD + "• " + ChatColor.RED + "" + ChatColor.ITALIC + "/<command> denylist" + ChatColor.GRAY + " - Lists players that have been denied to log-in.",
                ChatColor.GRAY + "" + ChatColor.BOLD + "• " + ChatColor.RED + "" + ChatColor.ITALIC + "/<command> allowlogin (playername)" + ChatColor.GRAY + " - Allow a player to login again.",
                ChatColor.GRAY + "" + ChatColor.BOLD + "• " + ChatColor.RED + "" + ChatColor.ITALIC + "/<command> exemptions (playername)" + ChatColor.GRAY + " - Lists all exemptions for a player.",
                ChatColor.GRAY + "" + ChatColor.BOLD + "• " + ChatColor.RED + "" + ChatColor.ITALIC + "/<command> exempt (playername) (checktype)" + ChatColor.GRAY + " - Exempt a player from a check. * will exempt from all checks.",
                ChatColor.GRAY + "" + ChatColor.BOLD + "• " + ChatColor.RED + "" + ChatColor.ITALIC + "/<command> unexempt (playername) (checktype)" + ChatColor.GRAY + " - Unexempt a player from a check. * will unexempt from all checks."
        };

        final String[] moreCommandsZh = new String[]{
                ChatColor.GOLD + "" + ChatColor.BOLD + "控制台命令:",
                ChatColor.GRAY + "" + ChatColor.BOLD + "• " + ChatColor.RED + "" + ChatColor.ITALIC + "/<command> ban (playername) (reason)" + ChatColor.GRAY + " - 封禁玩家。",
                ChatColor.GRAY + "" + ChatColor.BOLD + "• " + ChatColor.RED + "" + ChatColor.ITALIC + "/<command> kick (playername) (reason)" + ChatColor.GRAY + " - 踢出玩家。",
                ChatColor.GRAY + "" + ChatColor.BOLD + "• " + ChatColor.RED + "" + ChatColor.ITALIC + "/<command> tell (playername) (message)" + ChatColor.GRAY + " - 私聊消息。",
                ChatColor.GRAY + "" + ChatColor.BOLD + "• " + ChatColor.RED + "" + ChatColor.ITALIC + "/<command> delay (delay=ticks) (command to delay)" + ChatColor.GRAY + " - 延迟执行命令（单位 tick）。",
                ChatColor.GRAY + "" + ChatColor.BOLD + "• " + ChatColor.RED + "" + ChatColor.ITALIC + "/<command> denylogin (playername) (minutes) (reason)" + ChatColor.GRAY + " - 禁止玩家登录一段时间。",
                "",
                ChatColor.GOLD + "" + ChatColor.BOLD + "辅助命令:",
                ChatColor.GRAY + "" + ChatColor.BOLD + "• " + ChatColor.RED + "" + ChatColor.ITALIC + "/<command> setup <survival|pvp|minigame|anarchy|status|reset>" + ChatColor.GRAY + " - 首次启动模板向导。",
                ChatColor.GRAY + "" + ChatColor.BOLD + "• " + ChatColor.RED + "" + ChatColor.ITALIC + "/<command> lang <en|zh-cn|status>" + ChatColor.GRAY + " - 切换命令输出语言。",
                ChatColor.GRAY + "" + ChatColor.BOLD + "• " + ChatColor.RED + "" + ChatColor.ITALIC + "/<command> log counters" + ChatColor.GRAY + " - 查看统计/调试计数器。",
                ChatColor.GRAY + "" + ChatColor.BOLD + "• " + ChatColor.RED + "" + ChatColor.ITALIC + "/<command> reset counters" + ChatColor.GRAY + " - 重置统计/调试计数器。",
                ChatColor.GRAY + "" + ChatColor.BOLD + "• " + ChatColor.RED + "" + ChatColor.ITALIC + "/<command> debug player (playername) yes/no:(checktype)" + ChatColor.GRAY + " - 针对特定检查开启/关闭调试。",
                ChatColor.GRAY + "" + ChatColor.BOLD + "• " + ChatColor.RED + "" + ChatColor.ITALIC + "/<command> denylist" + ChatColor.GRAY + " - 查看禁止登录列表。",
                ChatColor.GRAY + "" + ChatColor.BOLD + "• " + ChatColor.RED + "" + ChatColor.ITALIC + "/<command> allowlogin (playername)" + ChatColor.GRAY + " - 允许玩家重新登录。",
                ChatColor.GRAY + "" + ChatColor.BOLD + "• " + ChatColor.RED + "" + ChatColor.ITALIC + "/<command> exemptions (playername)" + ChatColor.GRAY + " - 查看玩家豁免列表。",
                ChatColor.GRAY + "" + ChatColor.BOLD + "• " + ChatColor.RED + "" + ChatColor.ITALIC + "/<command> exempt (playername) (checktype)" + ChatColor.GRAY + " - 给玩家添加检查豁免（* = 全部）。",
                ChatColor.GRAY + "" + ChatColor.BOLD + "• " + ChatColor.RED + "" + ChatColor.ITALIC + "/<command> unexempt (playername) (checktype)" + ChatColor.GRAY + " - 移除玩家检查豁免（* = 全部）。"
        };

        for (int i = 0; i < moreCommandsEn.length; i++) {
            moreCommandsEn[i] = moreCommandsEn[i].replace("<command>", "ncp");
        }
        for (int i = 0; i < moreCommandsZh.length; i++) {
            moreCommandsZh[i] = moreCommandsZh[i].replace("<command>", "ncp");
        }

        String allEn = TAG + ChatColor.GOLD + "All commands info:\n";
        final Command cmd = plugin.getCommand("nocheatplus");
        if (cmd != null) {
            allEn += cmd.getUsage().replace("<command>", "ncp");
        }
        allEn += StringUtil.join(Arrays.asList(moreCommandsEn), "\n");
        this.allCommandsEn = allEn;

        String allZh = TAG + ChatColor.GOLD + "命令总览:\n";
        if (cmd != null) {
            allZh += cmd.getUsage().replace("<command>", "ncp");
        }
        allZh += StringUtil.join(Arrays.asList(moreCommandsZh), "\n");
        this.allCommandsZh = allZh;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        sender.sendMessage(LangUtil.isChinese() ? allCommandsZh : allCommandsEn);
        return true;
    }
}
