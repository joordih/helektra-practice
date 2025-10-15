package dev.voltic.helektra.plugin.utils;

import java.util.HashMap;
import java.util.Map;

import dev.voltic.helektra.plugin.Helektra;
import dev.voltic.helektra.plugin.utils.config.FileConfig;

public final class TranslationUtils {
    
    private static final Map<String, FileConfig> LOCALES = new HashMap<>();
    private static String defaultLocale = "en_US";
    
    private TranslationUtils() {
        throw new UnsupportedOperationException("Utility class");
    }
    
    public static void loadLocale(String locale) {
        if (LOCALES.containsKey(locale)) {
            return;
        }
        
        FileConfig config = new FileConfig(Helektra.getInstance(), "lang/" + locale + ".yml");
        LOCALES.put(locale, config);
        
        int keys = config.getConfig().getKeys(true).size();
        Helektra.getInstance().getLogger().info("Loaded locale: " + locale + " (" + keys + " keys)");
    }
    
    public static void setDefaultLocale(String locale) {
        defaultLocale = locale;
        loadLocale(locale);
    }
    
    public static String translate(String key) {
        return ColorUtils.translate(getRaw(key, defaultLocale));
    }
    
    public static String translate(String key, String locale) {
        return ColorUtils.translate(getRaw(key, locale));
    }
    
    public static String translate(String key, Object... replacements) {
        String text = getRaw(key, defaultLocale);
        
        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                text = text.replace("{" + replacements[i] + "}", String.valueOf(replacements[i + 1]));
            }
        }
        
        return ColorUtils.translate(text);
    }
    
    private static String getRaw(String key, String locale) {
        if (!LOCALES.containsKey(locale)) {
            loadLocale(locale);
        }
        
        FileConfig config = LOCALES.get(locale);
        if (config == null) {
            if (!locale.equals(defaultLocale)) {
                return getRaw(key, defaultLocale);
            }
            return key;
        }
        
        String value = config.getConfig().getString(key);
        return value != null ? value : key;
    }
    
    public static void initialize() {
        String configuredLang = Helektra.getInstance().getSettingsConfig()
                .getConfig().getString("settings.lang", "en_US");
        
        setDefaultLocale(configuredLang);
        
        Helektra.getInstance().getLogger().info(
            "Translation system initialized with locale: " + configuredLang
        );
    }
    
    public static void clear() {
        LOCALES.clear();
    }
}
