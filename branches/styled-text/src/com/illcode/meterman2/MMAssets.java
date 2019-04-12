package com.illcode.meterman2;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;

import static com.illcode.meterman2.MMLogging.logger;

public final class MMAssets
{
    private Path assetsPath, systemAssetsPath, gameAssetsPath;
    private FileSystem systemZipFs, gameZipFs;

    public MMAssets() {
    }

    public void dispose() {
        closeSystemZipFs();
        closeGameZipFs();
    }

    private void closeSystemZipFs() {
        if (systemZipFs != null) {
            try {
                systemZipFs.close();
            } catch (IOException e) {
                logger.log(Level.WARNING, "MMAssets.closeSystemZipFs()", e);
            }
            systemZipFs = null;
        }
    }

    private void closeGameZipFs() {
        if (gameZipFs != null) {
            try {
                gameZipFs.close();
            } catch (IOException e) {
                logger.log(Level.WARNING, "MMAssets.closeGameZipFs()", e);
            }
            gameZipFs = null;
        }
    }

    /**
     * Sets the base assets path against which the system and game assets paths will be resolved.
     */
    void setAssetsPath(Path assetsPath) {
        this.assetsPath = assetsPath;
    }

    /** Sets the system assets path. This will be resolved to a directory or a ZIP file.
     * @param path path, relative to {@link #setAssetsPath(Path) assetsPath}. We will first check
     * if the path is a directory, and if so, use it as is; if not, we append a ".zip" extension
     * and check if it's a ZIP file.*/
    void setSystemAssetsPath(String path) {
        closeSystemZipFs();
        if (path != null) {
            boolean isZip = StringUtils.endsWithIgnoreCase(path, ".zip");
            systemAssetsPath = assetsPath.resolve(path);
            // If the path does not resolve to a directory, check for a ZIP file.
            if (!isZip && !Files.isDirectory(systemAssetsPath)) {
                systemAssetsPath = assetsPath.resolve(path + ".zip");
                isZip = true;
            }
            if (isZip) {
                try {
                    systemZipFs = FileSystems.newFileSystem(systemAssetsPath, null);
                    systemAssetsPath = systemZipFs.getPath("/");
                } catch (IOException e) {
                    logger.log(Level.WARNING, "MMAssets.setSystemAssetsPath()", e);
                    systemAssetsPath = null;
                }
            }
        } else {
            systemAssetsPath = null;
        }
    }

    /** Sets the game assets path. This will be resolved to a directory or a ZIP file.
     * @param path path, relative to {@link #setAssetsPath(Path) assetsPath}. We will first check
     * if the path is a directory, and if so, use it as is; if not, we append a ".zip" extension
     * and check if it's a ZIP file.*/
    void setGameAssetsPath(String path) {
        closeGameZipFs();
        if (path != null) {
            boolean isZip = StringUtils.endsWithIgnoreCase(path, ".zip");
            gameAssetsPath = assetsPath.resolve(path);
            if (!isZip && !Files.isDirectory(gameAssetsPath)) {
                gameAssetsPath = assetsPath.resolve(path + ".zip");
                isZip = true;
            }
            if (isZip) {
                try {
                    gameZipFs = FileSystems.newFileSystem(gameAssetsPath, null);
                    gameAssetsPath = gameZipFs.getPath("/");
                } catch (IOException e) {
                    logger.log(Level.WARNING, "MMAssets.setGameAssetsPath()", e);
                    gameAssetsPath = null;
                }
            }
        } else {
            gameAssetsPath = null;
        }
    }

    /**
     * Returns a Path representing the given asset, resolved against the game assets path.
     * @param asset path (relative to the game assets path) of the asset we want
     * @return the resolved path
     */
    public Path pathForGameAsset(String asset) {
        return gameAssetsPath.resolve(asset);
    }

    /**
     * Returns a Path representing the given asset, resolved against the system assets path.
     * <p/>
     * This method is for the Meterman system classes only: games should call
     * {@link #pathForGameAsset(String)} instead.
     * @param asset (relative to the system assets path) of the asset we want
     * @return the resolved path
     */
    public Path pathForSystemAsset(String asset) {
        return systemAssetsPath.resolve(asset);
    }

}
