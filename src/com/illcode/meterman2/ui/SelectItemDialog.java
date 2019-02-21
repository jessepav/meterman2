package com.illcode.meterman2.ui;

import com.jformdesigner.model.FormModel;
import com.jformdesigner.runtime.FormCreator;
import com.jformdesigner.runtime.FormLoader;

import javax.swing.*;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.logging.Level;

import static com.illcode.meterman2.MMLogging.logger;

/**
 * A modal dialog that displays a list of items in a combo box for user selection.
 * It is intended for internal use by the <tt>MMUI</tt>.
 */
public final class SelectItemDialog implements ActionListener
{
    Window owner;

    JDialog dialog;
    JLabel headerLabel;
    JLabel promptLabel;
    JComboBox<String> itemCombo;
    JButton okButton, cancelButton;

    private int selectedIdx;

    @SuppressWarnings("unchecked")
    public SelectItemDialog(Window owner) {
        this.owner = owner;
        try {
            FormModel formModel = FormLoader.load("com/illcode/meterman2/ui/SelectItemDialog.jfd");
            FormCreator cr = new FormCreator(formModel);

            dialog = (JDialog) cr.createWindow(owner);
            headerLabel = cr.getLabel("headerLabel");
            promptLabel = cr.getLabel("promptLabel");
            itemCombo = cr.getComboBox("itemCombo");
            okButton = cr.getButton("okButton");
            cancelButton = cr.getButton("cancelButton");

            okButton.addActionListener(this);
            cancelButton.addActionListener(this);
            dialog.getRootPane().setDefaultButton(okButton);
            GuiUtils.attachEscapeCloseOperation(dialog);
        } catch (Exception ex) {
            logger.log(Level.WARNING, "PromptDialog()", ex);
        }
    }

    /**
     * Show the SelectItemDialog.
     * @param header header text
     * @param prompt prompt text
     * @param items items to show in a combo box
     * @param initialSelectedIdx item index to select when the dialog is shown, or -1 for no selection
     * @param <T> type of items
     * @return the index of the item the user selected, or -1 if the dialog was closed or Cancel hit
     */
    public <T> int showSelectItemDialog(String header, String prompt, List<T> items, int initialSelectedIdx) {
        headerLabel.setText(header);
        promptLabel.setText(prompt);
        itemCombo.removeAllItems();
        for (T item : items)
            itemCombo.addItem(item.toString());
        if (initialSelectedIdx != -1)
            itemCombo.setSelectedIndex(initialSelectedIdx);
        dialog.pack();
        dialog.setLocationRelativeTo(owner);
        itemCombo.requestFocusInWindow();
        selectedIdx = -1;
        dialog.setVisible(true);  // blocks until hidden
        return selectedIdx;
    }

    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == okButton) {
            selectedIdx = itemCombo.getSelectedIndex();
            dialog.setVisible(false);
        } else if (source == cancelButton) {
            dialog.setVisible(false);
        }
    }

    public void dispose() {
        dialog.dispose();
    }
}
