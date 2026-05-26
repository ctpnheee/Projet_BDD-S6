package vue.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;

public class Theme {

    // Palette — bleu/rose
    public static final Color BG_DARK    = new Color(10, 14, 35);
    public static final Color BG_PANEL   = new Color(16, 22, 52);
    public static final Color BG_TABLE   = new Color(13, 18, 42);
    public static final Color BG_ROW_ALT = new Color(22, 28, 60);
    public static final Color ACCENT     = new Color(236, 72, 153);   // rose
    public static final Color ACCENT_HOV = new Color(249, 115, 180);
    public static final Color SUCCESS    = new Color(52, 211, 153);
    public static final Color DANGER     = new Color(239, 68, 68);
    public static final Color TEXT       = new Color(226, 232, 240);
    public static final Color TEXT_DIM   = new Color(148, 163, 210);
    public static final Color BORDER     = new Color(37, 57, 120);
    public static final Color HEADER_BG  = new Color(25, 45, 100);

    public static final Font FONT_NORMAL = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font FONT_BOLD   = new Font("SansSerif", Font.BOLD,  13);
    public static final Font FONT_TITLE  = new Font("SansSerif", Font.BOLD,  15);
    public static final Font FONT_SMALL  = new Font("SansSerif", Font.PLAIN, 11);

