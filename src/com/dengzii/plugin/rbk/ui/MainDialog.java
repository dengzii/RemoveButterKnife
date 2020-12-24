package com.dengzii.plugin.rbk.ui;

import com.dengzii.plugin.rbk.Config;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.AbstractList;
import java.util.Arrays;

public class MainDialog extends JDialog {

    JTextField insertToMethodTextField;
    JCheckBox undoWhenRemoveFailedCheckBox;
    JTextField bindViewMethodNameField;
    JTextField insertAfterTextField;
    JButton OKButton;
    JPanel contentPanel;

    private final Callback callback;

    public MainDialog(Callback callback) {
        this.callback = callback;
        setContentPane(contentPanel);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        OKButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                ok();
            }
        });
        contentPanel.registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        contentPanel.registerKeyboardAction(e -> ok(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    public static void show_(Callback callback) {
        MainDialog p = new MainDialog(callback);
        p.pack();
        p.setVisible(true);
    }

    private void ok() {
        Config.INSTANCE.setMethodNameBindView(bindViewMethodNameField.getText());
        Config.INSTANCE.setInsertBindViewMethodIntoMethod(getResult(insertToMethodTextField));
        Config.INSTANCE.setInsertCallBindViewMethodAfterCallMethod(getResult(insertAfterTextField));
        dispose();
        callback.ok();
    }

    private AbstractList<String> getResult(JTextField field) {
        String s = field.getText().replaceAll(" ", "");
        return (AbstractList<String>) Arrays.asList(s.split(","));
    }

    @Override
    public void pack() {
        super.pack();
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int w = getWidth();
        int h = getHeight();
        int x = screen.width / 2 - w / 2;
        int y = screen.height / 2 - h / 2;
        setLocation(x, y);
        setPreferredSize(new Dimension(w, h));

        setTitle("Remove ButterKnife");
    }

    public interface Callback {
        void ok();
    }
}
