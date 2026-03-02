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

        final String[] mainCommandsEn = new String[]{
                ChatColor.GOLD + "" + ChatColor.BOLD + "+" + ChatColor.GRAY + "" + ChatColor.BOLD + ChatColor.STRIKETHROUGH + "------------------" + ChatColor.GOLD + "" + ChatColor.BOLD + "+",
                ChatColor.GRAY + "" + ChatColor.BOLD + "| " + ChatColor.GOLD + "" + ChatColor.BOLD + "Commands Overview" + ChatColor.GRAY + "" + ChatColor.BOLD + " |",
                ChatColor.GOLD + "" + ChatColor.BOLD + "+" + ChatColor.GRAY + "" + ChatColor.BOLD + ChatColor.STRIKETHROUGH + "------------------" + ChatColor.GOLD + "" + ChatColor.BOLD + "+",
                ChatColor.GRAY + "" + ChatColor.BOLD + "• " + ChatColor.RED + "" + ChatColor.ITALIC + "/ncp top (entries) (check/s...) (sort by...)" + ChatColor.GRAY + " - Display the top results of a given check.",
                ChatColor.GRAY + "" + ChatColor.BOLD + "• " + ChatColor.RED + "" + ChatColor.ITALIC + "/ncp info (playername)" + ChatColor.GRAY + " - Violation summary for a player.",
                ChatColor.GRAY + "" + ChatColor.BOLD + "• " + ChatColor.RED + "" + ChatColor.ITALIC + "/ncp inspect (playername)" + ChatColor.GRAY + " - Status info for a player.",
                ChatColor.GRAY + "" + ChatColor.BOLD + "• " + ChatColor.RED + "" + ChatColor.ITALIC + "/ncp notify on|off" + ChatColor.GRAY + " - Toggle in-game notifications on/off.",
                ChatColor.GRAY + "" + ChatColor.BOLD + "• " + ChatColor.RED + "" + ChatColor.ITALIC + "/ncp removeplayer (playername) [(check type)]" + ChatColor.GRAY + " - Clear a check's data for a given player.",
                ChatColor.GRAY + "" + ChatColor.BOLD + "• " + ChatColor.RED + "" + ChatColor.ITALIC + "/ncp reload" + ChatColor.GRAY + " - Reload the configuration.",
                ChatColor.GRAY + "" + ChatColor.BOLD + "• " + ChatColor.RED + "" + ChatColor.ITALIC + "/ncp lag" + ChatColor.GRAY + " - Lag-related info.",
                ChatColor.GRAY + "" + ChatColor.BOLD + "• " + ChatColor.RED + "" + ChatColor.ITALIC + "/ncp version" + ChatColor.GRAY + " - Display information about both the server and the plugin.",
                ChatColor.GRAY + "" + ChatColor.BOLD + "• " + ChatColor.RED + "" + ChatColor.ITALIC + "/ncp commands" + ChatColor.GRAY + " - List all commands, including auxiliary ones.",
                ChatColor.GRAY + "" + ChatColor.BOLD + "• " + ChatColor.RED + "" + ChatColor.ITALIC + "/ncp setup <survival|pvp|minigame|anarchy|status|reset>" + ChatColor.GRAY + " - First-start profile wizard.",
                ChatColor.GRAY + "" + ChatColor.BOLD + "• " + ChatColor.RED + "" + ChatColor.ITALIC + "/ncp lang <en|zh-cn|status>" + ChatColor.GRAY + " - Switch command output language.",
                ChatColor.GRAY + "" + ChatColor.BOLD + "• " + ChatColor.RED + "" + ChatColor.ITALIC + "/ncp stopwatch (start/stop/distance/return) [distance]" + ChatColor.GRAY + " - Simple command for measuring time and distances. Useful for testing purposes."
        };

        final String[] mainCommandsZh = new String[]{
                ChatColor.GOLD + "" + ChatColor.BOLD + "+" + ChatColor.GRAY + "" + ChatColor.BOLD + ChatColor.STRIKETHROUGH + "------------------" + ChatColor.GOLD + "" + ChatColor.BOLD + "+",
                ChatColor.GRAY + "" + ChatColor.BOLD + "| " + ChatColor.GOLD + "" + ChatColor.BOLD + "命令总览" + ChatColor.GRAY + "" + ChatColor.BOLD + " |",
                ChatColor.GOLD + "" + ChatColor.BOLD + "+" + ChatColor.GRAY + "" + ChatColor.BOLD + ChatColor.STRIKETHROUGH + "------------------" + ChatColor.GOLD + "" + ChatColor.BOLD + "+",
                ChatColor.GRAY + "" + ChatColor.BOLD + "• " + ChatColor.RED + "" + ChatColor.ITALIC + "/ncp top (entries) (check/s...) (sort by...)" + ChatColor.GRAY + " - 查看某检查的违规排行。",
                ChatColor.GRAY + "" + ChatColor.BOLD + "• " + ChatColor.RED + "" + ChatColor.ITALIC + "/ncp info (playername)" + ChatColor.GRAY + " - 查看玩家违规摘要。",
                ChatColor.GRAY + "" + ChatColor.BOLD + "• " + ChatColor.RED + "" + ChatColor.ITALIC + "/ncp inspect (playername)" + ChatColor.GRAY + " - 查看玩家状态信息。",
                ChatColor.GRAY + "" + ChatColor.BOLD + "• " + ChatColor.RED + "" + ChatColor.ITALIC + "/ncp notify on|off" + ChatColor.GRAY + " - 开关游戏内通知。",
                ChatColor.GRAY + "" + ChatColor.BOLD + "• " + ChatColor.RED + "" + ChatColor.ITALIC + "/ncp removeplayer (playername) [(check type)]" + ChatColor.GRAY + " - 清除玩家某检查的数据。",
                ChatColor.GRAY + "" + ChatColor.BOLD + "• " + ChatColor.RED + "" + ChatColor.ITALIC + "/ncp reload" + ChatColor.GRAY + " - 重载配置。",
                ChatColor.GRAY + "" + ChatColor.BOLD + "• " + ChatColor.RED + "" + ChatColor.ITALIC + "/ncp lag" + ChatColor.GRAY + " - 查看延迟/卡顿相关信息。",
                ChatColor.GRAY + "" + ChatColor.BOLD + "• " + ChatColor.RED + "" + ChatColor.ITALIC + "/ncp version" + ChatColor.GRAY + " - 查看服务端与插件版本信息。",
                ChatColor.GRAY + "" + ChatColor.BOLD + "• " + ChatColor.RED + "" + ChatColor.ITALIC + "/ncp commands" + ChatColor.GRAY + " - 查看全部命令（含辅助命令）。",
                ChatColor.GRAY + "" + ChatColor.BOLD + "• " + ChatColor.RED + "" + ChatColor.ITALIC + "/ncp setup <survival|pvp|minigame|anarchy|status|reset>" + ChatColor.GRAY + " - 首次启动模板向导。",
                ChatColor.GRAY + "" + ChatColor.BOLD + "• " + ChatColor.RED + "" + ChatColor.ITALIC + "/ncp lang <en|zh-cn|status>" + ChatColor.GRAY + " - 切换命令输出语言。",
                ChatColor.GRAY + "" + ChatColor.BOLD + "• " + ChatColor.RED + "" + ChatColor.ITALIC + "/ncp stopwatch (start/stop/distance/return) [distance]" + ChatColor.GRAY + " - 简易计时/测距工具（测试用）。"
        };

        final String[] moreCommandsEn = new String[]{
                "",
                ChatColor.GOLD + "" + ChatColor.BOLD + "Console commands:",
                ChatColor.GRAY + "" + ChatColor.BOLD + "• " + ChatColor.RED + "" + ChatColor.ITALIC + "/ncp ban (playername) (reason)" + ChatColor.GRAY + " - Ban player.",
                ChatColor.GRAY + "" + ChatColor.BOLD + "• " + ChatColor.RED + "" + ChatColor.ITALIC + "/ncp kick (playername) (reason)" + ChatColor.GRAY + " - Kick player.",
                ChatColor.GRAY + "" + ChatColor.BOLD + "• " + ChatColor.RED + "" + ChatColor.ITALIC + "/ncp tell (playername) (message)" + ChatColor.GRAY + " - Tell a private message to the player.",
                ChatColor.GRAY + "" + ChatColor.BOLD + "• " + ChatColor.RED + "" + ChatColor.ITALIC + "/ncp delay (delay=ticks) (command to delay)" + ChatColor.GRAY + " - Delay a command execution. Time is in ticks.",
                ChatColor.GRAY + "" + ChatColor.BOLD + "• " + ChatColor.RED + "" + ChatColor.ITALIC + "/ncp denylogin (playername) (minutes) (reason)" + ChatColor.GRAY + " - Deny log-in for a player.",
                "",
                ChatColor.GOLD + "" + ChatColor.BOLD + "Auxiliary commands:",
                ChatColor.GRAY + "" + ChatColor.BOLD + "• " + ChatColor.RED + "" + ChatColor.ITALIC + "/ncp log counters" + ChatColor.GRAY + " - Show some stats/debug counters summary.",
                ChatColor.GRAY + "" + ChatColor.BOLD + "• " + ChatColor.RED + "" + ChatColor.ITALIC + "/ncp reset counters" + ChatColor.GRAY + " - Reset some stats/debug counters.",
                ChatColor.GRAY + "" + ChatColor.BOLD + "• " + ChatColor.RED + "" + ChatColor.ITALIC + "/ncp debug player (playername) yes/no:(checktype)" + ChatColor.GRAY + " - Start/End a debug session for a specific check.",
                ChatColor.GRAY + "" + ChatColor.BOLD + "• " + ChatColor.RED + "" + ChatColor.ITALIC + "/ncp denylist" + ChatColor.GRAY + " - Lists players that have been denied to log-in.",
                ChatColor.GRAY + "" + ChatColor.BOLD + "• " + ChatColor.RED + "" + ChatColor.ITALIC + "/ncp allowlogin (playername)" + ChatColor.GRAY + " - Allow a player to login again.",
                ChatColor.GRAY + "" + ChatColor.BOLD + "• " + ChatColor.RED + "" + ChatColor.ITALIC + "/ncp exemptions (playername)" + ChatColor.GRAY + " - Lists all exemptions for a player.",
                ChatColor.GRAY + "" + ChatColor.BOLD + "• " + ChatColor.RED + "" + ChatColor.ITALIC + "/ncp exempt (playername) (checktype)" + ChatColor.GRAY + " - Exempt a player from a check. * will exempt from all checks.",
                ChatColor.GRAY + "" + ChatColor.BOLD + "• " + ChatColor.RED + "" + ChatColor.ITALIC + "/ncp unexempt (playername) (checktype)" + ChatColor.GRAY + " - Unexempt a player from a check. * will unexempt from all checks."
        };

        final String[] moreCommandsZh = new String[]{
                "",
                ChatColor.GOLD + "" + ChatColor.BOLD + "控制台命令:",
                ChatColor.GRAY + "" + ChatColor.BOLD + "• " + ChatColor.RED + "" + ChatColor.ITALIC + "/ncp ban (playername) (reason)" + ChatColor.GRAY + " - 封禁玩家。",
                ChatColor.GRAY + "" + ChatColor.BOLD + "• " + ChatColor.RED + "" + ChatColor.ITALIC + "/ncp kick (playername) (reason)" + ChatColor.GRAY + " - 踢出玩家。",
                ChatColor.GRAY + "" + ChatColor.BOLD + "• " + ChatColor.RED + "" + ChatColor.ITALIC + "/ncp tell (playername) (message)" + ChatColor.GRAY + " - 私聊消息。",
                ChatColor.GRAY + "" + ChatColor.BOLD + "• " + ChatColor.RED + "" + ChatColor.ITALIC + "/ncp delay (delay=ticks) (command to delay)" + ChatColor.GRAY + " - 延迟执行命令（单位 tick）。",
                ChatColor.GRAY + "" + ChatColor.BOLD + "• " + ChatColor.RED + "" + ChatColor.ITALIC + "/ncp denylogin (playername) (minutes) (reason)" + ChatColor.GRAY + " - 禁止玩家登录一段时间。",
                "",
                ChatColor.GOLD + "" + ChatColor.BOLD + "辅助命令:",
                ChatColor.GRAY + "" + ChatColor.BOLD + "• " + ChatColor.RED + "" + ChatColor.ITALIC + "/ncp log counters" + ChatColor.GRAY + " - 查看统计/调试计数器。",
                ChatColor.GRAY + "" + ChatColor.BOLD + "• " + ChatColor.RED + "" + ChatColor.ITALIC + "/ncp reset counters" + ChatColor.GRAY + " - 重置统计/调试计数器。",
                ChatColor.GRAY + "" + ChatColor.BOLD + "• " + ChatColor.RED + "" + ChatColor.ITALIC + "/ncp debug player (playername) yes/no:(checktype)" + ChatColor.GRAY + " - 针对特定检查开启/关闭调试。",
                ChatColor.GRAY + "" + ChatColor.BOLD + "• " + ChatColor.RED + "" + ChatColor.ITALIC + "/ncp denylist" + ChatColor.GRAY + " - 查看禁止登录列表。",
                ChatColor.GRAY + "" + ChatColor.BOLD + "• " + ChatColor.RED + "" + ChatColor.ITALIC + "/ncp allowlogin (playername)" + ChatColor.GRAY + " - 允许玩家重新登录。",
                ChatColor.GRAY + "" + ChatColor.BOLD + "• " + ChatColor.RED + "" + ChatColor.ITALIC + "/ncp exemptions (playername)" + ChatColor.GRAY + " - 查看玩家豁免列表。",
                ChatColor.GRAY + "" + ChatColor.BOLD + "• " + ChatColor.RED + "" + ChatColor.ITALIC + "/ncp exempt (playername) (checktype)" + ChatColor.GRAY + " - 给玩家添加检查豁免（* = 全部）。",
                ChatColor.GRAY + "" + ChatColor.BOLD + "• " + ChatColor.RED + "" + ChatColor.ITALIC + "/ncp unexempt (playername) (checktype)" + ChatColor.GRAY + " - 移除玩家检查豁免（* = 全部）。"
        };

        this.allCommandsEn = TAG + StringUtil.join(Arrays.asList(mainCommandsEn), "\n") + "\n"
                + StringUtil.join(Arrays.asList(moreCommandsEn), "\n");
        this.allCommandsZh = TAG + StringUtil.join(Arrays.asList(mainCommandsZh), "\n") + "\n"
                + StringUtil.join(Arrays.asList(moreCommandsZh), "\n");
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        sender.sendMessage(LangUtil.isChinese() ? allCommandsZh : allCommandsEn);
        return true;
    }
}
