package com.illcode.meterman2.ui;

import com.illcode.meterman2.MMActions.Action;
import com.illcode.meterman2.Meterman2;
import com.illcode.meterman2.SystemActions;
import com.illcode.meterman2.Utils;
import com.illcode.meterman2.text.TextUtils;
import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.map.LRUMap;
import org.jdom2.Element;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import static com.illcode.meterman2.MMLogging.logger;

public final class MMUI
{
    MainFrame mainFrame;
    TextDialog textDialog;
    PromptDialog promptDialog;
    ListDialog listDialog;
    ImageDialog imageDialog;
    SelectItemDialog selectItemDialog;
    WaitDialog waitDialog;

    List<String> roomEntityIds, inventoryEntityIds;

    private Map<String,Path> imageMap;
    private LRUImageCacheMap loadedImages;
    private BufferedImage defaultFrameImage;
    private String currentFrameImage, currentEntityImage;

    int maxBufferSize;
    int dialogTextColumns;

    UIHandler handler;

    public MMUI(UIHandler handler) {
        this.handler = handler;
        roomEntityIds = new ArrayList<>(16);
        inventoryEntityIds = new ArrayList<>(16);
        final int cacheSize = Utils.intPref("image-cache-size", 32);
        imageMap = new HashMap<>(cacheSize * 2);
        loadedImages = new LRUImageCacheMap(cacheSize);
    }

    /**
     * Initializes and displays the UI.
     */
    public void show() {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                } catch (ClassNotFoundException | UnsupportedLookAndFeelException |
                         InstantiationException | IllegalAccessException | ClassCastException ex) {
                    logger.log(Level.WARNING, "UIManager.setLookAndFeel()", ex);
                }
                // This prevents JComboBox from firing an ActionEvent every time the selection
                // changes when using keyboard navigation.
                UIManager.getLookAndFeelDefaults().put("ComboBox.noActionOnKeyNavigation", Boolean.TRUE);

                final ToolTipManager toolTipManager = ToolTipManager.sharedInstance();
                toolTipManager.setInitialDelay(Utils.intPref("tooltip-initial-delay", 750));
                toolTipManager.setDismissDelay(Utils.intPref("tooltip-dismiss-delay", 4000));

                GuiUtils.initGraphics();

                mainFrame = new MainFrame(MMUI.this);
                textDialog = new TextDialog(mainFrame.frame);
                promptDialog = new PromptDialog(mainFrame.frame);
                listDialog = new ListDialog(mainFrame.frame);
                imageDialog = new ImageDialog(mainFrame.frame);
                selectItemDialog = new SelectItemDialog(mainFrame.frame);
                waitDialog = new WaitDialog(mainFrame.frame);

                GuiUtils.registerFontDir(Meterman2.fontPath);

                clearStatusLabels();

                setRoomName("");

                defaultFrameImage = GuiUtils.loadBitmaskImage(Meterman2.assets.pathForSystemAsset("default-frame-image.png"));
                currentFrameImage = UIConstants.NO_IMAGE;
                currentEntityImage = UIConstants.NO_IMAGE;

