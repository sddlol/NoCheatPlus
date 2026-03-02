package fr.neatmonster.nocheatplus.command.admin;

import java.util.Locale;

import fr.neatmonster.nocheatplus.config.ConfPaths;
import fr.neatmonster.nocheatplus.config.ConfigFile;
import fr.neatmonster.nocheatplus.config.ConfigManager;

public final class LangUtil {

    private LangUtil() {}

    public static String normalizeLanguage(final String language) {
        if (language == null) {
            return "en";
        }
        final String lang = language.trim().toLowerCase(Locale.ROOT);
        if ("zh".equals(lang) || "zh-cn".equals(lang) || "cn".equals(lang) || "chinese".equals(lang)) {
            return "zh-cn";
        }
        return "en";
    }

    public static String getCurrentLanguage() {
        final ConfigFile config = ConfigManager.getConfigFile();
        return normalizeLanguage(config.getString(ConfPaths.SETUP_LANGUAGE, "en"));
    }

    public static boolean isChinese() {
        return "zh-cn".equals(getCurrentLanguage());
    }

    public static String tr(final String english, final String chinese) {
        return isChinese() ? chinese : english;
    }
}
