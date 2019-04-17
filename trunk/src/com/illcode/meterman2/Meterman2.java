package com.illcode.meterman2;

import com.illcode.meterman2.bundle.BundleGroup;
import com.illcode.meterman2.bundle.XBundle;
import com.illcode.meterman2.state.KryoPersistence;
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
    public static final int VERSION = 2;

    public static Path prefsPath, savesPath, assetsPath, gluePath, fontPath;
    static Properties prefs;

    /** The MMAssets instance handling the game assets. */
    public static MMAssets assets;

    /** The BundleGroup instance managing system and game bundles. */
    public static BundleGroup bundles;

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

    /** Package-local KryoPersistence instance used for saving and loading game state. */
    static KryoPersistence persistence;

    /** GameManager instance running the current game. */
    public static GameManager gm;

    /** GamesList instance responsible for discovery of games at startup time. */
    public static GamesList gamesList;

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

        fontPath = Paths.get(Utils.pref("font-path", "fonts"));
        if (Files.notExists(fontPath))
            Files.createDirectories(fontPath);

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

        bundles = new BundleGroup();
        final XBundle sysBundle = XBundle.loadFromPath(assets.pathForSystemAsset("system-bundle.xml"));
        final XBundle keybindings = XBundle.loadFromPath(assets.pathForSystemAsset("keybindings.xml"));
        bundles.setSystemBundles(sysBundle, keybindings);

        actions = new MMActions();
        SystemActions.init();
        attributes = new MMAttributes();
        SystemAttributes.init();

        template = new MMTemplate();
        template.initSystemHash(bundles);
        script = new MMScript();
        sound = new MMSound();
        persistence = new KryoPersistence();
        gamesList = new GamesList();
        gm = new GameManager();
        ui = new MMUI(new MMHandler());

        addScriptBindings();

        ui.show();
    }

    /** Called when the program is shutting down. */
    public static void shutdown() {
        logger.info("Meterman shutting down...");

        gm.dispose();  // the ui must still be alive when we dispose the GameManager
        ui.dispose();
        gamesList.dispose();
        persistence.dispose();
        sound.dispose();
        script.dispose();
        template.dispose();
        attributes.dispose();
        actions.dispose();
        bundles.dispose();
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

    private static void addScriptBindings() {
        script.putSystemBinding("logger", logger);
        script.putSystemBinding("ui", ui);
        script.putSystemBinding("gm", gm);
        script.putSystemBinding("sound", sound);
        script.putSystemBinding("bundles", bundles);
        script.putSystemBinding("assets", assets);
        script.putSystemBinding("actions", actions);
    }
}
