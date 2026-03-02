package fr.neatmonster.nocheatplus.command.admin;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import fr.neatmonster.nocheatplus.command.BaseCommand;
import fr.neatmonster.nocheatplus.config.ConfPaths;
import fr.neatmonster.nocheatplus.config.ConfigFile;
import fr.neatmonster.nocheatplus.config.ConfigManager;
import fr.neatmonster.nocheatplus.permissions.Permissions;

/**
 * Runtime language switch for NCP command outputs.
 */
public class LangCommand extends BaseCommand {

    public LangCommand(final JavaPlugin plugin) {
        super(plugin, "lang", Permissions.COMMAND_LANG, new String[]{"language"});
        this.usage = TAG + "Usage: /ncp lang <en|zh-cn|status>";
    }

    @Override
    public boolean testPermission(final CommandSender sender, final Command command, final String alias, final String[] args) {
        if (sender instanceof ConsoleCommandSender) {
            return true;
        }
        return super.testPermission(sender, command, alias, args);
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (args.length == 1 || (args.length == 2 && "status".equalsIgnoreCase(args[1]))) {
            final String lang = LangUtil.getCurrentLanguage();
            sender.sendMessage(TAG + ("zh-cn".equals(lang)
                    ? ChatColor.GRAY + "当前语言: " + ChatColor.YELLOW + "中文"
                    : ChatColor.GRAY + "Current language: " + ChatColor.YELLOW + "English"));
            return true;
        }

        if (args.length != 2) {
            sender.sendMessage(TAG + ChatColor.YELLOW + LangUtil.tr(
                    "Usage: /ncp lang <en|zh-cn|status>",
                    "用法: /ncp lang <en|zh-cn|status>"));
            return true;
        }

        final String normalized = LangUtil.normalizeLanguage(args[1]);
        final ConfigFile config = ConfigManager.getConfigFile();
        config.set(ConfPaths.SETUP_LANGUAGE, normalized);
        try {
            config.save(new File(access.getDataFolder(), "config.yml"));
        }
        catch (final Exception ex) {
            sender.sendMessage(TAG + ChatColor.RED + ("zh-cn".equals(normalized)
                    ? "保存语言设置失败: " + ex.getClass().getSimpleName()
                    : "Failed to save language setting: " + ex.getClass().getSimpleName()));
            return true;
        }

        sender.sendMessage(TAG + ("zh-cn".equals(normalized)
                ? ChatColor.GREEN + "语言已切换为中文，正在重载配置..."
                : ChatColor.GREEN + "Language switched to English, reloading config..."));
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "ncp reload");
        return true;
    }
}
