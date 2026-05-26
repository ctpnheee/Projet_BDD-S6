package vue.gui;

import dao.EquipeDAO;
import dao.JoueurDAO;
import dao.TournoiDAO;
import modele.Equipe;
import modele.Joueur;
import modele.Tournoi;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class PanelConsultation extends JPanel {

    private final JoueurDAO  joueurDAO  = new JoueurDAO();
    private final EquipeDAO  equipeDAO  = new EquipeDAO();
    private final TournoiDAO tournoiDAO = new TournoiDAO();

    // Tableau d'affichage central
    private final DefaultTableModel model = new DefaultTableModel(new String[]{"—"}, 0);
    private final JTable table = Theme.styledTable(model);
    private final JLabel labelResult = new JLabel("Sélectionnez une action ci-dessus.");

    public PanelConsultation() {
        setLayout(new BorderLayout(0, 0));
        setBackground(Theme.BG_PANEL);
        add(buildTop(),    BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);
    }

    private JPanel buildTop() {
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(Theme.BG_DARK);
        top.setBorder(new EmptyBorder(6, 10, 6, 10));
        top.add(Theme.title("Consultation & statistiques"), BorderLayout.WEST);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        btns.setBackground(Theme.BG_DARK);

        JButton btnClassement = Theme.btnPrimary("Classement tournoi");
        JButton btnPalmares   = Theme.btnNeutral("Palmarès joueur");
        JButton btnSearch     = Theme.btnNeutral("Recherche joueurs");
        JButton btnTop        = Theme.btnNeutral("Top kills / jeu");

        btnClassement.addActionListener(e -> classementTournoi());
        btnPalmares.addActionListener(e   -> palmareesJoueur());
        btnSearch.addActionListener(e     -> rechercherJoueur());
        btnTop.addActionListener(e        -> topKillsParJeu());

        btns.add(btnClassement);
        btns.add(btnPalmares);
        btns.add(btnSearch);
        btns.add(btnTop);
        top.add(btns, BorderLayout.EAST);
        return top;
    }

    private JPanel buildCenter() {
        JPanel center = new JPanel(new BorderLayout(0, 4));
        center.setBackground(Theme.BG_PANEL);
        center.setBorder(new EmptyBorder(8, 10, 8, 10));

        labelResult.setFont(Theme.FONT_BOLD);
        labelResult.setForeground(Theme.TEXT_DIM);
        labelResult.setBorder(new EmptyBorder(4, 0, 6, 0));
        center.add(labelResult, BorderLayout.NORTH);
        center.add(Theme.scrollPane(table), BorderLayout.CENTER);
        return center;
    }

    private void setTableData(String title, String[] columns, Object[][] rows) {
        labelResult.setText(title);
        labelResult.setForeground(Theme.ACCENT);
        DefaultTableModel m = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        for (Object[] row : rows) m.addRow(row);

        // Reconstruire le tableau sur place
        table.setModel(m);
        Theme.styledTable(m); // réappliquer le style de l'en-tête
        JTableHeader header = table.getTableHeader();
        header.setBackground(Theme.HEADER_BG);
        header.setForeground(Theme.TEXT);
        header.setFont(Theme.FONT_BOLD);
    }

    // 1. Classement d'un tournoi
    private void classementTournoi() {
        try {
            List<Tournoi> tournois = tournoiDAO.listerTournois();
            if (tournois.isEmpty()) { JOptionPane.showMessageDialog(this, "Aucun tournoi en base."); return; }
            String[] choices = tournois.stream()
                    .map(t -> "[" + t.getIdTournoi() + "] " + t.getNom())
                    .toArray(String[]::new);
            String choice = Theme.pickFromList(this, "Classement", "Sélectionnez le tournoi :", choices);
            if (choice == null) return;
            int idT = tournois.get(java.util.Arrays.asList(choices).indexOf(choice)).getIdTournoi();

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
                + "GROUP BY e.id_equipe ORDER BY victoires DESC, (rg-rp) DESC";

            PreparedStatement ps = util.ConnexionBDD.getConnection().prepareStatement(sql);
            ps.setInt(1, idT);
            ResultSet rs = ps.executeQuery();
            java.util.List<Object[]> rows = new java.util.ArrayList<>();
            int rang = 1;
            while (rs.next()) {
                int rg = rs.getInt("rg"), rp = rs.getInt("rp");
                rows.add(new Object[]{rang++, rs.getString("equipe"),
                        rs.getInt("victoires"), rg, rp, rg - rp});
            }
            rs.close(); ps.close();
            setTableData("Classement — " + choice,
                    new String[]{"Rang", "Équipe", "Victoires", "Rds +", "Rds -", "Diff."},
                    rows.toArray(new Object[0][]));
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Erreur SQL", JOptionPane.ERROR_MESSAGE);
        }
    }

    // 2. Palmarès d'un joueur
    private void palmareesJoueur() {
        String pseudo = Theme.askText(this, "Palmarès", "Pseudo du joueur :");
        if (pseudo == null || pseudo.isBlank()) return;
        try {
            Joueur j = joueurDAO.rechercherParPseudo(pseudo.trim());
            if (j == null) { JOptionPane.showMessageDialog(this, "Joueur introuvable."); return; }

            String sql =
                "SELECT t.nom AS tournoi, jeu.nom AS jeu, t.statut, t.dotation, "
                + "COUNT(DISTINCT m.id_match) AS matchs_joues, "
                + "SUM(CASE WHEN m.id_equipe_vainqueur = r.id_equipe THEN 1 ELSE 0 END) AS victoires "
                + "FROM Statistique s "
                + "JOIN Match_Esport m  ON s.id_match   = m.id_match "
                + "JOIN Phase p         ON m.id_phase   = p.id_phase "
                + "JOIN Tournoi t       ON p.id_tournoi = t.id_tournoi "
                + "JOIN Jeu jeu         ON t.id_jeu     = jeu.id_jeu "
                + "JOIN Roster r        ON r.id_joueur  = s.id_joueur "
                + "                    AND r.id_jeu     = t.id_jeu "
                + "                    AND (r.id_equipe = m.id_equipe1 OR r.id_equipe = m.id_equipe2) "
                + "WHERE s.id_joueur = ? GROUP BY t.id_tournoi ORDER BY t.date_debut DESC";

            PreparedStatement ps = util.ConnexionBDD.getConnection().prepareStatement(sql);
            ps.setInt(1, j.getIdJoueur());
            ResultSet rs = ps.executeQuery();
            java.util.List<Object[]> rows = new java.util.ArrayList<>();
            while (rs.next())
                rows.add(new Object[]{rs.getString("tournoi"), rs.getString("jeu"),
                        rs.getString("statut"), rs.getDouble("dotation"),
                        rs.getInt("matchs_joues"), rs.getInt("victoires")});
            rs.close(); ps.close();

            if (rows.isEmpty()) { JOptionPane.showMessageDialog(this, "Aucun palmarès pour " + pseudo + "."); return; }
            setTableData("Palmarès de " + j.getPseudo() + " — " + j.getPrenom() + " " + j.getNom(),
                    new String[]{"Tournoi", "Jeu", "Statut", "Dotation (€)", "Matchs", "Victoires"},
                    rows.toArray(new Object[0][]));
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Erreur SQL", JOptionPane.ERROR_MESSAGE);
        }
    }

    // 3. Recherche joueur par mot-clé
    private void rechercherJoueur() {
        String motCle = Theme.askText(this, "Recherche joueur", "Mot-clé (pseudo / nom / prénom) :");
        if (motCle == null || motCle.isBlank()) return;
        try {
            List<Joueur> liste = joueurDAO.rechercherParMotCle(motCle.trim());
            if (liste.isEmpty()) { JOptionPane.showMessageDialog(this, "Aucun résultat pour « " + motCle + " »."); return; }
            Object[][] rows = liste.stream().map(j -> new Object[]{
                    j.getIdJoueur(), j.getPseudo(), j.getNom(), j.getPrenom(),
                    j.getNationalite(), j.getNiveau()}).toArray(Object[][]::new);
            setTableData("Résultats pour « " + motCle + " »",
                    new String[]{"ID", "Pseudo", "Nom", "Prénom", "Nationalité", "Niveau"}, rows);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Erreur SQL", JOptionPane.ERROR_MESSAGE);
        }
    }

    // 4. Meilleur joueur (éliminations) par jeu
    private void topKillsParJeu() {
        String sql =
            "SELECT jeu, pseudo, total_kills FROM ("
            + "SELECT jeu.nom AS jeu, j.pseudo, "
            + "SUM(s.nb_kills) AS total_kills, "
            + "RANK() OVER (PARTITION BY t.id_jeu ORDER BY SUM(s.nb_kills) DESC) AS rang "
            + "FROM Statistique s "
            + "JOIN Joueur j         ON s.id_joueur  = j.id_joueur "
            + "JOIN Match_Esport m   ON s.id_match   = m.id_match "
            + "JOIN Phase p          ON m.id_phase   = p.id_phase "
            + "JOIN Tournoi t        ON p.id_tournoi = t.id_tournoi "
            + "JOIN Jeu jeu          ON t.id_jeu     = jeu.id_jeu "
            + "GROUP BY j.id_joueur, t.id_jeu, jeu.nom"
            + ") ranked WHERE rang = 1 ORDER BY jeu";
        try {
            PreparedStatement ps = util.ConnexionBDD.getConnection().prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            java.util.List<Object[]> rows = new java.util.ArrayList<>();
            while (rs.next())
                rows.add(new Object[]{rs.getString("jeu"), rs.getString("pseudo"), rs.getInt("total_kills")});
            rs.close(); ps.close();
            if (rows.isEmpty()) { JOptionPane.showMessageDialog(this, "Aucune donnée disponible."); return; }
            setTableData("Joueur avec le plus de kills — par jeu",
                    new String[]{"Jeu", "Joueur (pseudo)", "Total kills"},
                    rows.toArray(new Object[0][]));
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Erreur SQL", JOptionPane.ERROR_MESSAGE);
        }
    }
}
