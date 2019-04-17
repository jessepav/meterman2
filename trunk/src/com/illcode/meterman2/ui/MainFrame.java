package com.illcode.meterman2.ui;

import com.illcode.meterman2.MMActions.Action;
import com.illcode.meterman2.Meterman2;
import com.illcode.meterman2.SystemActions;
import com.illcode.meterman2.Utils;
import com.jformdesigner.model.FormModel;
import com.jformdesigner.runtime.FormCreator;
import com.jformdesigner.runtime.FormLoader;
import org.apache.commons.lang3.ArrayUtils;
import org.jdom2.Element;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.DefaultStyledDocument;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Level;

import static com.illcode.meterman2.MMLogging.logger;

final class MainFrame implements ActionListener, ListSelectionListener
{
    static final int NUM_EXIT_BUTTONS = 12;
    static final int NUM_ACTION_BUTTONS = 8;

    private MMUI ui;

    JFrame frame;
    JMenu gameMenu, settingsMenu, helpMenu;
    JMenuItem newMenuItem, saveMenuItem, saveAsMenuItem, loadMenuItem, endMenuItem, saveTranscriptMenuItem,
        quitMenuItem, aboutMenuItem, webSiteMenuItem, onlineManualMenuItem, scrollbackMenuItem,
        chooseFontsMenuItem;
    JCheckBoxMenuItem alwaysLookCheckBoxMenuItem, musicCheckBoxMenuItem, soundCheckBoxMenuItem,
        promptToQuitCheckBoxMenuItem;
    JPanel imagePanel;
    JLabel roomNameLabel, roomListLabel, inventoryListLabel, exitsLabel, actionsLabel;
    JButton lookButton, waitButton;
    JTextPane textPane;
    DefaultStyledDocument document;
    JList<String> roomList, inventoryList;
    JButton[] exitButtons, actionButtons;
    JComboBox<String> moreActionCombo;
    StringBuilder toolTipBuilder;
    JLabel leftStatusLabel, centerStatusLabel, rightStatusLabel;
    FrameImageComponent imageComponent;

    ChooseFontsDialog chooseFontsDialog;

    JFileChooser fc;
    File currentSaveFile;

    DefaultListModel<String> roomListModel, inventoryListModel;

    private InputMap inputMap;    // key binding maps for the frame's root pane
    private ActionMap actionMap;

    private Map<Action,KeyStroke> actionKeystrokeMap;
    private Set<KeyStroke> boundKeystrokes;

    private BufferedImage frameImage, entityImage;
    private List<Action> actions;
    private Action lastAction;

    private boolean suppressValueChanged;

    // For our action popup menu
    private JPopupMenu popupMenu;
    private List<JMenuItem> popupItems;  // we maintain a pool of menu items and actions to avoid allocation