    public static void apply() {
        UIManager.put("Panel.background",             BG_PANEL);
        UIManager.put("OptionPane.background",        new Color(16, 22, 52));
        UIManager.put("OptionPane.messageForeground", Color.WHITE);
        UIManager.put("OptionPane.foreground",        Color.WHITE);
        UIManager.put("TextField.background",         new Color(30, 40, 80));
        UIManager.put("TextField.foreground",         Color.WHITE);
        UIManager.put("TextField.caretForeground",    ACCENT);
        UIManager.put("TextField.border",
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER),
                        new EmptyBorder(4, 6, 4, 6)));
        UIManager.put("ComboBox.background",                new Color(30, 40, 90));
        UIManager.put("ComboBox.foreground",               Color.WHITE);
        UIManager.put("ComboBox.selectionBackground",      ACCENT);
        UIManager.put("ComboBox.selectionForeground",      Color.WHITE);
        UIManager.put("ComboBox.disabledForeground",       TEXT_DIM);
        UIManager.put("ComboBox.buttonBackground",         new Color(30, 40, 90));
        UIManager.put("List.background",                   new Color(20, 30, 70));
        UIManager.put("List.foreground",                   Color.WHITE);
        UIManager.put("List.selectionBackground",          ACCENT);
        UIManager.put("List.selectionForeground",          Color.WHITE);
        UIManager.put("PopupMenu.background",              new Color(20, 30, 70));
        UIManager.put("PopupMenu.foreground",              Color.WHITE);
        UIManager.put("MenuItem.background",               new Color(20, 30, 70));
        UIManager.put("MenuItem.foreground",               Color.WHITE);
        UIManager.put("MenuItem.selectionBackground",      ACCENT);
        UIManager.put("MenuItem.selectionForeground",      Color.WHITE);
        UIManager.put("Label.foreground",             TEXT);
        UIManager.put("TabbedPane.background",        BG_DARK);
        UIManager.put("TabbedPane.foreground",        TEXT);
        UIManager.put("TabbedPane.selected",          BG_PANEL);
        UIManager.put("TabbedPane.selectedForeground",ACCENT);
        UIManager.put("TabbedPane.contentBorderInsets", new Insets(2, 0, 0, 0));
        UIManager.put("ScrollPane.background",        BG_TABLE);
        UIManager.put("ScrollBar.background",         BG_DARK);
        UIManager.put("ScrollBar.thumb",              BORDER);
        UIManager.put("Table.background",             BG_TABLE);
        UIManager.put("Table.foreground",             TEXT);
        UIManager.put("Table.selectionBackground",    ACCENT);
        UIManager.put("Table.selectionForeground",    Color.WHITE);
        UIManager.put("Table.gridColor",              BORDER);
        UIManager.put("TableHeader.background",       HEADER_BG);
        UIManager.put("TableHeader.foreground",       TEXT);
        UIManager.put("Button.background",            ACCENT);
        UIManager.put("Button.foreground",            Color.WHITE);
        UIManager.put("Button.font",                  FONT_BOLD);
    }

    public static JButton button(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(FONT_BOLD);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(7, 16, 7, 16));
        return btn;
    }

    public static <E> JComboBox<E> styledCombo(JComboBox<E> cb) {
        cb.setFont(FONT_NORMAL);
        cb.setBackground(new Color(30, 40, 90));
        cb.setForeground(Color.WHITE);
        cb.setRenderer(new javax.swing.DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setBackground(isSelected ? ACCENT : new Color(20, 30, 75));
                setForeground(Color.WHITE);
                setFont(FONT_NORMAL);
                setBorder(new EmptyBorder(4, 8, 4, 8));
                return this;
            }
        });
        cb.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT, 1),
                new EmptyBorder(2, 4, 2, 4)));
        return cb;
    }

    public static JButton btnPrimary(String text)  { return button(text, ACCENT); }
    public static JButton btnSuccess(String text)  { return button(text, SUCCESS.darker()); }
    public static JButton btnDanger(String text)   { return button(text, DANGER.darker()); }
    public static JButton btnNeutral(String text)  { return button(text, new Color(60, 60, 90)); }

    public static JTable styledTable(javax.swing.table.DefaultTableModel model) {
        JTable table = new JTable(model) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }

            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? BG_TABLE : BG_ROW_ALT);
                }
                return c;
            }
        };
        table.setFont(FONT_NORMAL);
        table.setForeground(TEXT);
        table.setRowHeight(26);
        table.setShowGrid(true);
        table.setGridColor(BORDER);
        table.setSelectionBackground(ACCENT);
        table.setSelectionForeground(Color.WHITE);
        table.setIntercellSpacing(new Dimension(1, 1));
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        header.setBackground(HEADER_BG);
        header.setForeground(TEXT);
        header.setFont(FONT_BOLD);
        header.setReorderingAllowed(false);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < model.getColumnCount(); i++) {
            if (i == 0) table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        return table;
    }

    public static JScrollPane scrollPane(JTable table) {
        JScrollPane sp = new JScrollPane(table);
        sp.getViewport().setBackground(BG_TABLE);
        sp.setBorder(BorderFactory.createLineBorder(BORDER));
        return sp;
    }

    public static JLabel title(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_TITLE);
        lbl.setForeground(ACCENT);
        lbl.setBorder(new EmptyBorder(10, 12, 6, 0));
        return lbl;
    }

    public static JPanel toolBar(JButton... buttons) {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        bar.setBackground(BG_DARK);
        for (JButton b : buttons) bar.add(b);
        return bar;
    }

    // Boîte de dialogue personnalisée de sélection dans une liste — entièrement thématisée, remplace JOptionPane.showInputDialog avec un tableau.
    public static String pickFromList(Component parent, String title, String label, String[] choices) {
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(parent), title,
                java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        dlg.getContentPane().setBackground(BG_PANEL);

        JComboBox<String> cb = styledCombo(new JComboBox<>(choices));
        cb.setPreferredSize(new Dimension(320, 32));

        JLabel lbl = new JLabel(label);
        lbl.setFont(FONT_BOLD);
        lbl.setForeground(TEXT);

        JButton btnOk     = btnPrimary("OK");
        JButton btnCancel = button("Annuler", new Color(60, 60, 90));

        final String[] result = {null};
        btnOk.addActionListener(e -> { result[0] = (String) cb.getSelectedItem(); dlg.dispose(); });
        btnCancel.addActionListener(e -> dlg.dispose());

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnBar.setBackground(BG_PANEL);
        btnBar.add(btnCancel); btnBar.add(btnOk);

        JPanel content = new JPanel(new BorderLayout(0, 12));
        content.setBackground(BG_PANEL);
        content.setBorder(new EmptyBorder(20, 24, 16, 24));
        content.add(lbl, BorderLayout.NORTH);
        content.add(cb,  BorderLayout.CENTER);
        content.add(btnBar, BorderLayout.SOUTH);

        dlg.setContentPane(content);
        dlg.getRootPane().setDefaultButton(btnOk);
        dlg.pack();
        dlg.setLocationRelativeTo(parent);
        dlg.setVisible(true);
        return result[0];
    }

    // Boîte de dialogue personnalisée de saisie de texte — entièrement thématisée, remplace JOptionPane.showInputDialog avec du texte.
    public static String askText(Component parent, String title, String label) {
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(parent), title,
                java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        dlg.getContentPane().setBackground(BG_PANEL);

        JTextField tf = new JTextField(22);
        tf.setFont(FONT_NORMAL);
        tf.setBackground(new Color(30, 40, 90));
        tf.setForeground(Color.WHITE);
        tf.setCaretColor(ACCENT);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT, 1),
                new EmptyBorder(4, 8, 4, 8)));

        JLabel lbl = new JLabel(label);
        lbl.setFont(FONT_BOLD);
        lbl.setForeground(TEXT);

        JButton btnOk     = btnPrimary("OK");
        JButton btnCancel = button("Annuler", new Color(60, 60, 90));

        final String[] result = {null};
        btnOk.addActionListener(e -> { result[0] = tf.getText(); dlg.dispose(); });
        btnCancel.addActionListener(e -> dlg.dispose());

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnBar.setBackground(BG_PANEL);
        btnBar.add(btnCancel); btnBar.add(btnOk);

        JPanel content = new JPanel(new BorderLayout(0, 12));
        content.setBackground(BG_PANEL);
        content.setBorder(new EmptyBorder(20, 24, 16, 24));
        content.add(lbl, BorderLayout.NORTH);
        content.add(tf,  BorderLayout.CENTER);
        content.add(btnBar, BorderLayout.SOUTH);

        dlg.setContentPane(content);
        dlg.getRootPane().setDefaultButton(btnOk);
        dlg.pack();
        dlg.setLocationRelativeTo(parent);
        dlg.setVisible(true);
        return result[0];
    }

    public static JTextField searchField(String placeholder) {
        JTextField tf = new JTextField(20);
        tf.setFont(FONT_NORMAL);
        tf.setText(placeholder);
        tf.setForeground(TEXT_DIM);
        tf.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                if (tf.getText().equals(placeholder)) { tf.setText(""); tf.setForeground(TEXT); }
            }
            public void focusLost(java.awt.event.FocusEvent e) {
                if (tf.getText().isEmpty()) { tf.setText(placeholder); tf.setForeground(TEXT_DIM); }
            }
        });
        return tf;
    }
}
