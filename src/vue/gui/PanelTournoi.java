package vue.gui;

import dao.TournoiDAO;
import modele.Phase;
import modele.Tournoi;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class PanelTournoi extends JPanel {

    private final TournoiDAO dao = new TournoiDAO();

    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"ID", "Nom", "Jeu", "Type", "Début", "Fin", "Dotation (€)", "Statut"}, 0);
    private final JTable table = Theme.styledTable(model);

    public PanelTournoi() {
        setLayout(new BorderLayout());
        setBackground(Theme.BG_PANEL);
        add(buildTop(),              BorderLayout.NORTH);
        add(Theme.scrollPane(table), BorderLayout.CENTER);
        add(buildButtons(),          BorderLayout.SOUTH);
        charger();
    }

    private JPanel buildTop() {
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(Theme.BG_DARK);
        top.setBorder(new EmptyBorder(6, 10, 6, 10));
        top.add(Theme.title("Gestion des tournois"), BorderLayout.WEST);
        return top;
    }

    private JPanel buildButtons() {
        JButton btnCreate   = Theme.btnSuccess("+ Créer tournoi");
        JButton btnPhases   = Theme.btnPrimary("Gérer phases");
        JButton btnClass    = Theme.btnNeutral("Classement");
        JButton btnRefresh  = Theme.btnNeutral("Rafraîchir");

        btnCreate.addActionListener(e  -> dialogCreer());
        btnPhases.addActionListener(e  -> dialogPhases());
        btnClass.addActionListener(e   -> voirClassement());
        btnRefresh.addActionListener(e -> charger());

        return Theme.toolBar(btnCreate, btnPhases, btnClass, btnRefresh);
    }

    private void charger() {
        try {
            List<Tournoi> liste = dao.listerTournois();
            model.setRowCount(0);
            for (Tournoi t : liste)
                model.addRow(new Object[]{
                        t.getIdTournoi(), t.getNom(), t.getNomJeu(),
                        t.getType(), t.getDateDebut(), t.getDateFin(),
                        t.getDotation(), t.getStatut()});
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Erreur SQL", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void dialogCreer() {
        TournoiForm form = new TournoiForm();
        int res = JOptionPane.showConfirmDialog(this, form, "Créer un tournoi",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) return;
        try {
            Tournoi t = form.build();
            if (t == null) return;
            if (dao.creerTournoi(t)) {
                charger();
                // Proposer d'ajouter des phases immédiatement
                int addPhases = JOptionPane.showConfirmDialog(this,
                        "Tournoi créé (id=" + t.getIdTournoi() + ").\nAjouter les phases maintenant ?",
                        "Phases", JOptionPane.YES_NO_OPTION);
                if (addPhases == JOptionPane.YES_OPTION) ouvrirDialogPhases(t.getIdTournoi(), t.getNom());
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void dialogPhases() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Sélectionnez un tournoi."); return; }
        int    id  = (int)    model.getValueAt(row, 0);
        String nom = (String) model.getValueAt(row, 1);
        ouvrirDialogPhases(id, nom);
    }

    private void ouvrirDialogPhases(int idTournoi, String nomTournoi) {
        try {
            List<Phase> phases = dao.listerPhases(idTournoi);

            DefaultTableModel pm = new DefaultTableModel(
                    new String[]{"ID", "Libellé", "Ordre"}, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };
            for (Phase p : phases)
                pm.addRow(new Object[]{p.getIdPhase(), p.getLibelle(), p.getNumeroOrdre()});
            JTable phaseTable = Theme.styledTable(pm);

            JPanel panel = new JPanel(new BorderLayout(0, 6));
            panel.setBackground(Theme.BG_PANEL);
            panel.setPreferredSize(new Dimension(400, 250));
            panel.add(new JLabel("Phases du tournoi : " + nomTournoi), BorderLayout.NORTH);
            panel.add(Theme.scrollPane(phaseTable), BorderLayout.CENTER);

            JPanel addRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
            addRow.setBackground(Theme.BG_PANEL);
            JTextField libField = new JTextField(15);
            libField.setFont(Theme.FONT_NORMAL);
            JButton btnAdd = Theme.btnSuccess("Ajouter");
            btnAdd.addActionListener(e -> {
                String lib = libField.getText().trim();
                if (lib.isEmpty()) return;
                try {
                    int ordre = pm.getRowCount() + 1;
                    Phase p = new Phase(0, lib, ordre, idTournoi);
                    if (dao.ajouterPhase(p)) {
                        pm.addRow(new Object[]{p.getIdPhase(), lib, ordre});
                        libField.setText("");
                    }
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(panel, ex.getMessage());
                }
            });
            addRow.add(new JLabel("Libellé :")); addRow.add(libField); addRow.add(btnAdd);
            panel.add(addRow, BorderLayout.SOUTH);

            JOptionPane.showMessageDialog(this, panel, "Phases — " + nomTournoi, JOptionPane.PLAIN_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Erreur SQL", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void voirClassement() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Sélectionnez un tournoi."); return; }
        int    id  = (int)    model.getValueAt(row, 0);
        String nom = (String) model.getValueAt(row, 1);

        try {
            String sql =
                "SELECT e.nom AS equipe, "
                + "COUNT(CASE WHEN m.id_equipe_vainqueur = e.id_equipe THEN 1 END) AS victoires, "
                + "SUM(CASE WHEN m.id_equipe1 = e.id_equipe THEN m.score_equipe1 "
                + "         WHEN m.id_equipe2 = e.id_equipe THEN m.score_equipe2 END) AS rg, "
                + "SUM(CASE WHEN m.id_equipe1 = e.id_equipe THEN m.score_equipe2 "
                + "         WHEN m.id_equipe2 = e.id_equipe THEN m.score_equipe1 END) AS rp "
                + "FROM Participer pa "
                + "JOIN Equipe e ON pa.id_equipe = e.id_equipe "
                + "JOIN Phase ph ON ph.id_tournoi = pa.id_tournoi "
                + "JOIN Match_Esport m ON m.id_phase = ph.id_phase "
                + "  AND (m.id_equipe1 = e.id_equipe OR m.id_equipe2 = e.id_equipe) "
                + "WHERE pa.id_tournoi = ? "
                + "GROUP BY e.id_equipe ORDER BY victoires DESC, (rg - rp) DESC";

            java.sql.PreparedStatement ps = util.ConnexionBDD.getConnection().prepareStatement(sql);
            ps.setInt(1, id);
            java.sql.ResultSet rs = ps.executeQuery();

            DefaultTableModel cm = new DefaultTableModel(
                    new String[]{"Rang", "Équipe", "Victoires", "Rds +", "Rds -", "Diff."}, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };
            int rang = 1;
            while (rs.next()) {
                int rg = rs.getInt("rg"), rp = rs.getInt("rp");
                cm.addRow(new Object[]{rang++, rs.getString("equipe"),
                        rs.getInt("victoires"), rg, rp, rg - rp});
            }
            rs.close(); ps.close();

            JTable ct = Theme.styledTable(cm);
            JPanel p = new JPanel(new BorderLayout());
            p.setPreferredSize(new Dimension(500, 250));
            p.add(Theme.scrollPane(ct));
            JOptionPane.showMessageDialog(this, p, "Classement — " + nom, JOptionPane.PLAIN_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Erreur SQL", JOptionPane.ERROR_MESSAGE);
        }
    }

    static class TournoiForm extends JPanel {
        private final JTextField nom      = new JTextField(18);
        private final JTextField dateD    = new JTextField(18);
        private final JTextField dateF    = new JTextField(18);
        private final JComboBox<String> type = new JComboBox<>(new String[]{"en_ligne", "LAN"});
        private final JTextField dotation = new JTextField(18);
        private final JComboBox<String> statut = new JComboBox<>(
                new String[]{"a_venir", "en_cours", "termine"});
        private final JTextField idJeu    = new JTextField(18);

        TournoiForm() {
            setLayout(new GridBagLayout());
            setBackground(Theme.BG_PANEL);
            GridBagConstraints c = new GridBagConstraints();
            c.insets = new Insets(4, 8, 4, 8);
            c.anchor = GridBagConstraints.WEST;
            addRow(c, 0, "Nom *",           nom);
            addRow(c, 1, "Date début *\n(YYYY-MM-DD)", dateD);
            addRow(c, 2, "Date fin *\n(YYYY-MM-DD)",   dateF);
            addCombo(c, 3, "Type *",         type);
            addRow(c, 4, "Dotation (€) *",  dotation);
            addCombo(c, 5, "Statut *",       statut);
            addRow(c, 6, "Id du jeu *",      idJeu);
        }

        private void addRow(GridBagConstraints c, int row, String label, JTextField f) {
            c.gridx = 0; c.gridy = row;
            JLabel l = new JLabel(label); l.setFont(Theme.FONT_NORMAL); l.setForeground(Theme.TEXT_DIM);
            add(l, c); c.gridx = 1; f.setFont(Theme.FONT_NORMAL); add(f, c);
        }
        private void addCombo(GridBagConstraints c, int row, String label, JComboBox<?> cb) {
            c.gridx = 0; c.gridy = row;
            JLabel l = new JLabel(label); l.setFont(Theme.FONT_NORMAL); l.setForeground(Theme.TEXT_DIM);
            add(l, c); c.gridx = 1; Theme.styledCombo(cb); add(cb, c);
        }

        Tournoi build() {
            if (nom.getText().isBlank()) {
                JOptionPane.showMessageDialog(this, "Nom obligatoire."); return null;
            }
            try {
                return new Tournoi(0,
                        nom.getText().trim(),
                        LocalDate.parse(dateD.getText().trim()),
                        LocalDate.parse(dateF.getText().trim()),
                        (String) type.getSelectedItem(),
                        new BigDecimal(dotation.getText().trim()),
                        (String) statut.getSelectedItem(),
                        Integer.parseInt(idJeu.getText().trim()));
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Valeur invalide : " + e.getMessage()); return null;
            }
        }
    }
}