    @SuppressWarnings("unchecked")
    MainFrame(MMUI ui) {
        this.ui = ui;
        try {
            FormModel formModel = FormLoader.load("com/illcode/meterman2/ui/MainFrame.jfd");
            FormCreator cr = new FormCreator(formModel);

            frame = (JFrame) cr.createWindow(null);
            gameMenu = cr.getMenu("gameMenu");
            settingsMenu = cr.getMenu("settingsMenu");
            helpMenu = cr.getMenu("helpMenu");
            newMenuItem = cr.getMenuItem("newMenuItem");
            saveMenuItem = cr.getMenuItem("saveMenuItem");
            saveAsMenuItem = cr.getMenuItem("saveAsMenuItem");
            loadMenuItem = cr.getMenuItem("loadMenuItem");
            endMenuItem = cr.getMenuItem("endMenuItem");
            saveTranscriptMenuItem = cr.getMenuItem("saveTranscriptMenuItem");
            quitMenuItem = cr.getMenuItem("quitMenuItem");
            aboutMenuItem = cr.getMenuItem("aboutMenuItem");
            webSiteMenuItem = cr.getMenuItem("webSiteMenuItem");
            onlineManualMenuItem = cr.getMenuItem("onlineManualMenuItem");
            scrollbackMenuItem = cr.getMenuItem("scrollbackMenuItem");
            chooseFontsMenuItem = cr.getMenuItem("chooseFontsMenuItem");
            alwaysLookCheckBoxMenuItem = cr.getCheckBoxMenuItem("alwaysLookCheckBoxMenuItem");
            musicCheckBoxMenuItem = cr.getCheckBoxMenuItem("musicCheckBoxMenuItem");
            soundCheckBoxMenuItem = cr.getCheckBoxMenuItem("soundCheckBoxMenuItem");
            promptToQuitCheckBoxMenuItem = cr.getCheckBoxMenuItem("promptToQuitCheckBoxMenuItem");
            imagePanel = cr.getPanel("imagePanel");
            roomNameLabel = cr.getLabel("roomNameLabel");
            roomListLabel = cr.getLabel("roomListLabel");
            inventoryListLabel = cr.getLabel("inventoryListLabel");
            exitsLabel = cr.getLabel("exitsLabel");
            actionsLabel = cr.getLabel("actionsLabel");
            lookButton = cr.getButton("lookButton");
            waitButton = cr.getButton("waitButton");
            textPane = cr.getTextPane("textPane");
            document = new DefaultStyledDocument();
            textPane.setStyledDocument(document);
            roomList = cr.getList("roomList");
            inventoryList = cr.getList("inventoryList");
            exitButtons = new JButton[NUM_EXIT_BUTTONS];
            for (int i = 0; i < NUM_EXIT_BUTTONS; i++)
                exitButtons[i] = cr.getButton("exitButton" + (i+1));
            actionButtons = new JButton[NUM_ACTION_BUTTONS];
            for (int i = 0; i < NUM_ACTION_BUTTONS; i++)
                actionButtons[i] = cr.getButton("actionButton" + (i+1));
            moreActionCombo = cr.getComboBox("moreActionCombo");
            leftStatusLabel = cr.getLabel("leftStatusLabel");
            centerStatusLabel = cr.getLabel("centerStatusLabel");
            rightStatusLabel = cr.getLabel("rightStatusLabel");

            imageComponent = new FrameImageComponent();
            imagePanel.add(imageComponent);

            frame.getRootPane().setDoubleBuffered(true);
            frame.addWindowListener(new FrameWindowListener());

            roomListModel = new DefaultListModel<>();
            inventoryListModel = new DefaultListModel<>();
            roomList.setModel(roomListModel);
            inventoryList.setModel(inventoryListModel);

            for (AbstractButton b : new AbstractButton[]
                   {newMenuItem, saveMenuItem, saveAsMenuItem, loadMenuItem, endMenuItem, saveTranscriptMenuItem,
                    quitMenuItem, aboutMenuItem, alwaysLookCheckBoxMenuItem, musicCheckBoxMenuItem,
                    soundCheckBoxMenuItem, promptToQuitCheckBoxMenuItem, webSiteMenuItem, scrollbackMenuItem,
                    chooseFontsMenuItem, onlineManualMenuItem, lookButton, waitButton})
                b.addActionListener(this);
            for (JButton b : exitButtons)
                b.addActionListener(this);
            for (JButton b : actionButtons)
                b.addActionListener(this);
            roomList.addListSelectionListener(this);
            inventoryList.addListSelectionListener(this);
            ListMouseListener lml = new ListMouseListener();
            roomList.addMouseListener(lml);
            inventoryList.addMouseListener(lml);
            moreActionCombo.addActionListener(this);
            toolTipBuilder = new StringBuilder(96);
            actions = new ArrayList<>(16);

            fc = new JFileChooser();
            fc.setCurrentDirectory(Meterman2.savesPath.toFile());

            final JRootPane root = frame.getRootPane();
            inputMap = root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
            actionMap = root.getActionMap();
            actionKeystrokeMap = new HashMap<>(32);
            boundKeystrokes = new HashSet<>(32);
            installKeyBindings();

            frame.setIconImage(GuiUtils.loadOpaqueImage(Meterman2.assets.pathForSystemAsset("frame-icon-32x32.png")));

            GuiUtils.setBoundsFromPrefs(frame, "main-window-size", "900, 700");
        } catch (Exception ex) {
            logger.log(Level.WARNING, "MainFrame()", ex);
        }
    }

    void dispose() {
        GuiUtils.saveBoundsToPref(frame, "main-window-size");
        frameImage = null;
        entityImage = null;
        setVisible(false);
        frame.dispose();
    }