                maxBufferSize = Utils.intPref("max-text-buffer-size", 50000);
                dialogTextColumns = Utils.intPref("dialog-text-columns", 60);
                setGameName(null);
                handler.uiInitialized();
                mainFrame.setVisible(true);
                mainFrame.startup();
            }
        });
    }

    /**
     * Hides the interface and disposes of any resources used by the UI.
     */
    public void dispose() {
        Runnable doRun = new Runnable() {
            public void run() {
                clearImages();
                defaultFrameImage.flush();
                defaultFrameImage = null;
                waitDialog.dispose();
                selectItemDialog.dispose();
                imageDialog.dispose();
                listDialog.dispose();
                promptDialog.dispose();
                textDialog.dispose();
                mainFrame.dispose();
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            doRun.run();
        } else {
            try {
                SwingUtilities.invokeAndWait(doRun);
            } catch (InterruptedException | InvocationTargetException e) {
                logger.log(Level.WARNING, "MMUI.dispose()", e);
            }
        }
        loadedImages = null;
        inventoryEntityIds = null;
        roomEntityIds = null;
        this.handler = null;
    }

    /**
     * Sets the frame title (or equivalent) and About menu text to show the game name.
     * @param name game name, or null if no game loaded
     */
    public void setGameName(String name) {
        if (name == null) {
            mainFrame.frame.setTitle("Meterman2 (no game loaded)");
            mainFrame.aboutMenuItem.setText("About...");
            mainFrame.aboutMenuItem.setEnabled(false);
        } else {
            mainFrame.frame.setTitle("Meterman2 - " + name);
            mainFrame.aboutMenuItem.setText("About " + name);
            mainFrame.aboutMenuItem.setEnabled(true);
        }
    }

    /** Set the text of the Look and Wait UI buttons to the text of the
     *  LOOK and WAIT system actions, respectively. */
    public void setGlobalActionButtonText() {
        mainFrame.setGlobalActionButtonText(SystemActions.LOOK, SystemActions.WAIT);
    }

    /**
     * Add an action keyboard shortcut.
     * @param a action
     * @param keystroke keystroke, as specified by {@link javax.swing.KeyStroke#getKeyStroke(java.lang.String)}.
     */
    public void putActionBinding(Action a, String keystroke) { mainFrame.putActionBinding(a, keystroke); }

    /**
     * Remove an action keyboard shortcut.
     * @param a action
     */
    public void removeActionBinding(Action a) { mainFrame.removeActionBinding(a); }

    /** Clear all action keyboard shortcuts. */
    public void clearActionBindings() { mainFrame.clearActionBindings(); }

    /**
     * Opens a web browser to the given URL, if applicable. If the UI doesn't
     * support this, or the URL is malformed, nothing will happen.
     * @param url URL to open
     */
    public void openURL(String url) {
        DesktopUtils.browseURI(url);
    }

    /**
     * Add an image mapping.
     * @param name name by which the image will be referenced
     * @param path path to the image file. JPEG and PNG (with bitmask transparency) are supported.
     */
    public void addImageMapping(String name, Path path) {
        imageMap.put(name, path);
    }

    /**
     * Remove an image mapping. If the image is loaded, it will be unloaded.
     * @param name name under which the image was added.
     */
    public void removeImageMapping(String name) {
        unloadImage(name);
        imageMap.remove(name);
    }

    /**
     * Load image mappings from an image-map XML element.
     * @param el element
     */
    public void loadImageMap(Element el) {
        if (el == null)
            return;
        for (Element source : el.getChildren("image")) {
            String name = source.getAttributeValue("name");
            String path = source.getAttributeValue("path");
            if (name == null || path == null)
                continue;
            addImageMapping(name, Meterman2.assets.pathForGameAsset(path));
        }
    }

    /**
     * Remove image mappings defined in an image-map XML element.
     * @param el element
     */
    public void removeImageMap(Element el) {
        if (el == null)
            return;
        for (Element source : el.getChildren("image")) {
            String name = source.getAttributeValue("name");
            if (name == null)
                continue;
            removeImageMapping(name);
        }
    }

    /**
     * Load an image, if it's not already loaded.
     * @param name image name, as given in {@link #addImageMapping(String, Path)}
     * @return image thus loaded, or null if it could not be loaded
     */
    public BufferedImage loadImage(String name) {
        BufferedImage img = loadedImages.get(name);
        if (img == null) {
            Path p = imageMap.get(name);
            if (p == null)
                return null;
            img = GuiUtils.loadBitmaskImage(p);
            if (img != null)
                loadedImages.put(name, img);
        }
        return img;
    }

    /**
     * Unload an image.
     * @param name name of image
     */
    public void unloadImage(String name) {
        BufferedImage img = loadedImages.remove(name);
        if (img != null) {
            if (currentFrameImage.equals(name)) {
                currentFrameImage = UIConstants.NO_IMAGE;
                mainFrame.setFrameImage(null);
            }
            if (currentEntityImage.equals(name)) {
                currentEntityImage = UIConstants.NO_IMAGE;
                mainFrame.setEntityImage(null);
            }
            img.flush();
        }
    }

    /** Unload all images and remove all mappings. */
    public void clearImages() {
        mainFrame.setFrameImage(null);
        currentFrameImage = UIConstants.NO_IMAGE;
        mainFrame.setEntityImage(null);
        currentEntityImage = UIConstants.NO_IMAGE;
        MapIterator<String,BufferedImage> iter = loadedImages.mapIterator();
        while (iter.hasNext()) {
            iter.next();
            iter.getValue().flush();
        }
        loadedImages.clear();
        imageMap.clear();
    }

    /**
     * Sets the image displayed in the main UI frame. The recommended size for
     * frame images is 150x400 pixels, or an integer fraction of that, in which
     * case the image will be scaled up.
     * @param imageName name of the image
     */
    public void setFrameImage(String imageName) {
        if (currentFrameImage.equals(imageName))
            return;
        currentFrameImage = imageName;
        BufferedImage img;
        if (imageName == UIConstants.DEFAULT_FRAME_IMAGE)
            img = defaultFrameImage;
        else if (imageName == UIConstants.NO_IMAGE)
            img = null;
        else
            img = loadImage(imageName);
        mainFrame.setFrameImage(img);
    }

    /**
     * Get the name of the current frame image. This can be useful if a game object
     * wants to save the current image name so that it can restore it later.
     * @return the name of the current frame image.
     */
    public String getFrameImage() {
        return currentFrameImage;
    }

    /**
     * Sets the entity image that will be drawn inset in the frame image. The recommended size for entity
     * images is 140x140 pixels, or an integer fraction of that, in which case the image will be scaled up;
     * the image itself should have a border to visually separate it from the frame image.
     * @param imageName name of the image
     */
    public void setEntityImage(String imageName) {
        if (currentEntityImage.equals(imageName))
            return;
        currentEntityImage = imageName;
        BufferedImage img;
        if (imageName == UIConstants.NO_IMAGE)
            img = null;
        else
            img = loadImage(imageName);
        mainFrame.setEntityImage(img);
    }

    /**
     * Get the name of the current entity image. This can be useful if a game object
     * wants to save the current image name so that it can restore it later.
     * @return the name of the current entity image.
     */
    public String getEntityImage() {
        return currentEntityImage;
    }

    /**
     * Sets the room name displayed in the UI
     * @param name room name
     */
    public void setRoomName(String name) {
        mainFrame.roomNameLabel.setText(name);
    }

    /**
     * Clears the main text area.
     */
    public void clearText() {
        mainFrame.textArea.setText(null);
    }

    /**
     * Appends text to the main text area.
     * @param text text to append
     */
    public void appendText(String text) {
        JTextArea ta = mainFrame.textArea;
        ta.append(text);
        Document doc = ta.getDocument();
        int len = doc.getLength();
        if (len > maxBufferSize) {
            try {
                doc.remove(0, len - maxBufferSize);
                len = maxBufferSize;
            } catch (BadLocationException e) {
                logger.log(Level.WARNING, "SwingUI.appendText()", e);
            }
        }
        ta.setCaretPosition(len); // scroll to the bottom of the text area
    }

    /**
     * Clears the list displaying Entities in the current room.
     */
    public void clearRoomEntities() {
        roomEntityIds.clear();
        mainFrame.roomListModel.clear();
    }

    /**
     * Adds an entity to the list of entities in the current room.
     * @param id entity ID
     * @param name name to show in the list
     */
    public void addRoomEntity(String id, String name) {
        roomEntityIds.add(id);
        mainFrame.roomListModel.addElement(name);
    }

    /**
     * Removes an entity from the list of entities in the current room.
     * @param id entity ID
     */
    public void removeRoomEntity(String id) {
        int idx = roomEntityIds.indexOf(id);
        if (idx != -1) {
            roomEntityIds.remove(idx);
            mainFrame.roomListModel.remove(idx);
        }
    }

    /**
     * Update the list item corresponding to an entity in the current room.
     * @param id entity ID
     * @param name name to show in the list
     */
    public void updateRoomEntity(String id, String name) {
        int idx = roomEntityIds.indexOf(id);
        if (idx != -1)
            mainFrame.roomListModel.set(idx, name);
    }

    /**
     * Clears the list displaying Entities in the player's inventory.
     */
    public void clearInventoryEntities() {
        inventoryEntityIds.clear();
        mainFrame.inventoryListModel.clear();
    }

    /**
     * Adds an entity to the list of entities in the player's inventory.
     * @param id entity ID
     * @param name name to show in the list
     */
    public void addInventoryEntity(String id, String name) {
        inventoryEntityIds.add(id);
        mainFrame.inventoryListModel.addElement(name);
    }

    /**
     * Removes an entity from the list of entities in the player's inventory.
     * @param id entity ID
     */
    public void removeInventoryEntity(String id) {
        int idx = inventoryEntityIds.indexOf(id);
        if (idx != -1) {
            inventoryEntityIds.remove(idx);
            mainFrame.inventoryListModel.remove(idx);
        }
    }

    /**
     * Update the list item corresponding to an entity in inventory.
     * @param id entity ID
     * @param name name to show in the list
     */
    public void updateInventoryEntity(String id, String name) {
        int idx = inventoryEntityIds.indexOf(id);
        if (idx != -1)
            mainFrame.inventoryListModel.set(idx, name);
    }

    /**
     * Cause a given entity to be selected in the UI, if it is present in the room
     * or inventory lists.
     * @param id entity ID
     */
    public void selectEntity(String id) {
        int idx = roomEntityIds.indexOf(id);
        if (idx != -1) {
            mainFrame.roomList.setSelectedIndex(idx);
        } else {
            idx = inventoryEntityIds.indexOf(id);
            if (idx != -1)
                mainFrame.inventoryList.setSelectedIndex(idx);
        }
    }

    /**
     * Clears any selection in the room and inventory entity lists.
     */
    public void clearEntitySelection() {
        mainFrame.roomList.clearSelection();
        mainFrame.inventoryList.clearSelection();
    }


    /**
     * Clears the exit button list.
     */
    public void clearExits() {
        mainFrame.clearExits();
    }

    /**
     * Sets a given exit button to a Room, or hides it.
     * @param buttonPos one of the constants indicating a button position (ex. {@link UIConstants#N_BUTTON})
     * @param label label to use for the specified exit button; if null, the given button will be hidden.
     */
    public void setExitLabel(int buttonPos, String label) {
        mainFrame.setExitLabel(buttonPos, label);
    }

    /**
     * Clears the action button group.
     */
    public void clearActions() {
        mainFrame.clearActions();
    }

    /**
     * Add an action to the action button list. If the action is already present,
     * this method will return without any effect.
     * @param action action to add
     */
    public void addAction(Action action) {
        mainFrame.addAction(action);
    }

    /**
     * Removes an action from the action button list.
     * @param action action to remove
     */
    public void removeAction(Action action) {
        mainFrame.removeAction(action);
    }

    /**
     * Sets one of the three status bar labels.
     * @param labelPosition one of {@link UIConstants#LEFT_LABEL},
     *           {@link UIConstants#CENTER_LABEL}, {@link UIConstants#RIGHT_LABEL}
     * @param label the text to show for the given label; if null, the label is cleared.
     */
    public void setStatusLabel(int labelPosition, String label) {
        switch (labelPosition) {
        case UIConstants.LEFT_LABEL:
            mainFrame.leftStatusLabel.setText(label);
            break;
        case UIConstants.CENTER_LABEL:
            mainFrame.centerStatusLabel.setText(label);
            break;
        case UIConstants.RIGHT_LABEL:
            mainFrame.rightStatusLabel.setText(label);
            break;
        }
    }

    public void clearStatusLabels() {
        setStatusLabel(UIConstants.LEFT_LABEL, "");
        setStatusLabel(UIConstants.CENTER_LABEL, "");
        setStatusLabel(UIConstants.RIGHT_LABEL, "");
    }

    /**
     * Displays a modal dialog showing a passage of text, with multiple button choices.
     * @param header header surmounted above the text passage
     * @param text text passage
     * @param buttonLabels labels of the buttons presented as choices (up to 3)
     * @return the 0-based index of the button selected by the user, or -1 if the dialog
     *         was closed without selecting a button.
     */
    public int showTextDialog(String header, String text, String... buttonLabels) {
        handler.transcribe(text, true).transcribe(Utils.NL);
        int r = showTextDialogImpl(header, text, buttonLabels);
        if (r != -1 && r < buttonLabels.length)
            handler.transcribe(">").transcribe(buttonLabels[r]).transcribe(Utils.NL);
        return r;
    }

    /** Like {@link #showTextDialog} but without transcribing. Intended for use by the game system. */
    public int showTextDialogImpl(String header, String text, String... buttonLabels) {
        return textDialog.show(header, wrapDialogText(text), buttonLabels);
    }

    /**
     * Shows a modal dialog displaying an image and text, with multiple button choices.
     * @param header header surmounted above the image
     * @param imageName name of the image
     * @param scale the factor (>= 1) by which the image will be scaled before being shown
     * @param text text passage shown below the image
     * @param buttonLabels labels of the buttons presented as choices (up to 3)
     * @return the 0-based index of the button selected by the user, or -1 if the dialog
     *         was closed without selecting a button.
     */
    public int showImageDialog(String header, String imageName, int scale, String text, String... buttonLabels) {
        handler.transcribe(text, true).transcribe(Utils.NL);
        int r = showImageDialogImpl(header, imageName, scale, text, buttonLabels);
        if (r != -1 && r < buttonLabels.length)
            handler.transcribe(">").transcribe(buttonLabels[r]).transcribe(Utils.NL);
        return r;
    }

    /** Like {@link #showImageDialog} but without transcribing. Intended for use by the game system. */
    public int showImageDialogImpl(String header, String imageName, int scale, String text, String... buttonLabels) {
        BufferedImage image = imageName == UIConstants.NO_IMAGE ? null : loadImage(imageName);
        if (image != null && scale > 1)
            image = GuiUtils.getScaledImage(image, scale);
        return imageDialog.show(header, image, wrapDialogText(text), buttonLabels);
    }

    /**
     * Shows a dialog allowing the user to select one of a list of items.
     * @param header header surmounted above the text passage
     * @param text text passage
     * @param items items from which the user can select one
     * @param showCancelButton if true, a cancel button will be shown; if clicked,
     *          this method will return null
     * @return the item selected, or null if no item selected.
     */
    public <T> T showListDialog(String header, String text, List<T> items, boolean showCancelButton) {
        handler.transcribe(text, true).transcribe(Utils.NL);
        final T choice = showListDialogImpl(header, text, items, showCancelButton);
        if (choice != null)
            handler.transcribe("> ").transcribe(choice.toString()).transcribe(Utils.NL);
        return choice;
    }

    /** Like {@link #showListDialog} but without transcribing. Intended for use by the game system. */
    public <T> T showListDialogImpl(String header, String text, List<T> items, boolean showCancelButton) {
        return listDialog.showListDialog(header, wrapDialogText(text), items, showCancelButton);
    }

    /**
     * Displays a modal dialog showing a passage of text and a field for the user to
     * enter a line of text.
     * @param header header surmounted above the text passage
     * @param text text passage
     * @param prompt prompt displayed in front of the field
     * @param initialText the text initially set in the text field
     * @return the text entered by the user
     */
    public String showPromptDialog(String header, String text, String prompt, String initialText) {
        handler.transcribe(text, true).transcribe(Utils.NL);
        final String s = showPromptDialogImpl(header, text, prompt, initialText);
        if (!s.isEmpty())
            handler.transcribe(prompt).transcribe(" >").transcribe(s).transcribe(Utils.NL);
        return s;
    }

    /** Like {@link #showPromptDialog} but without transcribing. Intended for use by the game system. */
    public String showPromptDialogImpl(String header, String text, String prompt, String initialText) {
        return promptDialog.show(header, wrapDialogText(text), prompt, initialText);
    }

    /**
     * Displays an always-on-top dialog with a message, that stays visible until {@link #hideWaitDialog() hidden}. It is
     * intended to inform the user when a potentially long-running operation is taking place.
     * @param message message to show
     */
    public void showWaitDialog(String message) {
        waitDialog.show(message);
    }

    /**
     * Hides the dialog previously shown by {@link #showWaitDialog(String)}.
     */
    public void hideWaitDialog() {
        waitDialog.hide();
    }

    /** Wraps text according to the current "dialog-text-columns" config property. */
    String wrapDialogText(String text) {
        return TextUtils.wrapText(text, dialogTextColumns);
    }

    private class LRUImageCacheMap extends LRUMap<String,BufferedImage>
    {
        LRUImageCacheMap(int maxSize) {
            super(maxSize, true);
        }

        protected boolean removeLRU(LinkEntry<String,BufferedImage> entry) {
            final String name = entry.getKey();
            final BufferedImage image = entry.getValue();
            // Don't evict a currently-used image.
            if (!name.equals(currentFrameImage) && !name.equals(currentEntityImage)) {
                image.flush();
                return true;
            } else {
                return false;
            }
        }
    }
}
