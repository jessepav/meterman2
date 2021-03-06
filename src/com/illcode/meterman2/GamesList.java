package com.illcode.meterman2;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Level;

import static com.illcode.meterman2.MMLogging.logger;

/**
 * Handles the discovery and loading of games at system startup.
 */
public final class GamesList
{
    private List<String> gameNames;
    private Map<String,PieceOfGlue> gamesMap;

    GamesList() {
        loadGamesFromGlue();
    }

    void dispose() {
        gamesMap = null;
        gameNames = null;
    }

    /** Returns true if a game with the given name exists. */
    public boolean gameExists(String gameName) {
        return gamesMap.containsKey(gameName);
    }

    /**
     * Create a new instance of a game.
     * @param gameName name of the game
     * @return a new game instance
     */
    public Game createGame(String gameName) {
        return gamesMap.get(gameName).createGame();
    }

    /** Return a list of all game names. */
    public List<String> getGameNames() {
        return gameNames;
    }

    public String getGameVersion(String gameName) {
        return gamesMap.get(gameName).version;
    }

    public boolean getGameFrameImageVisible(String gameName) {
        return gamesMap.get(gameName).frameImageVisible;
    }

    /**
     * Return the string assets path of a game.
     * @param gameName game name
     * @return string assets path
     */
    public String getGameAssetsPath(String gameName) {
        return gamesMap.get(gameName).assetsPath;
    }

    /**
     * Return the package of the game class.
     * @param gameName game name
     * @return package name
     */
    public String getGamePackageName(String gameName) {
        return gamesMap.get(gameName).getGamePackageName();
    }

    public void loadGamesFromGlue() {
        gamesMap = new HashMap<>();
        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(Meterman2.gluePath)) {
            SAXBuilder sax = new SAXBuilder();
            for (Path p : dirStream) {
                String filename = p.getFileName().toString().toLowerCase();
                if (filename.endsWith(".xml")) {
                    boolean validGlue = false;
                    parseGlue:
                    try {
                        Document doc = sax.build(p.toFile());
                        if (!doc.hasRootElement())
                            break parseGlue;
                        Element root = doc.getRootElement();
                        if (!root.getName().equals("game"))
                            break parseGlue;
                        String name = root.getChildText("name");
                        String version = root.getChildText("version");
                        String assetsPath = root.getChildText("assets-path");
                        String gameClassName = root.getChildText("class-name");
                        boolean frameImageVisible = Utils.parseBoolean(root.getChildText("frame-image-visible"));
                        if (name != null && version != null && assetsPath != null && gameClassName != null) {
                            gamesMap.put(name, new PieceOfGlue(version, assetsPath, gameClassName, frameImageVisible));
                            validGlue = true;
                        }
                    } catch (IOException|JDOMException e) {
                        logger.log(Level.WARNING, "GamesList.loadGamesFromGlue()", e);
                    }
                    if (!validGlue)
                        logger.warning("Invalid XML glue definition: " + filename);
                }
            }
        } catch (IOException e) {
            logger.log(Level.WARNING, "GamesList.loadGamesFromGlue()", e);
        }
        gameNames = new ArrayList<>(gamesMap.keySet());
        Collections.sort(gameNames);  // alphabetical order please
    }

    private static final class PieceOfGlue {
        final String version;
        final String assetsPath;
        final String gameClassName;
        final boolean frameImageVisible;

        private PieceOfGlue(String version, String assetsPath, String gameClassName, boolean frameImageVisible) {
            this.version = version;
            this.assetsPath = assetsPath;
            this.gameClassName = gameClassName;
            this.frameImageVisible = frameImageVisible;
        }

        public String getGamePackageName() {
            final int idx = gameClassName.lastIndexOf('.');
            if (idx == -1)
                return "";
            else
                return gameClassName.substring(0, idx);
        }

        public Game createGame() {
            try {
                Class<?> gameClass = Class.forName(gameClassName);
                return (Game) gameClass.newInstance();
            } catch (Exception ex) {
                logger.log(Level.WARNING, "GamesList.createGame()", ex);
                return null;
            }
        }
    }

}