    private void installKeyBindings() {
        final Element keybindingsEl = Meterman2.bundles.getElement("ui-shortcuts");
        if (keybindingsEl == null)
            return;
        for (Element binding : keybindingsEl.getChildren("binding")) {
            final String name = binding.getAttributeValue("name");
            final String shortcut = binding.getAttributeValue("shortcut");
            if (name == null || shortcut == null)
                continue;
            final KeyStroke k = KeyStroke.getKeyStroke(shortcut);
            if (k == null)
                continue;
            Object actionKey = name;
            javax.swing.Action action = actionMap.get(actionKey);
            final boolean needAction = action == null;
            switch (name) {
            case "select-room-entity":
                if (needAction)
                    action = new SelectItemAction(roomList, roomListModel,
                        "Select an object in the room", "Object:");
                break;
            case "select-inventory-entity":
                if (needAction)
                    action = new SelectItemAction(inventoryList, inventoryListModel,
                        "Select an item in your inventory", "Item:");
                break;
            case "select-action":
                if (needAction)
                    action = new SelectItemAction(actions, "Select an action", "Action:");
                break;
            case "again-action":
                if (needAction)
                    action = new SpecialAction(SpecialAction.AGAIN);
                break;
            case "debug-command":
                if (needAction)
                    action = new SpecialAction(SpecialAction.DEBUG);
                break;
            default:
                actionKey = null;
                break;
            }
            if (actionKey != null) {
                inputMap.put(k, actionKey);
                if (needAction)
                    actionMap.put(actionKey, action);
            }
        }

        final Element movementEl = keybindingsEl.getChild("movement");
        if (movementEl == null)
            return;
        for (Element binding : movementEl.getChildren("binding")) {
            final String name = binding.getAttributeValue("name");
            final String shortcut = binding.getAttributeValue("shortcut");
            if (name == null || shortcut == null)
                continue;
            final int buttonPos = UIConstants.buttonTextToPosition(name);
            if (buttonPos == -1)
                continue;
            final KeyStroke k = KeyStroke.getKeyStroke(shortcut);
            if (k == null)
                continue;
            final Object actionKey = "selectExit:" + name;
            javax.swing.Action action = actionMap.get(actionKey);
            inputMap.put(k, actionKey);
            if (action == null) {
                action = new ButtonAction(exitButtons[buttonPos]);
                actionMap.put(actionKey, action);
            }
        }
    }

    void putActionBinding(Action a, String keystroke) {
        removeActionBinding(a);
        KeyStroke k = KeyStroke.getKeyStroke(keystroke);
        if (k == null || boundKeystrokes.contains(k))  // don't allow re-binding
            return;
        final Object actionKey = "action:" + a.getName();
        inputMap.put(k, actionKey);
        actionMap.put(actionKey, new GameAction(a));
        actionKeystrokeMap.put(a, k);
        boundKeystrokes.add(k);
    }

    void removeActionBinding(Action a) {
        KeyStroke k = actionKeystrokeMap.remove(a);
        if (k == null)
            return;
        final Object actionKey = inputMap.get(k);
        inputMap.remove(k);
        actionMap.remove(actionKey);
        boundKeystrokes.remove(k);
    }

    void clearActionBindings() {
        for (KeyStroke k : actionKeystrokeMap.values()) {
            final Object actionKey = inputMap.get(k);
            inputMap.remove(k);
            actionMap.remove(actionKey);
        }
        actionKeystrokeMap.clear();
        boundKeystrokes.clear();
    }

    void setGlobalActionButtonText(Action lookAction, Action waitAction) {
        lookButton.setText(lookAction.getText());
        lookButton.setToolTipText(getActionToolTipText(lookAction));
        waitButton.setText(waitAction.getText());
        waitButton.setToolTipText(getActionToolTipText(waitAction));
    }

    void setVisible(boolean visible) {
        frame.setVisible(visible);
    }

