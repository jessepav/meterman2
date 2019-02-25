package com.illcode.meterman2;

import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Properties;
import java.util.Random;

public final class Utils
{
    private static Random random;

    /**
     * Examines a String to determine if it's a way to say "true"
     * @param s if this is "true", "on", "1", or "yes" then we consider it true
     * @return truth value of {@code s}
     */
    public static boolean parseBoolean(String s) {
        if (s != null && (s.equalsIgnoreCase("true") || s.equalsIgnoreCase("on") || s.equals("1") || s.equals("yes")))
            return true;
        else
            return false;
    }

    /**
     * Parses an input string as a decimal integer
     * @param s String representation of an integer
     * @param errorVal if {@code s} is not successfully parsed, we return this value
     * @return int value of {@code s} if parseable, or {@code errorVal} otherwise
     */
    public static int parseInt(String s, int errorVal) {
        int i = errorVal;
        if (s != null && s.length() > 0) {
            try {
                i = Integer.parseInt(s);
            } catch (NumberFormatException ex) {
                i = errorVal;
            }
        }
        return i;
    }

    /**
     * Parses an input string as a float
     * @param s String representation of a float
     * @param errorVal if {@code s} is not successfully parsed, we return this value
     * @return float value of {@code s} if parseable, or {@code errorVal} otherwise
     */
    public static float parseFloat(String s, float errorVal) {
        float f = errorVal;
        if (s != null && s.length() > 0) {
            try {
                f = Float.parseFloat(s);
            } catch (NumberFormatException ex) {
                f = errorVal;
            }
        }
        return f;
    }

    /**
     * Parses an input string as a double
     * @param s String representation of a double
     * @param errorVal if {@code s} is not successfully parsed, we return this value
     * @return double value of {@code s} if parseable, or {@code errorVal} otherwise
     */
    public static double parseDouble(String s, double errorVal) {
        double f = errorVal;
        if (s != null && s.length() > 0) {
            try {
                f = Double.parseDouble(s);
            } catch (NumberFormatException ex) {
                f = errorVal;
            }
        }
        return f;
    }

    /**
     * Equivalent to {@link #parseInt(String, int) parseInt(s, 0)}
     */
    public static int parseInt(String s) {
        return parseInt(s, 0);
    }

    /**
     * Ensures an input int {@code i} is between {@code [min, max]} (inclusive on both ends).
     */
    public static int clamp(int i, int min, int max) {
        if (i < min) return min;
        else if (i > max) return max;
        else return i;
    }

    /** Equivalent to {@code String.format(s, args)} */
    public static String fmt(String s, Object... args) {
        return String.format(s, args);
    }

    /**
     * Returns the boolean value of a preference. If the key doesn't exist,
     * it will be created and set to {@code defaultVal}.
     * @param key preference key
     * @param defaultVal default value
     * @return value of the preference, or defaultVal if it doesn't exist.
     */
    public static boolean booleanPref(String key, boolean defaultVal) {
        if (Meterman2.prefs.containsKey(key)) {
            return parseBoolean(Meterman2.prefs.getProperty(key));
        } else {
            Meterman2.prefs.setProperty(key, Boolean.toString(defaultVal));
            return defaultVal;
        }
    }

    /**
     * Returns the int value of a preference. If the key doesn't exist,
     * it will be created and set to {@code defaultVal}. If the value of
     * the preference cannot be parsed as an int, we return {@code defaultVal}.
     * @param key preference key
     * @param defaultVal default value
     * @return value of the preference, or defaultVal if it doesn't exist.
     */
    public static int intPref(String key, int defaultVal) {
        if (Meterman2.prefs.containsKey(key)) {
            return parseInt(Meterman2.prefs.getProperty(key), defaultVal);
        } else {
            Meterman2.prefs.setProperty(key, Integer.toString(defaultVal));
            return defaultVal;
        }
    }

    /**
     * Returns the float value of a preference. If the key doesn't exist,
     * it will be created and set to {@code defaultVal}. If the value of
     * the preference cannot be parsed as a float, we return {@code defaultVal}.
     * @param key preference key
     * @param defaultVal default value
     * @return value of the preference, or defaultVal if it doesn't exist.
     */
    public static float floatPref(String key, float defaultVal) {
        if (Meterman2.prefs.containsKey(key)) {
            return parseFloat(Meterman2.prefs.getProperty(key), defaultVal);
        } else {
            Meterman2.prefs.setProperty(key, Float.toString(defaultVal));
            return defaultVal;
        }
    }

    /**
     * Returns the String value of a preference. If the key doesn't exist,
     * it will be created and set to {@code defaultVal}.
     * @param key preference key
     * @param defaultVal default value
     * @return value of the preference, or defaultVal if it doesn't exist.
     */
    public static String pref(String key, String defaultVal) {
        if (Meterman2.prefs.containsKey(key)) {
            return Meterman2.prefs.getProperty(key);
        } else {
            Meterman2.prefs.setProperty(key, defaultVal);
            return defaultVal;
        }
    }

    /** Facade for {@link Properties#getProperty(java.lang.String)}*/
    public static String getPref(String key) {
        return Meterman2.prefs.getProperty(key);
    }

    /** Facade for {@link Properties#setProperty(java.lang.String, java.lang.String)}*/
    public static void setPref(String key, String value) {
        Meterman2.prefs.setProperty(key, value);
    }

    /** Causes the current thread to sleep for {@code millis} milliseconds, catching InterruptedException
     *  and re-setting the thread interrupted flag. */
    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Returns a random int between {@code min} and {@code max}, inclusive.
     */
    public static int randInt(int min, int max) {
        if (random == null)
            random = new Random();
        return random.nextInt(max - min + 1) + min;
    }

    /**
     * Read all characters available from a Reader and return them as a string.
     * We use a BufferedReader internally to make the process more efficient.
     * @param r Reader from which to read characters
     * @param bufferSize buffer size for our BufferedReader
     * @return String read from the reader, or null if an exception occurred
     */
    public static String slurpReaderText(Reader r, int bufferSize) {
        String s = null;
        try (BufferedReader reader = new BufferedReader(r)) {
            char [] buffer = new char[bufferSize];
            StringBuilder sb = new StringBuilder(5*bufferSize);
            int n;
            while ((n = reader.read(buffer, 0, buffer.length)) != -1)
                sb.append(buffer, 0, n);
            s = sb.toString();
        } catch (IOException ex) {
            s = null;
        }
        return s;
    }

    /**
     * Loads a ClassLoader text resource and returns its contents as a String, assuming a UTF-8 encoding.
     * @param resourcePath class-loader resource path, relative to {@code Utils.class}
     * @return String value of resource, or an empty string on error
     */
    public static String getStringResource(String resourcePath) {
        String val = null;
        InputStream in = Utils.class.getResourceAsStream(resourcePath);
        if (in != null)
            val = Utils.slurpReaderText(new InputStreamReader(in, StandardCharsets.UTF_8), 512);
        return val != null ? val : "";
    }

    /**
     * Return the filename extension of a path.
     * @param p path
     * @return the filename extension without the '.'. If the given path has no filename extension,
     *      then we return the empty string "".
     */
    public static String getPathExtension(Path p) {
        return FilenameUtils.getExtension(p.toString());
    }
}
