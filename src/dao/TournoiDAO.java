package dao;

import modele.Phase;
import modele.Tournoi;
import util.ConnexionBDD;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TournoiDAO {

    // CRÉATION — tournoi
    public boolean creerTournoi(Tournoi t) throws SQLException {
        String sql = "INSERT INTO Tournoi (nom, date_debut, date_fin, type, dotation, statut, id_jeu) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = ConnexionBDD.getConnection().prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, t.getNom());
            ps.setDate(2, Date.valueOf(t.getDateDebut()));
            ps.setDate(3, Date.valueOf(t.getDateFin()));
            ps.setString(4, t.getType());
            ps.setBigDecimal(5, t.getDotation());
            ps.setString(6, t.getStatut());
            ps.setInt(7, t.getIdJeu());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) t.setIdTournoi(keys.getInt(1));
                }
                return true;
            }
            return false;
        }
    }

    // LECTURE — liste complète
    public List<Tournoi> listerTournois() throws SQLException {
        String sql = "SELECT t.*, j.nom AS nom_jeu FROM Tournoi t "
                   + "JOIN Jeu j ON t.id_jeu = j.id_jeu ORDER BY t.date_debut DESC";
        List<Tournoi> liste = new ArrayList<>();
        try (PreparedStatement ps = ConnexionBDD.getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Tournoi t = mapper(rs);
                t.setNomJeu(rs.getString("nom_jeu"));
                liste.add(t);
            }
        }
        return liste;
    }

    // LECTURE — par identifiant
    public Tournoi rechercherParId(int idTournoi) throws SQLException {
        String sql = "SELECT t.*, j.nom AS nom_jeu FROM Tournoi t "
                   + "JOIN Jeu j ON t.id_jeu = j.id_jeu WHERE t.id_tournoi = ?";
        try (PreparedStatement ps = ConnexionBDD.getConnection().prepareStatement(sql)) {
            ps.setInt(1, idTournoi);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Tournoi t = mapper(rs);
                    t.setNomJeu(rs.getString("nom_jeu"));
                    return t;
                }
            }
        }
        return null;
    }

    // LECTURE — phases d'un tournoi
    public List<Phase> listerPhases(int idTournoi) throws SQLException {
        String sql = "SELECT * FROM Phase WHERE id_tournoi = ? ORDER BY numero_ordre";
        List<Phase> liste = new ArrayList<>();
        try (PreparedStatement ps = ConnexionBDD.getConnection().prepareStatement(sql)) {
            ps.setInt(1, idTournoi);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    liste.add(new Phase(
                            rs.getInt("id_phase"),
                            rs.getString("libelle"),
                            rs.getInt("numero_ordre"),
                            rs.getInt("id_tournoi")));
                }
            }
        }
        return liste;
    }

    // CRÉATION — phase
    public boolean ajouterPhase(Phase p) throws SQLException {
        String sql = "INSERT INTO Phase (libelle, numero_ordre, id_tournoi) VALUES (?, ?, ?)";
        try (PreparedStatement ps = ConnexionBDD.getConnection().prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getLibelle());
            ps.setInt(2, p.getNumeroOrdre());
            ps.setInt(3, p.getIdTournoi());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) p.setIdPhase(keys.getInt(1));
                }
                return true;
            }
            return false;
        }
    }

    // Classement des équipes dans un tournoi (victoires, puis diff. de manches)
    public void afficherClassement(int idTournoi) throws SQLException {
        String sql =
            "SELECT e.nom AS equipe, "
            + "     COUNT(CASE WHEN m.id_equipe_vainqueur = e.id_equipe THEN 1 END) AS victoires, "
            + "     SUM(CASE WHEN m.id_equipe1 = e.id_equipe THEN m.score_equipe1 "
            + "              WHEN m.id_equipe2 = e.id_equipe THEN m.score_equipe2 END) AS rounds_gagnes, "
            + "     SUM(CASE WHEN m.id_equipe1 = e.id_equipe THEN m.score_equipe2 "
            + "              WHEN m.id_equipe2 = e.id_equipe THEN m.score_equipe1 END) AS rounds_perdus "
            + "FROM Participer pa "
            + "JOIN Equipe e       ON pa.id_equipe  = e.id_equipe "
            + "JOIN Phase ph       ON ph.id_tournoi = pa.id_tournoi "
            + "JOIN Match_Esport m ON m.id_phase    = ph.id_phase "
            + "                   AND (m.id_equipe1 = e.id_equipe OR m.id_equipe2 = e.id_equipe) "
            + "WHERE pa.id_tournoi = ? "
            + "GROUP BY e.id_equipe "
            + "ORDER BY victoires DESC, (rounds_gagnes - rounds_perdus) DESC";
        try (PreparedStatement ps = ConnexionBDD.getConnection().prepareStatement(sql)) {
            ps.setInt(1, idTournoi);
            try (ResultSet rs = ps.executeQuery()) {
                System.out.println();
                System.out.printf("%-4s %-25s %-10s %-12s %-12s %-6s%n",
                        "Rang", "Équipe", "Victoires", "Rds gagnés", "Rds perdus", "Diff.");
                System.out.println("-".repeat(72));
                int rang = 1;
                boolean found = false;
                while (rs.next()) {
                    found = true;
                    int gagne = rs.getInt("rounds_gagnes");
                    int perdu = rs.getInt("rounds_perdus");
                    System.out.printf("%-4d %-25s %-10d %-12d %-12d %-6d%n",
                            rang++,
                            rs.getString("equipe"),
                            rs.getInt("victoires"),
                            gagne,
                            perdu,
                            gagne - perdu);
                }
                if (!found) System.out.println("  Aucune donnée de classement disponible.");
            }
        }
    }

    private Tournoi mapper(ResultSet rs) throws SQLException {
        return new Tournoi(
                rs.getInt("id_tournoi"),
                rs.getString("nom"),
                rs.getDate("date_debut").toLocalDate(),
                rs.getDate("date_fin").toLocalDate(),
                rs.getString("type"),
                rs.getBigDecimal("dotation"),
                rs.getString("statut"),
                rs.getInt("id_jeu"));
    }
}
