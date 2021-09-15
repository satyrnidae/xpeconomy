package dev.satyrn.xpeconomy.lang;

import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.regex.Pattern;

/**
 * Message internationalization for Experience Economy
 */
public final class I18n {
    /**
     * The base name for all locales without a language name.
     */
    private static final String BASE_NAME = "xpeconomy";
    /**
     * Used to remove double apostrophes from the translated string.
     */
    private static final Pattern DOUBLEAPOS = Pattern.compile("''");
    /**
     * The internationalization instance.
     */
    private static I18n instance;
    /**
     * The parent plugin instance.
     */
    private final transient Plugin plugin;
    /**
     * The default language file (en_us.lang).
     */
    private final transient ResourceBundle defaultBundle;
    /**
     * Cache for formatted messages.
     */
    private final transient HashMap<String, MessageFormat> messageFormatCache = new HashMap<>();
    /**
     * The default locale for translation.
     */
    private final transient Locale defaultLocale = Locale.US;
    /**
     * The current locale for translation.
     */
    private transient Locale currentLocale = defaultLocale;
    /**
     * The language file for the current locale.
     */
    private transient ResourceBundle localeBundle;

    /**
     * Initializes a new I18n instance.
     *
     * @param plugin The parent plugin instance.
     */
    public I18n(final Plugin plugin) {
        this.plugin = plugin;
        this.defaultBundle = ResourceBundle.getBundle(BASE_NAME, this.defaultLocale, new Utf8LangFileControl());
        this.localeBundle = this.defaultBundle;
    }

    /**
     * Translates a resource string to the current locale.
     *
     * @param key    The translation key.
     * @param format The translation format.
     * @return The translated message.
     */
    public static String tr(final String key, final Object... format) {
        if (instance == null) {
            return "";
        }
        if (format.length == 0) {
            return DOUBLEAPOS.matcher(instance.translate(key)).replaceAll("'");
        } else {
            return instance.format(key, format);
        }
    }

    /**
     * Changes the locale of the internationalization handler.
     *
     * @param locale The new locale to use.
     */
    public void setLocale(final String locale) {
        if (locale != null && !locale.isEmpty()) {
            final String[] parts = locale.split("_");
            if (parts.length == 1) {
                this.currentLocale = new Locale(parts[0]);
            } else if (parts.length >= 2) {
                this.currentLocale = new Locale(parts[0], parts[parts.length - 1]);
            }
        }
        this.localeBundle = ResourceBundle.getBundle(BASE_NAME, this.currentLocale, new Utf8LangFileControl());
    }

    /**
     * Enables the i18n handler.
     */
    public void enable() {
        instance = this;
    }

    /**
     * Disables the i18n handler.
     */
    public void disable() {
        instance = null;
    }

    /**
     * Translates a key with formatting.
     *
     * @param key    The translation key.
     * @param format The objects to use while formatting.
     * @return The formatted message.
     */
    private String format(String key, Object... format) {
        String resourceString = translate(key);
        MessageFormat messageFormat = messageFormatCache.get(resourceString);
        if (messageFormat == null) {
            try {
                messageFormat = new MessageFormat(resourceString);
            } catch (IllegalArgumentException ex) {
                this.plugin.getLogger().log(Level.SEVERE,
                        String.format("Invalid Translation Key for \"%s\": %s", key, ex.getMessage()),
                        ex);
                resourceString = resourceString.replaceAll("\\{(\\D*?)}", "\\[$1\\]");
                messageFormat = new MessageFormat(resourceString);
            }
            messageFormatCache.put(resourceString, messageFormat);
        }
        return messageFormat.format(format);
    }

    /**
     * Translates a single string value.
     *
     * @param key The string value.
     * @return The translated string value.
     */
    private String translate(String key) {
        try {
            return localeBundle.getString(key);
        } catch (MissingResourceException ex) {
            this.plugin.getLogger().log(Level.WARNING,
                    String.format("Missing translation key \"%s\" in resource file \"%s.lang\"", ex.getKey(),
                            localeBundle.getLocale()), ex);
        }
        try {
            return defaultBundle.getString(key);
        } catch (MissingResourceException ex) {
            return key;
        }
    }

    /**
     * Gathers a UTF-8 *.lang file into a resource bundle.
     */
    private static class Utf8LangFileControl extends ResourceBundle.Control {
        /**
         * Creates a new bundle from the *.lang file.
         *
         * @param baseName    The base name of the file.
         * @param locale      The locale to use.
         * @param format      The file format. Unused.
         * @param classLoader The java class loader instance.
         * @param reload      Whether the stream should be reloaded or the cached class loader stream should be used.
         * @return A new ResourceBundle from the language file.
         * @throws IOException Occurs when the bundle file cannot be located or read.
         */
        public ResourceBundle newBundle(final String baseName, final Locale locale, final String format,
                                        final ClassLoader classLoader, final boolean reload) throws IOException {
            final String resourceName = this.toResourceName(this.toBundleName(baseName, locale), "lang");
            ResourceBundle bundle = null;
            InputStream stream = null;
            // Reload the file from the URL if reload is specified, otherwise use the class loader's cached stream.
            if (reload) {
                final URL url = classLoader.getResource(resourceName);
                if (url != null) {
                    final URLConnection connection = url.openConnection();
                    if (connection != null) {
                        connection.setUseCaches(false);
                        stream = connection.getInputStream();
                    }
                }
            } else {
                stream = classLoader.getResourceAsStream(resourceName);
            }

            // If we successfully found the file, create a new bundle from the stream.
            if (stream != null) {
                try {
                    bundle = new PropertyResourceBundle(new InputStreamReader(stream, StandardCharsets.UTF_8));
                } finally {
                    stream.close();
                }
            }
            return bundle;
        }

        /**
         * Gets the bundle name of this resource.
         *
         * @param baseName The fallback resource name if the locale does not have a language code.
         * @param locale   The locale.
         * @return The locale's language and country code separated by an underscore, or, if the locale lacks a language
         * code, returns the base name.
         */
        public String toBundleName(final String baseName, final Locale locale) {
            // Root locale returns base name.
            if (locale == Locale.ROOT) {
                return baseName;
            }
            final String language = locale.getLanguage();
            // If language is not specified return the base name.
            if (language == null || language.isEmpty()) {
                return baseName;
            }

            String country = locale.getCountry();
            if (country != null) {
                country = country.toLowerCase(Locale.ROOT);
            }

            StringBuilder sb = new StringBuilder(language);
            if (country != null && !country.isEmpty()) {
                sb.append('_').append(country);
            }

            return sb.toString();
        }
    }
}
