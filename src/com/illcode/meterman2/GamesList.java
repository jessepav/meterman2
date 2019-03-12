package com.illcode.meterman2;

import com.illcode.meterman2.model.Game;
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

    /**
     * Return the string assets path of a game.
     * @param gameName game name
     * @return string assets path
     */
    public String getGameAssetsPath(String gameName) {
        return gamesMap.get(gameName).assetsPath;
    }

    private void loadGamesFromGlue() {
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
                        String assetsPath = root.getChildText("assets-path");
                        String gameClassName = root.getChildText("class");
                        if (name != null && assetsPath != null && gameClassName != null) {
                            gamesMap.put(name, new PieceOfGlue(assetsPath, gameClassName));
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

    private static class PieceOfGlue {
        private final String assetsPath;
        private final String gameClassName;

        private PieceOfGlue(String assetsPath, String gameClassName) {
            this.assetsPath = assetsPath;
            this.gameClassName = gameClassName;
        }

        public String getAssetsPath() {
            return assetsPath;
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
