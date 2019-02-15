package com.illcode.meterman2.ui;

import com.illcode.meterman2.Meterman2;
import com.illcode.meterman2.SystemActions;
import com.illcode.meterman2.Utils;
import com.jformdesigner.model.FormModel;
import com.jformdesigner.runtime.FormCreator;
import com.jformdesigner.runtime.FormLoader;

import static com.illcode.meterman2.MMLogging.logger;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

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

    private Meterman2UI ui;

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
    private List<String> actions;

    private boolean suppressValueChanged;

    @SuppressWarnings("unchecked")
    MainFrame(Meterman2UI ui) {
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

            lookButton.setText(SystemActions.LOOK);
            waitButton.setText(SystemActions.WAIT);

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

    private void installKeyBindings() {

    }


    void setVisible(boolean visible) {

    }

    void startup() {

    }

    private void close() {
        if (Utils.booleanPref("prompt-to-quit", true)) {
            if (/*TODO: Meterman2.gm.getGame() != null &&*/
                JOptionPane.showConfirmDialog(frame, "Quit Meterman?", "Quit",
                   JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
                return;  // don't quit
        }
        Meterman2.shutdown();
    }

    public void actionPerformed(ActionEvent e) {

    }

    public void valueChanged(ListSelectionEvent e) {

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
}
