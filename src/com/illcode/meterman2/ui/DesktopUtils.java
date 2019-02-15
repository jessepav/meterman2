package com.illcode.meterman2.ui;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;

public final class DesktopUtils
{
    private static Desktop desktop;

    public static void browseURI(String uri) {
        if (!ensureDesktop())
            return;
        try {
            desktop.browse(new URI(uri));
        } catch (URISyntaxException|IOException e) {
            return;  // don't do anything - in this program, browsing is not critical
        }
    }

    public static void openPath(Path path) throws IOException {
        if (!ensureDesktop())
            return;
        desktop.open(path.toFile());
    }

    private static boolean ensureDesktop() {
        if (desktop == null) {
            if (Desktop.isDesktopSupported()) {
                desktop = Desktop.getDesktop();
            }
        }
        return desktop != null;
    }
}
