package com.illcode.meterman2.ui;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextArea;

import com.jformdesigner.model.FormModel;
import com.jformdesigner.runtime.FormCreator;
import com.jformdesigner.runtime.FormLoader;
import org.apache.commons.lang3.ArrayUtils;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;

import static com.illcode.meterman2.MMLogging.logger;

class TextDialog implements ActionListener
{
    protected static String[] CLOSE_BUTTON_ARRAY = new String[] {"Close"};

    Window owner;

    JDialog dialog;
    JLabel headerLabel;
    JTextArea textArea;
    JButton[] buttons;

    int selectedButtonIdx;

    TextDialog() {
    }

    TextDialog(Window owner) {
        this.owner = owner;
        try {
            FormModel formModel = FormLoader.load("com/illcode/meterman2/ui/TextDialog.jfd");
            FormCreator cr = new FormCreator(formModel);

            dialog = (JDialog) cr.createWindow(owner);
            headerLabel = cr.getLabel("headerLabel");
            textArea = cr.getTextArea("textArea");
            buttons = new JButton[3];
            buttons[0] = cr.getButton("button1");
            buttons[1] = cr.getButton("button2");
            buttons[2] = cr.getButton("button3");

            for (JButton b : buttons)
                b.addActionListener(this);
        } catch (Exception ex) {
            logger.log(Level.WARNING, "TextDialog()", ex);
        }
    }

    int show(String header, String text, String... buttonLabels) {
        headerLabel.setText(header);
        textArea.setText(text);
        setButtonsText(buttonLabels);
        dialog.pack();
        dialog.setLocationRelativeTo(owner);
        requestButtonFocus();
        selectedButtonIdx = -1;
        dialog.setVisible(true);  // blocks until hidden
        return selectedButtonIdx;
    }

    void setButtonsText(String... labels) {
        if (labels == null || labels.length == 0)
            labels = CLOSE_BUTTON_ARRAY;
        for (int i = 0; i < buttons.length; i++) {
            String label = labels.length > i ? labels[i] : null;
            if (label != null) {
                buttons[i].setText(labels[i]);
                buttons[i].setVisible(true);
            } else {
                buttons[i].setVisible(false);
            }
        }
    }

    // Have the first visible button request focus in the window.
    void requestButtonFocus() {
        for (int i = 0; i < buttons.length; i++) {
            if (buttons[i].isVisible()) {
                buttons[i].requestFocusInWindow();
                dialog.getRootPane().setDefaultButton(buttons[i]);
                break;
            }
        }
    }

    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        int buttonIdx;
        if ((buttonIdx = ArrayUtils.indexOf(buttons, source)) != -1) {
            selectedButtonIdx = buttonIdx;
            dialog.setVisible(false);
        }
    }

    public void dispose() {
        dialog.dispose();
    }
}