    void startup() {
        soundCheckBoxMenuItem.setSelected(ui.handler.isSoundEnabled());
        musicCheckBoxMenuItem.setSelected(ui.handler.isMusicEnabled());
        alwaysLookCheckBoxMenuItem.setSelected(ui.handler.isAlwaysLook());
        promptToQuitCheckBoxMenuItem.setSelected(Utils.booleanPref("prompt-to-quit", true));

        suppressValueChanged = true;
        ui.clearRoomEntities();
        ui.clearInventoryEntities();
        suppressValueChanged = false;
        ui.clearActions();
        ui.clearExits();
        ui.clearText();
        ui.setFrameImage(UIConstants.DEFAULT_FRAME_IMAGE);

        initGame:  // make the user keep selecting choices until a game is
        do {       // successfully started or loaded
            String choice;
            do {  // don't let the user avoid making a choice
                choice = ui.showListDialogImpl("Meterman2", "Select an option",
                    Arrays.asList("New Game", "Load Game", "Quit"), false);
            } while (choice == null);
            switch (choice) {
            case "New Game":
                newMenuItem.doClick();
                break;
            case "Load Game":
                loadMenuItem.doClick();
                break;
            case "Quit":
                close();
                break initGame;
            }
        } while (ui.handler.isGameActive() == false);
    }

    private void close() {
        if (Utils.booleanPref("prompt-to-quit", true)) {
            if (ui.handler.isGameActive() &&
                ui.showTextDialogImpl("Quit?", "Quit Meterman2?", "Quit", "Don't") != 0)
                return;  // don't quit
        }
        Meterman2.shutdown();
    }

    void setComponentFonts(Font mainTextFont, Font headerFont, Font listFont,
                           Font labelFont, Font buttonFont, Font dialogTextFont) {
        textPane.setFont(mainTextFont);
        roomNameLabel.setFont(headerFont.deriveFont(headerFont.getSize2D() + 2.0f));
        roomList.setFont(listFont);
        inventoryList.setFont(listFont);
        for (JLabel l : new JLabel[] {roomListLabel, inventoryListLabel, exitsLabel, actionsLabel})
            l.setFont(labelFont);
        final Font statusLabelFont = labelFont.deriveFont(labelFont.getSize2D() - 1.0f);
        for (JLabel l : new JLabel[] {leftStatusLabel, centerStatusLabel, rightStatusLabel})
            l.setFont(statusLabelFont);
        lookButton.setFont(buttonFont);
        waitButton.setFont(buttonFont);
        final Font gridButtonFont = buttonFont.deriveFont(buttonFont.getSize2D() - 1.0f);
        for (JButton b : exitButtons)
            b.setFont(gridButtonFont);
        for (JButton b : actionButtons)
            b.setFont(gridButtonFont);
        moreActionCombo.setFont(gridButtonFont);
    }

    void setFrameImage(BufferedImage image) {
        frameImage = image;
        if (imagePanel.isVisible())
            imageComponent.repaint();
    }

    void setEntityImage(BufferedImage image) {
        entityImage = image;
        if (imagePanel.isVisible())
            imageComponent.repaint();
    }

    public void clearExits() {
        for (JButton b : exitButtons)
            b.setVisible(false);
    }

    public void setExitLabel(int buttonPos, String label) {
        if (buttonPos < 0 || buttonPos >= UIConstants.NUM_EXIT_BUTTONS)
            return;
        JButton b = exitButtons[buttonPos];
        if (label == null) {
            b.setVisible(false);
        } else {
            b.setVisible(true);
            b.setText(label);
        }
    }

    void clearActions() {
        actions.clear();
        for (JButton b : actionButtons) {
            b.setVisible(false);
            b.setToolTipText(null);
        }
        moreActionCombo.setVisible(false);
        moreActionCombo.removeAllItems();
        moreActionCombo.addItem("More...");
        moreActionCombo.setToolTipText(null);
    }

    void addAction(Action action) {
        actions.add(action);
        final int n = actions.size();
        if (n <= NUM_ACTION_BUTTONS) {
            final JButton b = actionButtons[n - 1];
            b.setText(action.getText());
            b.setToolTipText(getActionToolTipText(action));
            b.setVisible(true);
        } else {
            moreActionCombo.addItem(action.getText());
            moreActionCombo.setVisible(true);
            // Construct a tooltip of all the actions in the combo box
            toolTipBuilder.setLength(0);
            for (int i = NUM_ACTION_BUTTONS; i < n; i++) {
                final Action a = actions.get(i);
                final String ttt = getActionToolTipText(a);
                if (ttt != null) {
                    if (toolTipBuilder.length() == 0)
                        toolTipBuilder.append("<html>");
                    else
                        toolTipBuilder.append("<br/>");
                    toolTipBuilder.append(a.getText()).append(" - ").append(ttt);
                }
            }
            if (toolTipBuilder.length() != 0) {
                toolTipBuilder.append("</html>");
                moreActionCombo.setToolTipText(toolTipBuilder.toString());
            } else {
                moreActionCombo.setToolTipText(null);
            }
        }
    }

