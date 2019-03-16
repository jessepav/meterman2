package com.illcode.meterman2.ui;

import com.illcode.meterman2.Meterman2;
import com.illcode.meterman2.MMActions.Action;
import com.illcode.meterman2.SystemActions;
import com.illcode.meterman2.Utils;
import com.jformdesigner.model.FormModel;
import com.jformdesigner.runtime.FormCreator;
import com.jformdesigner.runtime.FormLoader;
import org.apache.commons.lang3.ArrayUtils;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import static com.illcode.meterman2.MMLogging.logger;

public final class MainFrame implements ActionListener, ListSelectionListener
{
    static final int NUM_EXIT_BUTTONS = 12;
    static final int NUM_ACTION_BUTTONS = 8;

    private static final KeyStroke DEBUG_KEYSTROKE =
        KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.ALT_MASK | InputEvent.CTRL_MASK);

    private static final KeyStroke SELECT_ROOM_ENTITY_KEYSTROKE = KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.SHIFT_MASK);
    private static final KeyStroke SELECT_INVENTORY_ENTITY_KEYSTROKE = KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.SHIFT_MASK);
    private static final KeyStroke SELECT_ACTION_KEYSTROKE = KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.SHIFT_MASK);
    private static final KeyStroke LOOK_KEYSTROKE = KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.SHIFT_MASK);
    private static final KeyStroke WAIT_KEYSTROKE = KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.SHIFT_MASK);
    private static final KeyStroke EXAMINE_KEYSTROKE = KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.SHIFT_MASK);

    private MMUI ui;

    JFrame frame;
    JMenu gameMenu, settingsMenu, helpMenu;
    JMenuItem newMenuItem, saveMenuItem, saveAsMenuItem, loadMenuItem,
        quitMenuItem, aboutMenuItem, webSiteMenuItem, onlineManualMenuItem, scrollbackMenuItem;
    JCheckBoxMenuItem alwaysLookCheckBoxMenuItem, musicCheckBoxMenuItem, soundCheckBoxMenuItem,
        promptToQuitCheckBoxMenuItem;
    JPanel imagePanel;
    JLabel roomNameLabel;
    JButton lookButton, waitButton;
    JTextArea textArea;
    JList<String> roomList, inventoryList;
    JButton[] exitButtons, actionButtons;
    JComboBox<String> moreActionCombo;
    JLabel leftStatusLabel, centerStatusLabel, rightStatusLabel;
    FrameImageComponent imageComponent;

    JFileChooser fc;
    File lastSaveFile;

    DefaultListModel<String> roomListModel, inventoryListModel;

    private BufferedImage frameImage, entityImage;
    private List<Action> actions;

    private boolean suppressValueChanged;

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
            quitMenuItem = cr.getMenuItem("quitMenuItem");
            aboutMenuItem = cr.getMenuItem("aboutMenuItem");
            webSiteMenuItem = cr.getMenuItem("webSiteMenuItem");
            onlineManualMenuItem = cr.getMenuItem("onlineManualMenuItem");
            scrollbackMenuItem = cr.getMenuItem("scrollbackMenuItem");
            alwaysLookCheckBoxMenuItem = cr.getCheckBoxMenuItem("alwaysLookCheckBoxMenuItem");
            musicCheckBoxMenuItem = cr.getCheckBoxMenuItem("musicCheckBoxMenuItem");
            soundCheckBoxMenuItem = cr.getCheckBoxMenuItem("soundCheckBoxMenuItem");
            promptToQuitCheckBoxMenuItem = cr.getCheckBoxMenuItem("promptToQuitCheckBoxMenuItem");
            imagePanel = cr.getPanel("imagePanel");
            roomNameLabel = cr.getLabel("roomNameLabel");
            lookButton = cr.getButton("lookButton");
            waitButton = cr.getButton("waitButton");
            textArea = cr.getTextArea("textArea");
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

            lookButton.setText(SystemActions.LOOK.getText());
            waitButton.setText(SystemActions.WAIT.getText());

            imageComponent = new FrameImageComponent();
            imagePanel.add(imageComponent);

            frame.getRootPane().setDoubleBuffered(true);
            frame.addWindowListener(new FrameWindowListener());

            roomListModel = new DefaultListModel<>();
            inventoryListModel = new DefaultListModel<>();
            roomList.setModel(roomListModel);
            inventoryList.setModel(inventoryListModel);

            for (AbstractButton b : new AbstractButton[] {newMenuItem, saveMenuItem, saveAsMenuItem, loadMenuItem,
                    quitMenuItem, aboutMenuItem, alwaysLookCheckBoxMenuItem, musicCheckBoxMenuItem,
                    soundCheckBoxMenuItem, promptToQuitCheckBoxMenuItem, webSiteMenuItem, scrollbackMenuItem,
                    onlineManualMenuItem, lookButton, waitButton})
                b.addActionListener(this);
            for (JButton b : exitButtons)
                b.addActionListener(this);
            for (JButton b : actionButtons)
                b.addActionListener(this);
            roomList.addListSelectionListener(this);
            inventoryList.addListSelectionListener(this);
            moreActionCombo.addActionListener(this);
            actions = new ArrayList<>(16);

            fc = new JFileChooser();
            fc.setCurrentDirectory(Meterman2.savesPath.toFile());

            installKeyBindings();
            frame.setIconImage(GuiUtils.loadOpaqueImage(Meterman2.assets.pathForSystemAsset("frame-icon.png")));

            GuiUtils.setBoundsFromPrefs(frame, "main-window-size");
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
        JRootPane root = frame.getRootPane();
        InputMap inputMap = root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = root.getActionMap();

        KeyStroke[] exitButtonKeystrokes = new KeyStroke[NUM_EXIT_BUTTONS];
        exitButtonKeystrokes[UIConstants.NW_BUTTON] = KeyStroke.getKeyStroke(KeyEvent.VK_Q, 0);
        exitButtonKeystrokes[UIConstants.N_BUTTON] = KeyStroke.getKeyStroke(KeyEvent.VK_W, 0);
        exitButtonKeystrokes[UIConstants.NE_BUTTON] = KeyStroke.getKeyStroke(KeyEvent.VK_E, 0);
        exitButtonKeystrokes[UIConstants.X1_BUTTON] = KeyStroke.getKeyStroke(KeyEvent.VK_R, 0);
        exitButtonKeystrokes[UIConstants.W_BUTTON] = KeyStroke.getKeyStroke(KeyEvent.VK_A, 0);
        exitButtonKeystrokes[UIConstants.MID_BUTTON] = KeyStroke.getKeyStroke(KeyEvent.VK_S, 0);
        exitButtonKeystrokes[UIConstants.E_BUTTON] = KeyStroke.getKeyStroke(KeyEvent.VK_D, 0);
        exitButtonKeystrokes[UIConstants.X2_BUTTON] = KeyStroke.getKeyStroke(KeyEvent.VK_F, 0);
        exitButtonKeystrokes[UIConstants.SW_BUTTON] = KeyStroke.getKeyStroke(KeyEvent.VK_Z, 0);
        exitButtonKeystrokes[UIConstants.S_BUTTON] = KeyStroke.getKeyStroke(KeyEvent.VK_X, 0);
        exitButtonKeystrokes[UIConstants.SE_BUTTON] = KeyStroke.getKeyStroke(KeyEvent.VK_C, 0);
        exitButtonKeystrokes[UIConstants.X3_BUTTON] = KeyStroke.getKeyStroke(KeyEvent.VK_V, 0);

        for (int i = 0; i < NUM_EXIT_BUTTONS; i++) {
            String actionMapKey = "exitButton:" + UIConstants.buttonPositionToText(i);
            inputMap.put(exitButtonKeystrokes[i], actionMapKey);
            actionMap.put(actionMapKey, new ButtonAction(exitButtons[i]));
        }

        inputMap.put(LOOK_KEYSTROKE, "lookButton");
        actionMap.put("lookButton", new ButtonAction(lookButton));
        inputMap.put(WAIT_KEYSTROKE, "waitButton");
        actionMap.put("waitButton", new ButtonAction(waitButton));
        inputMap.put(EXAMINE_KEYSTROKE, "examineAction");
        actionMap.put("examineAction", new AbstractAction()
        {
            public void actionPerformed(ActionEvent e) {
                if (actions.contains(SystemActions.EXAMINE))
                    ui.handler.entityActionSelected(SystemActions.EXAMINE);
            }
        });

        inputMap.put(SELECT_ROOM_ENTITY_KEYSTROKE, "selectRoomEntity");
        actionMap.put("selectRoomEntity",
            new SelectItemAction(roomList, roomListModel, "Select an object in the room", "Object:"));

        inputMap.put(SELECT_INVENTORY_ENTITY_KEYSTROKE, "selectInventoryEntity");
        actionMap.put("selectInventoryEntity",
            new SelectItemAction(inventoryList, inventoryListModel, "Select an item in your inventory", "Item:"));

        inputMap.put(SELECT_ACTION_KEYSTROKE, "selectAction");
        actionMap.put("selectAction",
            new SelectItemAction(actions, "Select an action", "Action:"));

        inputMap.put(DEBUG_KEYSTROKE, "debugCommand");
        actionMap.put("debugCommand", new AbstractAction()
        {
            public void actionPerformed(ActionEvent e) {
                try {
                    if (ui.handler.isGameActive()) {
                        String command = ui.showPromptDialog("Debug Command",
                            "What is your debug command, oh Implementer?", "Command", "");
                        ui.handler.debugCommand(command);
                    }
                } catch (Exception ex) {
                    logger.log(Level.FINE, "debugTriggered()", ex);
                }
            }
        });
    }

    void setVisible(boolean visible) {
        frame.setVisible(visible);
    }

    void startup() {
        soundCheckBoxMenuItem.setSelected(ui.handler.isSoundEnabled());
        musicCheckBoxMenuItem.setSelected(ui.handler.isMusicEnabled());
        alwaysLookCheckBoxMenuItem.setSelected(ui.handler.isAlwaysLook());
        promptToQuitCheckBoxMenuItem.setSelected(Utils.booleanPref("prompt-to-quit", true));

        ui.clearActions();
        ui.clearExits();
        ui.clearText();
        ui.setFrameImage(UIConstants.DEFAULT_FRAME_IMAGE);

        initGame:  // make the user keep selecting choices until a game is
        do {       // successfully started or loaded
            String choice;
            do {  // don't let the user avoid making a choice
                choice = ui.showListDialog("Meterman2", "Select an option",
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
                JOptionPane.showConfirmDialog(frame, "Quit Meterman2?", "Quit",
                   JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
                return;  // don't quit
        }
        Meterman2.shutdown();
    }

    void setFrameImage(BufferedImage image) {
        frameImage = image;
        imageComponent.repaint();
    }

    void setEntityImage(BufferedImage image) {
        entityImage = image;
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

    public void clearActions() {
        actions.clear();
        for (JButton b : actionButtons)
            b.setVisible(false);
        moreActionCombo.setVisible(false);
        moreActionCombo.removeAllItems();
        moreActionCombo.addItem("More...");
    }

    public void addAction(Action action) {
        if (actions.contains(action))
            return;
        actions.add(action);
        final int n = actions.size();
        if (n <= NUM_ACTION_BUTTONS) {
            JButton b = actionButtons[n - 1];
            b.setText(action.getText());
            b.setVisible(true);
        } else {
            moreActionCombo.setVisible(true);
            moreActionCombo.addItem(action.getText());
        }
    }

    public void removeAction(Action action) {
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
            ui.handler.entityActionSelected(actions.get(buttonIdx));
        } else if (source == moreActionCombo) {
            int idx = moreActionCombo.getSelectedIndex();
            if (idx > 0)   // index 0 is "More..."
                ui.handler.entityActionSelected(actions.get(idx - 1 + NUM_ACTION_BUTTONS));
        } else if (source == newMenuItem) {
            String gameName = Utils.getPref("single-game-name");
            if (gameName == null)
                gameName = ui.showListDialog("New Game", "Choose a game:", ui.handler.getGameNames(), true);
            if (gameName != null)
                ui.handler.newGame(gameName);
        } else if (source == loadMenuItem) {
            int r = fc.showOpenDialog(frame);
            if (r == JFileChooser.APPROVE_OPTION) {
                File f = fc.getSelectedFile();
                try (InputStream in = new FileInputStream(f)) {
                    ui.handler.loadGameState(in);
                } catch (Exception ex) {
                    logger.log(Level.WARNING, "MainFrame loadMenuItem", ex);
                    ui.showTextDialog("Load Error", ex.getMessage(), "OK");
                }
            }
        } else if (source == saveMenuItem) {
            if (lastSaveFile == null) {
                saveAsMenuItem.doClick();
                return;
            }
            try (OutputStream out = new FileOutputStream(lastSaveFile)) {
                ui.handler.saveGameState(out);
            } catch (Exception ex) {
                logger.log(Level.WARNING, "MainFrame saveMenuItem", ex);
                ui.showTextDialog("Save Error", ex.getMessage(), "OK");
            }
        } else if (source == saveAsMenuItem) {
            int r = fc.showSaveDialog(frame);
            if (r == JFileChooser.APPROVE_OPTION) {
                lastSaveFile = fc.getSelectedFile();
                saveMenuItem.doClick();
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
            int newval = Utils.parseInt(ui.showPromptDialog("Scrollback",
                "Scrollback buffer size, in characters:", "Size:", Integer.toString(ui.maxBufferSize)));
            if (newval == 0) {
                ui.showTextDialog("Scrollback", "That's not a valid size!", "Sorry, I'll try again");
            } else {
                ui.maxBufferSize = newval;
                Utils.setPref("max-text-buffer-size", Integer.toString(newval));
            }
        } else if (source == webSiteMenuItem) {
            ui.openURL("https://jessepav.github.io/meterman/");
        } else if (source == onlineManualMenuItem) {
            ui.openURL("https://jessepav.github.io/meterman/manual.html");
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
                    ui.handler.entityActionSelected(actionsList.get(idx));
            }
        }
    }
}
