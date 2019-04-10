package com.illcode.meterman2.ui;

import com.illcode.meterman2.Utils;
import org.apache.commons.lang3.StringUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Level;

import static com.illcode.meterman2.MMLogging.logger;

public final class GuiUtils
{
    public static final KeyStroke ESCAPE_KEY_STROKE = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);

    static GraphicsEnvironment graphicsEnvironment;
    static GraphicsConfiguration graphicsConfiguration;
    static Toolkit toolkit;

    private static RenderingHints fastRenderingHints, qualityRenderingHints;

    static void initGraphics() {
        graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
        graphicsConfiguration = graphicsEnvironment.getDefaultScreenDevice().getDefaultConfiguration();
        toolkit = Toolkit.getDefaultToolkit();
    }

    static void syncToolkit() {
        toolkit.sync();
    }

    /** Creates a BufferedImage compatible with our default screen device, with BITMASK transparency.
     *  This means that each pixel of the image will be either completely opaque (alpha 255) or
     *  completely translucent (alpha 0). */
    public static BufferedImage createBitmaskImage(final int width, final int height) {
        return graphicsConfiguration.createCompatibleImage(width, height, Transparency.BITMASK);
    }

    /** Creates a BufferedImage compatible with our default screen device, with OPAQUE transparency. */
    public static BufferedImage createOpaqueImage(final int width, final int height) {
        return graphicsConfiguration.createCompatibleImage(width, height, Transparency.OPAQUE);
    }

    /** Creates a BufferedImage compatible with our default screen device, with TRANSLUCENT transparency. */
    public static BufferedImage createTranslucentImage(int width, int height) {
        return graphicsConfiguration.createCompatibleImage(width, height, Transparency.TRANSLUCENT);
    }

    /** Load an opaque image from a given Path */
    public static BufferedImage loadOpaqueImage(Path p) {
        BufferedImage image;
        try {
            BufferedImage bi = ImageIO.read(p.toUri().toURL());
            image = createOpaqueImage(bi.getWidth(), bi.getHeight());
            Graphics g = image.getGraphics();
            g.drawImage(bi, 0, 0, null);
            g.dispose();
            bi.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
            image = null;
        }
        return image;
    }

    /** Load a translucent image from a given Path */
    public static BufferedImage loadTranslucentImage(Path p) {
        BufferedImage image;
        try {
            BufferedImage bi = ImageIO.read(p.toUri().toURL());
            image = createTranslucentImage(bi.getWidth(), bi.getHeight());
            Graphics g = image.getGraphics();
            g.drawImage(bi, 0, 0, null);
            g.dispose();
            bi.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
            image = null;
        }
        return image;
    }

    /**
     * Load a bitmask image from a given Path.
     * @param path path from which to load the image
     */
    public static BufferedImage loadBitmaskImage(Path path) {
        return loadBitmaskImage(path, -1);
    }

    /**
     * Load a bitmask image from a given Path, optionally making a specified color transparent.
     * @param path path from which to load the image
     * @param transparentColor the 0x00RRGGBB color to make transparent. If equal to -1, no
     *          color transparency filtering will be performed.
     */
    public static BufferedImage loadBitmaskImage(Path path, int transparentColor) {
        BufferedImage image = null;
        try {
            BufferedImage bi = ImageIO.read(path.toUri().toURL());
            int w = bi.getWidth();
            int h = bi.getHeight();
            if (transparentColor != -1) {
                int[] rgb = bi.getRGB(0, 0, w, h, null, 0, w);
                for (int i = 0; i < rgb.length; i++) {
                    if ((rgb[i] & 0x00FFFFFF) == transparentColor)
                        rgb[i] = 0;  // fully transparent black
                }
                bi.setRGB(0, 0, w, h, rgb, 0, w);
            }
            image = createBitmaskImage(w, h);
            Graphics g = image.getGraphics();
            g.drawImage(bi, 0, 0, null);
            g.dispose();
            bi.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
            image = null;
        }
        return image;
    }

    /**
     * Returns a scaled version of a buffered image.
     * @param img image to scale
     * @param scale the factor {@code (> 1)} by which to scale the image
     * @return scaled image
     */
    public static BufferedImage getScaledImage(BufferedImage img, int scale) {
        if (scale <= 1)
            return img;
        return getSubImage(img, 0, 0, img.getWidth(), img.getHeight(), scale);
    }

    /**
     * Returns a new image generated from a sub-rectangle of the given source image.
     * @param img source image
     * @param sx1 the x coordinate of the first corner of the source rectangle.
     * @param sy1 the y coordinate of the first corner of the source rectangle.
     * @param sx2 the x coordinate of the second corner of the source rectangle.
     * @param sy2 the y coordinate of the second corner of the source rectangle.
     * @param scale scale factor {@code (>= 1)} by which to enlarge the resulting image
     * @return new image
     */
    public static BufferedImage getSubImage(BufferedImage img, int sx1, int sy1, int sx2, int sy2, int scale) {
        if (scale < 1)
            scale = 1;
        int dw = (sx2 - sx1) * scale;
        int dh = (sy2 - sy1) * scale;
        BufferedImage subImage = img.getTransparency() == Transparency.OPAQUE
            ? createOpaqueImage(dw, dh) : createBitmaskImage(dw, dh);
        Graphics2D g2d = subImage.createGraphics();
        g2d.setRenderingHints(getFastRenderingHints());
        g2d.drawImage(img, 0, 0, dw, dh, sx1, sy1, sx2, sy2, null);
        g2d.dispose();
        return subImage;
    }

    static RenderingHints getFastRenderingHints() {
        if (fastRenderingHints == null) {
            fastRenderingHints = new RenderingHints(null);
            fastRenderingHints.put(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
            fastRenderingHints.put(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
            fastRenderingHints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            fastRenderingHints.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            fastRenderingHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        }
        return fastRenderingHints;
    }

    static RenderingHints getQualityRenderingHints() {
        if (qualityRenderingHints == null) {
            qualityRenderingHints = new RenderingHints(null);
            qualityRenderingHints.put(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
            qualityRenderingHints.put(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
            qualityRenderingHints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            qualityRenderingHints.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            qualityRenderingHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        }
        return qualityRenderingHints;
    }

    public static void repaintImmediately(JComponent component) {
        component.paintImmediately(0, 0, component.getWidth(), component.getHeight());
    }

    static void registerFont(Path fontFilePath) {
        try {
            final Font font = Font.createFont(Font.TRUETYPE_FONT, fontFilePath.toFile());
            logger.config("Loaded Font: " + fontString(font));
            graphicsEnvironment.registerFont(font);
        } catch (FontFormatException|IOException e) {
            logger.log(Level.WARNING, "registerGameFonts()", e);
        }
    }

    /**
     * Returns a String representation of a font suitable to be parsed by {@link Font#decode(String)}.
     * <p/>
     * This is in the form of {@code "<fontname>-<style>-<pointsize>"}.
     * @param font a font
     * @return the string representation
     */
    static String fontString(Font font) {
        StringBuilder sb = new StringBuilder();
        sb.append(font.getFamily()).append("-");
        switch (font.getStyle()) {
        case Font.PLAIN:
            sb.append("PLAIN");
            break;
        case Font.ITALIC:
            sb.append("ITALIC");
            break;
        case Font.BOLD:
            sb.append("BOLD");
            break;
        case Font.BOLD + Font.ITALIC:
            sb.append("BOLDITALIC");
            break;
        }
        sb.append("-").append(font.getSize());
        return sb.toString();
    }


    public static void setBoundsFromPrefs(Window w, String prefName, String defaultVal) {
        int x = -1, y = -1, width = -1, height = -1;
        String val = Utils.pref(prefName, defaultVal);
        if (val != null && !val.isEmpty()) {
            String[] vals = StringUtils.split(val, ", ");
            if (vals.length == 4) {
                x = Utils.parseInt(vals[0], 50);
                y = Utils.parseInt(vals[1], 20);
                width = Utils.parseInt(vals[2], 800);
                height = Utils.parseInt(vals[3], 600);
            } else if (vals.length == 2) {
                width = Utils.parseInt(vals[0], 800);
                height = Utils.parseInt(vals[1], 600);
            }

            if (x != -1) {
                w.setBounds(x, y, width, height);
            } else if (width != -1) {
                w.setSize(width, height);
                w.setLocationRelativeTo(null);
            }
        }
    }

    public static void saveBoundsToPref(Window w, String prefName) {
        Rectangle r = w.getBounds();
        Utils.setPref(prefName, Utils.fmt("%d, %d, %d, %d", r.x, r.y, r.width, r.height));
    }

    public static void attachEscapeCloseOperation(final JDialog dialog) {
        Action closingAction = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                dialog.dispatchEvent(new WindowEvent(dialog, WindowEvent.WINDOW_CLOSING));
            }
        };
        JRootPane root = dialog.getRootPane();
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ESCAPE_KEY_STROKE, "ESCAPE:WINDOW_CLOSING");
        root.getActionMap().put("ESCAPE:WINDOW_CLOSING", closingAction);
    }
}