    private String getActionToolTipText(Action action) {
        String toolTipText = null;
        final KeyStroke k = actionKeystrokeMap.get(action);
        if (k != null)
            toolTipText = k.toString().replace("pressed", "+");
        return toolTipText;
    }

    void removeAction(Action action) {
        if (actions.remove(action)) {
            clearActions();
            for (Action a : actions)
                addAction(a);
        }
    }

    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        int buttonIdx;

        if (source == lookButton) {
            ui.handler.lookCommand();
        } else if (source == waitButton) {
            ui.handler.waitCommand();
        } else if ((buttonIdx = ArrayUtils.indexOf(exitButtons, source)) != -1) {
            ui.handler.exitSelected(buttonIdx);
        } else if ((buttonIdx = ArrayUtils.indexOf(actionButtons, source)) != -1) {
            actionSelected(actions.get(buttonIdx));
        } else if (source == moreActionCombo) {
            int idx = moreActionCombo.getSelectedIndex();
            if (idx > 0)   // index 0 is "More..."
                actionSelected(actions.get(idx - 1 + NUM_ACTION_BUTTONS));
        } else if (source == newMenuItem) {
            String gameName = Utils.getPref("single-game-name");
            if (gameName == null)
                gameName = ui.showListDialogImpl("New Game", "Choose a game:", ui.handler.getGameNames(), true);
            if (gameName != null) {
                currentSaveFile = null;
                ui.handler.newGame(gameName);
            }
        } else if (source == loadMenuItem) {
            int r = fc.showOpenDialog(frame);
            if (r == JFileChooser.APPROVE_OPTION) {
                final File f = fc.getSelectedFile();
                try (InputStream in = new FileInputStream(f)) {
                    ui.handler.loadGameState(in);
                    currentSaveFile = f;
                } catch (Exception ex) {
                    logger.log(Level.WARNING, "MainFrame loadMenuItem", ex);
                    ui.showTextDialogImpl("Load Error", ex.getMessage(), "OK");
                }
            }
        } else if (source == saveMenuItem) {
            if (currentSaveFile == null) {
                saveAsMenuItem.doClick();
                return;
            }
            try (OutputStream out = new FileOutputStream(currentSaveFile)) {
                ui.handler.saveGameState(out);
            } catch (Exception ex) {
                logger.log(Level.WARNING, "MainFrame saveMenuItem", ex);
                ui.showTextDialogImpl("Save Error", ex.getMessage(), "OK");
            }
        } else if (source == saveAsMenuItem) {
            int r = fc.showSaveDialog(frame);
            if (r == JFileChooser.APPROVE_OPTION) {
                currentSaveFile = fc.getSelectedFile();
                saveMenuItem.doClick();
            }
        } else if (source == endMenuItem) {
            if (ui.handler.isGameActive()) {
                int r = ui.showTextDialogImpl("End Game", "Are you sure you want to end the game?", "Yes", "No");
                if (r == 0)
                    ui.handler.endGame();
            }
        } else if (source == saveTranscriptMenuItem) {
            int r = fc.showSaveDialog(frame);
            if (r == JFileChooser.APPROVE_OPTION) {
                String text = ui.handler.getTranscript();
                if (text == null)
                    return;
                try {
                    final Path p = fc.getSelectedFile().toPath();
                    Files.write(p, text.getBytes(StandardCharsets.UTF_8));
                    ui.showTextDialogImpl("Saved", "Transcript saved to " + p.getFileName().toString(), "OK");
                } catch (IOException ex) {
                    logger.log(Level.WARNING, "MainFrame saveTranscript", ex);
                    ui.showTextDialogImpl("Save Error", ex.getMessage(), "OK");
                }
            }
        } else if (source == quitMenuItem) {
            close();
        } else if (source == musicCheckBoxMenuItem) {
            boolean enabled = musicCheckBoxMenuItem.isSelected();
            ui.handler.setMusicEnabled(enabled);
            Utils.setPref("music-enabled", Boolean.toString(enabled));
        } else if (source == soundCheckBoxMenuItem) {
            boolean enabled = soundCheckBoxMenuItem.isSelected();
            ui.handler.setSoundEnabled(enabled);
            Utils.setPref("sound-enabled", Boolean.toString(enabled));
        } else if (source == alwaysLookCheckBoxMenuItem) {
            boolean alwaysLook = alwaysLookCheckBoxMenuItem.isSelected();
            ui.handler.setAlwaysLook(alwaysLook);
            Utils.setPref("always-look", Boolean.toString(alwaysLook));
        } else if (source == promptToQuitCheckBoxMenuItem) {
            Utils.setPref("prompt-to-quit", Boolean.toString(promptToQuitCheckBoxMenuItem.isSelected()));
        } else if (source == scrollbackMenuItem) {
            int newval = Utils.parseInt(ui.showPromptDialogImpl("Scrollback",
                "Scrollback buffer size, in characters:", "Size (>= 1000):", Integer.toString(ui.maxBufferSize)), 0);
            if (newval < 1000) {
                ui.showTextDialogImpl("Scrollback", "That's not a valid size!", "Sorry, I'll try again");
            } else {
                ui.maxBufferSize = newval;
                Utils.setPref("max-text-buffer-size", Integer.toString(newval));
            }
        } else if (source == chooseFontsMenuItem) {
            if (chooseFontsDialog == null)
                chooseFontsDialog = new ChooseFontsDialog(frame);
            if (chooseFontsDialog.show())
                ui.updateComponentFonts();
        } else if (source == webSiteMenuItem) {
            ui.openURL("https://jessepav.github.io/meterman2/");
        } else if (source == onlineManualMenuItem) {
            ui.openURL("http://bit.ly/meterman-manual");
        } else if (source == aboutMenuItem) {
            ui.handler.aboutMenuClicked();
        }
    }

    public void valueChanged(ListSelectionEvent e) {
        if (suppressValueChanged)
            return;
        Object source = e.getSource();
        if (source == roomList) {
            suppressValueChanged = true;
            inventoryList.clearSelection();
            suppressValueChanged = false;
            int idx = roomList.getSelectedIndex();
            ui.handler.entitySelected(idx == -1 ? null : ui.roomEntityIds.get(idx));
        } else if (source == inventoryList) {
            suppressValueChanged = true;
            roomList.clearSelection();
            suppressValueChanged = false;
            int idx = inventoryList.getSelectedIndex();
            ui.handler.entitySelected(idx == -1 ? null : ui.inventoryEntityIds.get(idx));
        }
    }

    private void actionSelected(Action a) {
        lastAction = a;
        ui.handler.entityActionSelected(a);
    }

    private class FrameImageComponent extends JComponent {
        protected void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            int cw = getWidth();
            int ch = getHeight();
            int x, y, width, height;
            if (frameImage != null) {
                int iw = frameImage.getWidth();
                int ih = frameImage.getHeight();
                int scale = Utils.clamp(cw / iw, 1, ch / ih);
                width = iw * scale;
                height = ih * scale;
                x = Math.max(0, (cw - width) / 2);
                y = Math.max(0, (ch - height) / 2);
                g2d.drawImage(frameImage, x, y, width, height, null);
            }
            if (entityImage != null) {
                int iw = entityImage.getWidth();
                int ih = entityImage.getHeight();
                y = ch / 3;
                int scale = Utils.clamp(cw / iw, 1, (ch-y) / ih);
                width = iw * scale;
                height = ih * scale;
                x = Math.max(0, (cw - width) / 2);
                g2d.drawImage(entityImage, x, y, width, height, null);
            }
        }
    }

    private class FrameWindowListener extends WindowAdapter
    {
        public void windowClosing(WindowEvent e) {
            close();
        }
    }

    private class ListMouseListener extends MouseAdapter
    {
        public void mousePressed(MouseEvent e) { checkPopup(e); }
        public void mouseReleased(MouseEvent e) { checkPopup(e); }

        public void mouseClicked(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
                if (actionButtons[0].isVisible())
                    actionButtons[0].doClick();
            }
        }

        private void checkPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                JList list = (JList) e.getSource();
                final Point p = e.getPoint();
                int idx = list.locationToIndex(p);
                if (idx == -1)
                    return;
                if (!list.getCellBounds(idx, idx).contains(p))
                    return;
                list.setSelectedIndex(idx);
                showActionPopup(list, p);
            }
        }

        private void showActionPopup(JList list, Point p) {
            if (popupMenu == null) {
                popupMenu = new JPopupMenu("Actions");
                popupItems = new ArrayList<>(16);
            }
            // ensure we have enough menu items to cover our actions
            for (int i = popupItems.size(); i < actions.size(); i++)
                popupItems.add(new JMenuItem(new PopupAction(i)));
            popupMenu.removeAll();
            for (int i = 0; i < actions.size(); i++) {
                final JMenuItem item = popupItems.get(i);
                ((PopupAction) item.getAction()).updateName();
                popupMenu.add(item);
            }
            popupMenu.show(list, p.x, p.y);
        }
    }

    private class PopupAction extends AbstractAction
    {
        private final int actionIdx;

        private PopupAction(int actionIdx) {
            this.actionIdx = actionIdx;
        }

        void updateName() {
            if (actions.size() > actionIdx)
                putValue(NAME, actions.get(actionIdx).getText());
        }

        public void actionPerformed(ActionEvent e) {
            if (actions.size() > actionIdx)
                actionSelected(actions.get(actionIdx));
        }
    }


    // Activates a button when invoked (for keyboard shortcuts)
    private class ButtonAction extends AbstractAction
    {
        AbstractButton b;

        private ButtonAction(AbstractButton b) {
            this.b = b;
        }

        public void actionPerformed(ActionEvent e) {
            if (b.isVisible())
                b.doClick();
        }
    }

    // Used in interacting with the SelectItemDialog
    private class SelectItemAction extends AbstractAction
    {
        private JList<String> entityList;
        private DefaultListModel<String> entityListModel;
        private List<Action> actionsList;
        private String header, prompt;

        private SelectItemAction(JList<String> entityList, DefaultListModel<String> entityListModel,
                                 String header, String prompt) {
            this.entityList = entityList;
            this.entityListModel = entityListModel;
            this.header = header;
            this.prompt = prompt;
        }

        private SelectItemAction(List<Action> actionsList, String header, String prompt) {
            this.actionsList = actionsList;
            this.header = header;
            this.prompt = prompt;
        }

        public void actionPerformed(ActionEvent e) {
            if (entityListModel != null && !entityListModel.isEmpty()) {
                int n = entityListModel.size();
                List<String> l = new ArrayList<>(n);
                for (int i = 0; i < n; i++)
                    l.add(entityListModel.get(i));
                int idx = ui.selectItemDialog.showSelectItemDialog(header, prompt, l, entityList.getSelectedIndex());
                if (idx != -1)
                    entityList.setSelectedIndex(idx);
            } else if (actionsList != null && !actionsList.isEmpty()) {
                int idx = ui.selectItemDialog.showSelectItemDialog(header, prompt, actionsList, -1);
                if (idx != -1)
                    actionSelected(actionsList.get(idx));
            }
        }
    }

    private class SpecialAction extends AbstractAction
    {
        private final static int AGAIN = 0;
        private final static int DEBUG = 1;

        private int actionType;
        private String debugCommand;

        private SpecialAction(int actionType) {
            this.actionType = actionType;
            if (actionType == DEBUG)
                debugCommand = "";
        }

        public void actionPerformed(ActionEvent e) {
            switch (actionType) {
            case AGAIN:
                if (lastAction != null && actions.contains(lastAction))
                    ui.handler.entityActionSelected(lastAction);
                break;
            case DEBUG:
                try {
                    if (ui.handler.isGameActive()) {
                        debugCommand = ui.showPromptDialogImpl("Debug Command",
                            "What is your debug command, oh Implementer?", "Command", debugCommand);
                        ui.handler.debugCommand(debugCommand);
                    }
                } catch (Exception ex) {
                    logger.log(Level.FINE, "debugTriggered()", ex);
                }
                break;
            }
        }
    }

    /**
     * A {@code javax.swing.Action} that invokes a {@code com.illcode.meterman2.MMActions.Action}.
     */
    private class GameAction extends AbstractAction
    {
        private final Action action;

        private GameAction(Action action) {
            this.action = action;
        }

        public void actionPerformed(ActionEvent e) {
            // Look and Wait are treated specially, since they're not entity actions.
            if (action.equals(SystemActions.LOOK))
                ui.handler.lookCommand();
            else if (action.equals(SystemActions.WAIT))
                ui.handler.waitCommand();
            else if (actions.contains(action))
                actionSelected(action);
        }
    }
}
