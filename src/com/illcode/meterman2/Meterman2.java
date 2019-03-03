package com.illcode.meterman2;

import com.illcode.meterman2.ui.MMUI;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.logging.Level;

import static com.illcode.meterman2.MMLogging.logger;

public final class Meterman2
{
    public static Path prefsPath, savesPath, assetsPath, gluePath;
    static Properties prefs;

    /** The MMAssets instance handling the game assets. */
    public static MMAssets assets;

    /** The MMUI instance displaying the current game */
    public static MMUI ui;

    /** The MMSound instance handling the game's sound and music playback. */
    public static MMSound sound;

    /** MMActions instance in which system and game actions are registered. */
    public static MMActions actions;

    /** MMAttributes instance in which system and game attributes are registered. */
    public static MMAttributes attributes;

    /** MMTemplate instance responsible for rendering template text sources. */
    public static MMTemplate template;

    /** MMScript instance that handles our scripting needs. */
    public static MMScript script;

    private static MMHandler uiHandler;

    public static void main(String[] args) throws IOException {
        prefsPath = Paths.get("config/meterman2.properties");
        if (!loadPrefs(prefsPath)) {
            System.err.println("Error loading prefs from " + prefsPath.toString());
            System.exit(1);
        }

        MMLogging.initializeLogging();

        savesPath = Paths.get(Utils.pref("saves-path", "saves"));
        if (Files.notExists(savesPath))
            Files.createDirectories(savesPath);

        assetsPath = Paths.get(Utils.pref("assets-path", "assets"));
        if (Files.notExists(assetsPath)) {
            logger.severe("Assets path doesn't exist!");
            return;
        }

        gluePath = Paths.get(Utils.pref("glue-path", "glue"));
        if (Files.notExists(gluePath)) {
            logger.severe("Glue path doesn't exist!");
            return;
        }

        assets = new MMAssets();
        assets.setAssetsPath(assetsPath);
        assets.setSystemAssetsPath(Utils.pref("system-assets-path", "meterman2"));

        actions = new MMActions();
        SystemActions.init();

        attributes = new MMAttributes();
        SystemAttributes.init();

        template = new MMTemplate();
        script = new MMScript();

        sound = new MMSound();
        sound.setSoundEnabled(Utils.booleanPref("sound-enabled", true));
        sound.setMusicEnabled(Utils.booleanPref("music-enabled", true));

        uiHandler = new MMHandler();
        ui = new MMUI(uiHandler);
        ui.show();
    }

    /** Called when the program is shutting down. */
    public static void shutdown() {
        logger.info("Meterman shutting down...");
        //gm.dispose();
        ui.dispose();
        //persistence.dispose();
        sound.dispose();
        script.dispose();
        template.dispose();
        assets.dispose();
        savePrefs(prefsPath);
    }


    private static boolean loadPrefs(Path path) {
        prefs = new Properties();
        if (Files.exists(path)) {
            try (FileReader r = new FileReader(path.toFile())) {
                prefs.load(r);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    private static void savePrefs(Path path) {
        try (FileWriter w = new FileWriter(path.toFile())) {
            prefs.store(w, "Meterman2 preferences");
        } catch (IOException ex) {
            logger.log(Level.WARNING, "savePrefs " + path.toString(), ex);
        }
    }
}
