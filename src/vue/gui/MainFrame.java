package vue.gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;

public class MainFrame extends JFrame {

    public MainFrame() {
        setTitle("Plateforme E-sport — Gestion des Tournois");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1150, 720);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
        getContentPane().setBackground(Theme.BG_DARK);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.BG_DARK);

        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildTabs(),   BorderLayout.CENTER);
        root.add(buildFooter(), BorderLayout.SOUTH);

        setContentPane(root);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(12, 12, 24));
        header.setBorder(new EmptyBorder(12, 20, 12, 20));

        JLabel logo = new JLabel("ESPORT MANAGER");
        logo.setFont(new Font("SansSerif", Font.BOLD, 20));
        logo.setForeground(Theme.ACCENT);

        JLabel subtitle = new JLabel("Plateforme de gestion des tournois e-sport");
        subtitle.setFont(Theme.FONT_SMALL);
        subtitle.setForeground(Theme.TEXT_DIM);

        JPanel left = new JPanel(new GridLayout(2, 1));
        left.setBackground(new Color(12, 12, 24));
        left.add(logo);
        left.add(subtitle);

        header.add(left, BorderLayout.WEST);
        return header;
    }

    private JTabbedPane buildTabs() {
        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.setFont(Theme.FONT_BOLD);
        tabs.setBackground(Theme.BG_DARK);
        tabs.setForeground(Theme.TEXT);
        tabs.setBorder(new EmptyBorder(4, 4, 4, 4));

        String[] tabNames = {"Joueurs", "Équipes", "Tournois", "Matchs", "Consultation"};
        Component[] panels = {
            new PanelJoueur(), new PanelEquipe(), new PanelTournoi(),
            new PanelMatch(),  new PanelConsultation()
        };

        java.util.List<JLabel> tabLabels = new ArrayList<>();

        for (int i = 0; i < tabNames.length; i++) {
            tabs.addTab(null, panels[i]);
            JLabel lbl = makeTabLabel(tabNames[i], i == 0);
            tabLabels.add(lbl);
            tabs.setTabComponentAt(i, lbl);
        }

        tabs.addChangeListener(e -> {
            int sel = tabs.getSelectedIndex();
            for (int i = 0; i < tabLabels.size(); i++) {
                JLabel lbl = tabLabels.get(i);
                boolean active = (i == sel);
                lbl.setForeground(active ? Color.WHITE : Theme.TEXT);
                lbl.setBackground(active ? Theme.ACCENT : Theme.BG_DARK);
                lbl.setOpaque(active);
            }
        });

        return tabs;
    }

    private JLabel makeTabLabel(String text, boolean active) {
        JLabel lbl = new JLabel("  " + text + "  ");
        lbl.setFont(Theme.FONT_BOLD);
        lbl.setForeground(active ? Color.WHITE : Theme.TEXT);
        lbl.setBackground(active ? Theme.ACCENT : Theme.BG_DARK);
        lbl.setOpaque(active);
        lbl.setBorder(new EmptyBorder(6, 10, 6, 10));
        return lbl;
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setBackground(new Color(12, 12, 24));
        footer.setBorder(new EmptyBorder(4, 12, 4, 12));
        JLabel lbl = new JLabel("Projet BDD — EFREI 2025/2026");
        lbl.setFont(Theme.FONT_SMALL);
        lbl.setForeground(Theme.TEXT_DIM);
        footer.add(lbl);
        return footer;
    }
}
