package vue.gui;

import dao.JoueurDAO;
import modele.Joueur;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class PanelJoueur extends JPanel {

    private final JoueurDAO dao = new JoueurDAO();

    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"ID", "Pseudo", "Nom", "Prénom", "Date naissance", "Nationalité", "Niveau"}, 0);
    private final JTable   table  = Theme.styledTable(model);
    private final JTextField searchField = Theme.searchField("Rechercher par pseudo / nom...");

    public PanelJoueur() {
        setLayout(new BorderLayout(0, 0));
        setBackground(Theme.BG_PANEL);

        add(buildTop(),    BorderLayout.NORTH);
        add(Theme.scrollPane(table), BorderLayout.CENTER);
        add(buildButtons(), BorderLayout.SOUTH);

        charger();
    }

    private JPanel buildTop() {
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(Theme.BG_DARK);
        top.setBorder(new EmptyBorder(6, 10, 6, 10));

        top.add(Theme.title("Gestion des joueurs"), BorderLayout.WEST);

        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        searchBar.setBackground(Theme.BG_DARK);
        JButton btnSearch = Theme.btnNeutral("Rechercher");
        btnSearch.addActionListener(e -> rechercher());
        searchBar.add(searchField);
        searchBar.add(btnSearch);
        top.add(searchBar, BorderLayout.EAST);

        return top;
    }

    private JPanel buildButtons() {
        JButton btnAdd    = Theme.btnSuccess("+ Ajouter");
        JButton btnEdit   = Theme.btnPrimary("Modifier");
        JButton btnDel    = Theme.btnDanger("Supprimer");
        JButton btnRefresh = Theme.btnNeutral("Rafraîchir");

        btnAdd.addActionListener(e  -> dialogAjouter());
        btnEdit.addActionListener(e -> dialogModifier());
        btnDel.addActionListener(e  -> supprimer());
        btnRefresh.addActionListener(e -> charger());

        return Theme.toolBar(btnAdd, btnEdit, btnDel, btnRefresh);
    }

    private void charger() {
        try {
            List<Joueur> liste = dao.listerJoueurs();
            remplirTable(liste);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur : " + e.getMessage(), "SQL", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void rechercher() {
        String q = searchField.getText().trim();
        if (q.isEmpty() || q.equals("Rechercher par pseudo / nom...")) { charger(); return; }
        try {
            remplirTable(dao.rechercherParMotCle(q));
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur : " + e.getMessage(), "SQL", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void remplirTable(List<Joueur> liste) {
        model.setRowCount(0);
        for (Joueur j : liste) {
            model.addRow(new Object[]{
                    j.getIdJoueur(), j.getPseudo(), j.getNom(), j.getPrenom(),
                    j.getDateNaissance(), j.getNationalite(), j.getNiveau()});
        }
    }

    private void dialogAjouter() {
        JoueurForm form = new JoueurForm(null);
        int res = JOptionPane.showConfirmDialog(this, form, "Ajouter un joueur",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) return;
        try {
            Joueur j = form.build();
            if (j == null) return;
            if (dao.ajouterJoueur(j)) {
                charger();
                JOptionPane.showMessageDialog(this, "Joueur ajouté (id=" + j.getIdJoueur() + ")",
                        "Succès", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void dialogModifier() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Sélectionnez un joueur."); return; }
        int id = (int) model.getValueAt(row, 0);
        try {
            Joueur j = dao.rechercherParId(id);
            JoueurForm form = new JoueurForm(j);
            int res = JOptionPane.showConfirmDialog(this, form, "Modifier le joueur",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (res != JOptionPane.OK_OPTION) return;
            Joueur updated = form.build();
            if (updated == null) return;
            updated.setIdJoueur(id);
            if (dao.modifierJoueur(updated)) { charger(); }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void supprimer() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Sélectionnez un joueur."); return; }
        int id     = (int)    model.getValueAt(row, 0);
        String ps  = (String) model.getValueAt(row, 1);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Supprimer le joueur « " + ps + " » ?", "Confirmer",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            if (dao.supprimerJoueur(id)) charger();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Erreur SQL", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Panneau de formulaire interne
    static class JoueurForm extends JPanel {
        private final JTextField pseudo   = new JTextField(18);
        private final JTextField nom      = new JTextField(18);
        private final JTextField prenom   = new JTextField(18);
        private final JTextField dateNais = new JTextField(18);
        private final JTextField nat      = new JTextField(18);
        private final JTextField niveau   = new JTextField(18);

        JoueurForm(Joueur j) {
            setLayout(new GridBagLayout());
            setBackground(Theme.BG_PANEL);
            GridBagConstraints c = new GridBagConstraints();
            c.insets = new Insets(4, 8, 4, 8);
            c.anchor = GridBagConstraints.WEST;

            addRow(c, 0, "Pseudo *",           pseudo);
            addRow(c, 1, "Nom *",              nom);
            addRow(c, 2, "Prénom *",           prenom);
            addRow(c, 3, "Date naissance *\n(YYYY-MM-DD)", dateNais);
            addRow(c, 4, "Nationalité *",      nat);
            addRow(c, 5, "Niveau (Elo/rang) *", niveau);

            if (j != null) {
                pseudo.setText(j.getPseudo());
                nom.setText(j.getNom());
                prenom.setText(j.getPrenom());
                dateNais.setText(j.getDateNaissance().toString());
                nat.setText(j.getNationalite());
                niveau.setText(j.getNiveau());
            }
        }

        private void addRow(GridBagConstraints c, int row, String label, JTextField field) {
            c.gridx = 0; c.gridy = row;
            JLabel lbl = new JLabel(label);
            lbl.setFont(Theme.FONT_NORMAL);
            lbl.setForeground(Theme.TEXT_DIM);
            add(lbl, c);
            c.gridx = 1;
            field.setFont(Theme.FONT_NORMAL);
            add(field, c);
        }

        Joueur build() {
            if (pseudo.getText().isBlank() || nom.getText().isBlank()) {
                JOptionPane.showMessageDialog(this, "Pseudo et nom sont obligatoires.");
                return null;
            }
            try {
                return new Joueur(0,
                        pseudo.getText().trim(),
                        nom.getText().trim(),
                        prenom.getText().trim(),
                        LocalDate.parse(dateNais.getText().trim()),
                        nat.getText().trim(),
                        niveau.getText().trim());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Date invalide (format : YYYY-MM-DD).");
                return null;
            }
        }
    }
}
