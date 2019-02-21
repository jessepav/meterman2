package com.illcode.meterman2.ui;

import com.illcode.meterman2.Meterman2;
import com.illcode.meterman2.Utils;
import com.illcode.meterman2.model.Entity;

import static com.illcode.meterman2.MMLogging.logger;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public final class MMUI
{
    MainFrame mainFrame;
    //TextDialog textDialog;
    //PromptDialog promptDialog;
    //ListDialog listDialog;
    //ImageDialog imageDialog;
    //SelectItemDialog selectItemDialog;
    WaitDialog waitDialog;

    private List<Entity> roomEntities, inventoryEntities;

    private Map<String,BufferedImage> imageMap;
    private BufferedImage defaultFrameImage;
    private String currentFrameImage, currentEntityImage;

    int maxBufferSize;

    public MMUI() {
        roomEntities = new ArrayList<>();
        inventoryEntities = new ArrayList<>();
        imageMap = new HashMap<>();
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

                GuiUtils.initGraphics();

                mainFrame = new MainFrame(MMUI.this);
                //textDialog = new TextDialog(mainFrame.frame);
                //promptDialog = new PromptDialog(mainFrame.frame);
                //listDialog = new ListDialog(mainFrame.frame);
                //imageDialog = new ImageDialog(mainFrame.frame);
                //selectItemDialog = new SelectItemDialog(mainFrame.frame);
                waitDialog = new WaitDialog(mainFrame.frame);

                setStatusLabel(UIConstants.LEFT_LABEL, "");
                setStatusLabel(UIConstants.CENTER_LABEL, "");
                setStatusLabel(UIConstants.RIGHT_LABEL, "");

                defaultFrameImage = GuiUtils.loadBitmaskImage(Meterman2.assets.pathForSystemAsset("default-frame-image.png"));
                currentFrameImage = UIConstants.NO_IMAGE;
                currentEntityImage = UIConstants.NO_IMAGE;

                maxBufferSize = Utils.intPref("max-text-buffer-size", 50000);
                setGameName(null);
                mainFrame.setVisible(true);
                mainFrame.startup();
            }
        });
    }

    /**
     * Hides the interface and disposes of any resources used by the UI.
     */
    public void dispose() {

    }

    /**
     * Sets the frame title (or equivalent) and About menu text to show the game name.
     * @param name game name, or null if no game loaded
     */
    public void setGameName(String name) {

    }

    /**
     * Opens a web browser to the given URL, if applicable. If the UI doesn't
     * support this, or the URL is malformed, nothing will happen.
     * @param url URL to open
     */
    public void openURL(String url) {

    }

    /**
     * Load an image into the UI. JPEG and PNG (with bitmask transparency) are supported.
     * @param name name by which the image will be referred to in the {@code setXXXImage()} methods.
     * @param p path of the image file.
     */
    public void loadImage(String name, Path p) {

    }

    /**
     * Unload an image from the UI.
     * @param name name of image
     */
    public void unloadImage(String name) {

    }

    /** Unload all images from the UI. */
    public void unloadAllImages() {

    }


    /**
     * Sets the image displayed in the main UI frame. The recommended size for
     * frame images is 150x400 pixels, or an integer fraction of that, in which
     * case the image will be scaled up.
     * @param imageName name of the image, as chosen in {@link #loadImage(String, Path)}
     */
    public void setFrameImage(String imageName) {

    }

    /**
     * Get the name of the current frame image. This can be useful if a game object
     * wants to save the current image name so that it can restore it later.
     * @return the name of the current frame image.
     */
    public String getFrameImage() {
        return null;
    }

    /**
     * Sets the entity image that will be drawn inset in the frame image. The recommended size for entity
     * images is 140x140 pixels, or an integer fraction of that, in which case the image will be scaled up;
     * the image itself should have a border to visually separate it from the frame image.
     * @param imageName name of the image, as chosen in {@link #loadImage(String, Path)}
     */
    public void setEntityImage(String imageName) {

    }

    /**
     * Get the name of the current entity image. This can be useful if a game object
     * wants to save the current image name so that it can restore it later.
     * @return the name of the current entity image.
     */
    public String getEntityImage() {
        return null;
    }

    /**
     * Sets the room name displayed in the UI
     * @param name room name
     */
    public void setRoomName(String name) {

    }

    /**
     * Clears the main text area.
     */
    public void clearText() {

    }

    /**
     * Appends text to the main text area.
     * @param text text to append
     */
    public void appendText(String text) {

    }

    /**
     * Appens a newline to the main text area.
     */
    public void appendNewline() {

    }

    /**
     * Appends text to the main text area, followed by a newline.
     * @param text text to append
     */
    public void appendTextLn(String text) {

    }

    /**
     * Clears the list displaying Entities in the current room.
     */
    public void clearRoomEntities() {

    }

    /**
     * Adds an entity to the list of entities in the current room.
     * @param e entity to add
     */
    public void addRoomEntity(Entity e) {

    }

    /**
     * Removes an entity from the list of entities in the current room.
     * @param e entity to remove
     */
    public void removeRoomEntity(Entity e) {

    }

    /**
     * Refresh the list item corresponding to an entity in the current room.
     * @param e entity to refresh
     */
    public void refreshRoomEntity(Entity e) {

    }

    /**
     * Clears the list displaying Entities in the player's inventory.
     */
    public void clearInventoryEntities() {

    }

    /**
     * Adds an entity to the list of entities in the player's inventory.
     * @param e entity to add
     * @param modifiers string to append to the list display for the entity, to indicate status like worn or
     *                  equipped. Will be <tt>null</tt> if there are no modifiers.
     */
    public void addInventoryEntity(Entity e, String modifiers) {

    }

    /**
     * Removes an entity from the list of entities in the player's inventory.
     * @param e entity to remove
     */
    public void removeInventoryEntity(Entity e) {

    }

    /**
     * Refresh the list item corresponding to an entity in inventory.
     * @param e entity to refresh
     * @param modifiers string to append to the list display for the entity, to indicate status like worn or
     *                  equipped. Will be <tt>null</tt> if there are no modifiers.
     */
    public void refreshInventoryEntity(Entity e, String modifiers) {

    }

    /**
     * Cause a given entity to be selected in the UI, if it is present in the room
     * or inventory lists.
     * @param e entity to select
     */
    public void selectEntity(Entity e) {

    }

    /**
     * Clears any selection in the room and inventory entity lists.
     */
    public void clearEntitySelection() {

    }


    /**
     * Clears the exit button list.
     */
    public void clearExits() {

    }

    /**
     * Sets a given exit button to a Room, or hides it.
     * @param buttonPos one of the constants indicating a button position (ex. {@link UIConstants#N_BUTTON})
     * @param label label to use for the specified exit button; if null, the given button will be hidden.
     */
    public void setExitLabel(int buttonPos, String label) {

    }

    /**
     * Clears the action button group.
     */
    public void clearActions() {

    }

    /**
     * Add an action to the action button list. If an action with the same label is already shown,
     * this method will return without any effect.
     * @param actionLabel action to add
     */
    public void addAction(String actionLabel) {

    }

    /**
     * Removes an action from the action button list.
     * @param actionLabel action to remove
     */
    public void removeAction(String actionLabel) {

    }

    /**
     * Sets one of the three status bar labels.
     * @param labelPosition one of {@link UIConstants#LEFT_LABEL},
     *           {@link UIConstants#CENTER_LABEL}, {@link UIConstants#RIGHT_LABEL}
     * @param label the text to show for the given label
     */
    public void setStatusLabel(int labelPosition, String label) {

    }

    /**
     * Displays a modal dialog showing a passage of text.
     * @param header header surmounted above the text passage
     * @param text text passage (line-breaks kept intact)
     * @param buttonLabel label of the button to dismiss dialog
     */
    public void showTextDialog(String header, String text, String buttonLabel) {

    }

    /**
     * Displays a modal dialog showing a passage of text and a field for the user to
     * enter a line of text.
     * @param header header surmounted above the text passage
     * @param text text passage (line-breaks kept intact)
     * @param prompt prompt displayed in front of the field
     * @param initialText the text initially set in the text field
     * @return the text entered by the user
     */
    public String showPromptDialog(String header, String text, String prompt, String initialText) {
        return null;
    }

    /**
     * Shows a dialog allowing the user to select one of a list of items.
     * @param header header surmounted above the text passage
     * @param text text passage (line-breaks kept intact)
     * @param items items from which the user can select one
     * @param showCancelButton if true, a cancel button will be shown; if clicked,
     *          this method will return null
     * @return the item selected, or null if no item selected.
     */
    public <T> T showListDialog(String header, String text, List<T> items, boolean showCancelButton) {
        return null;
    }

    /**
     * Shows a dialog displaying an image.
     * @param header header surmounted above the image
     * @param imageName name of the image, as chosen in {@link #loadImage(String, Path)}
     * @param scale the factor (>= 1) by which the image will be scaled before being shown
     * @param text text passage (line-breaks kept intact) shown below the image
     * @param buttonLabel label of the button used to dismiss dialog
     */
    public void showImageDialog(String header, String imageName, int scale, String text, String buttonLabel) {

    }

    /**
     * Displays an always-on-top dialog with a message, that stays visible until {@link #hideWaitDialog() hidden}. It is
     * intended to inform the user when a potentially long-running operation is taking place.
     * @param message message to show
     */
    public void showWaitDialog(String message) {

    }

    /**
     * Hides the dialog previously shown by {@link #showWaitDialog(String)}.
     */
    public void hideWaitDialog() {

    }
}
