package vue.gui;

import dao.EquipeDAO;
import dao.TournoiDAO;
import modele.Equipe;
import modele.Tournoi;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class PanelEquipe extends JPanel {

    private final EquipeDAO  equipeDAO  = new EquipeDAO();
    private final TournoiDAO tournoiDAO = new TournoiDAO();

    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"ID", "Nom", "Pays", "Date création"}, 0);
    private final JTable table = Theme.styledTable(model);
    private final JTextField searchField = Theme.searchField("Rechercher par nom / pays...");

    public PanelEquipe() {
        setLayout(new BorderLayout());
        setBackground(Theme.BG_PANEL);
        add(buildTop(), BorderLayout.NORTH);
        add(Theme.scrollPane(table), BorderLayout.CENTER);
        add(buildButtons(), BorderLayout.SOUTH);
        charger();
    }

    private JPanel buildTop() {
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(Theme.BG_DARK);
        top.setBorder(new EmptyBorder(6, 10, 6, 10));
        top.add(Theme.title("Gestion des équipes"), BorderLayout.WEST);

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
        JButton btnAdd      = Theme.btnSuccess("+ Ajouter");
        JButton btnInscrire = Theme.btnPrimary("Inscrire au tournoi");
        JButton btnStats    = Theme.btnNeutral("Voir statistiques");
        JButton btnRefresh  = Theme.btnNeutral("Rafraîchir");

        btnAdd.addActionListener(e      -> dialogAjouter());
        btnInscrire.addActionListener(e -> dialogInscrire());
        btnStats.addActionListener(e    -> voirStats());
        btnRefresh.addActionListener(e  -> charger());

        return Theme.toolBar(btnAdd, btnInscrire, btnStats, btnRefresh);
    }

    private void charger() {
        try {
            remplirTable(equipeDAO.listerEquipes());
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Erreur SQL", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void rechercher() {
        String q = searchField.getText().trim();
        if (q.isEmpty() || q.startsWith("Rechercher")) { charger(); return; }
        try {
            remplirTable(equipeDAO.rechercherParMotCle(q));
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Erreur SQL", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void remplirTable(List<Equipe> liste) {
        model.setRowCount(0);
        for (Equipe e : liste)
            model.addRow(new Object[]{e.getIdEquipe(), e.getNom(), e.getPays(), e.getDateCreation()});
    }

    private void dialogAjouter() {
        EquipeForm form = new EquipeForm();
        int res = JOptionPane.showConfirmDialog(this, form, "Ajouter une équipe",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) return;
        try {
            Equipe e = form.build();
            if (e == null) return;
            if (equipeDAO.ajouterEquipe(e)) {
                charger();
                JOptionPane.showMessageDialog(this, "Équipe ajoutée (id=" + e.getIdEquipe() + ")",
                        "Succès", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void dialogInscrire() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Sélectionnez une équipe."); return; }
        int idEquipe = (int) model.getValueAt(row, 0);
        String nomEq  = (String) model.getValueAt(row, 1);

        try {
            List<Tournoi> tournois = tournoiDAO.listerTournois();
            if (tournois.isEmpty()) { JOptionPane.showMessageDialog(this, "Aucun tournoi disponible."); return; }
            String[] noms = tournois.stream()
                    .map(t -> "[" + t.getIdTournoi() + "] " + t.getNom() + " — " + t.getNomJeu())
                    .toArray(String[]::new);
            String choix = Theme.pickFromList(this, "Inscription au tournoi",
                    "Inscrire « " + nomEq + " » au tournoi :", noms);
            if (choix == null) return;
            int idTournoi = tournois.get(java.util.Arrays.asList(noms).indexOf(choix)).getIdTournoi();
            if (equipeDAO.inscrireEquipe(idEquipe, idTournoi)) {
                JOptionPane.showMessageDialog(this, "Équipe inscrite avec succès.", "Succès", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "L'équipe est déjà inscrite à ce tournoi.", "Info", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Erreur SQL", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void voirStats() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Sélectionnez une équipe."); return; }
        int    id  = (int)    model.getValueAt(row, 0);
        String nom = (String) model.getValueAt(row, 1);

        try {
            java.sql.Connection conn = util.ConnexionBDD.getConnection();
            String sql =
                "SELECT COUNT(DISTINCT m.id_match) AS matchs_joues, "
                + "SUM(CASE WHEN m.id_equipe_vainqueur = ? THEN 1 ELSE 0 END) AS victoires, "
                + "SUM(s.nb_kills) AS total_kills, SUM(s.nb_deaths) AS total_deaths, "
                + "SUM(s.nb_assists) AS total_assists, "
                + "ROUND(AVG(s.score_performance), 2) AS moy_perf "
                + "FROM Equipe e "
                + "JOIN Match_Esport m ON (m.id_equipe1 = e.id_equipe OR m.id_equipe2 = e.id_equipe) "
                + "JOIN Roster r ON r.id_equipe = e.id_equipe "
                + "JOIN Statistique s ON s.id_joueur = r.id_joueur AND s.id_match = m.id_match "
                + "WHERE e.id_equipe = ?";
            java.sql.PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id); ps.setInt(2, id);
            java.sql.ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String msg = "<html><b>Équipe : " + nom + "</b><br><br>"
                        + "Matchs joués  : " + rs.getInt("matchs_joues") + "<br>"
                        + "Victoires     : " + rs.getInt("victoires") + "<br>"
                        + "Total kills   : " + rs.getInt("total_kills") + "<br>"
                        + "Total deaths  : " + rs.getInt("total_deaths") + "<br>"
                        + "Total assists : " + rs.getInt("total_assists") + "<br>"
                        + "Moy. perf.    : " + rs.getDouble("moy_perf") + "</html>";
                JOptionPane.showMessageDialog(this, msg, "Statistiques — " + nom, JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Aucune statistique disponible.");
            }
            rs.close(); ps.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Erreur SQL", JOptionPane.ERROR_MESSAGE);
        }
    }

    static class EquipeForm extends JPanel {
        private final JTextField nom  = new JTextField(18);
        private final JTextField pays = new JTextField(18);
        private final JTextField date = new JTextField(18);

        EquipeForm() {
            setLayout(new GridBagLayout());
            setBackground(Theme.BG_PANEL);
            GridBagConstraints c = new GridBagConstraints();
            c.insets = new Insets(4, 8, 4, 8);
            c.anchor = GridBagConstraints.WEST;
            addRow(c, 0, "Nom *",                          nom);
            addRow(c, 1, "Pays *",                         pays);
            addRow(c, 2, "Date création *\n(YYYY-MM-DD)",  date);
        }

        private void addRow(GridBagConstraints c, int row, String label, JTextField f) {
            c.gridx = 0; c.gridy = row;
            JLabel l = new JLabel(label); l.setFont(Theme.FONT_NORMAL); l.setForeground(Theme.TEXT_DIM);
            add(l, c);
            c.gridx = 1; f.setFont(Theme.FONT_NORMAL); add(f, c);
        }

        Equipe build() {
            if (nom.getText().isBlank()) {
                JOptionPane.showMessageDialog(this, "Le nom est obligatoire."); return null;
            }
            try {
                return new Equipe(0, nom.getText().trim(),
                        "",
                        LocalDate.parse(date.getText().trim()),
                        pays.getText().trim());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Date invalide (format : YYYY-MM-DD)."); return null;
            }
        }
    }
}
