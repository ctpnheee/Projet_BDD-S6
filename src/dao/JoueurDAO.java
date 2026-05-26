package dao;

import modele.Joueur;
import util.ConnexionBDD;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class JoueurDAO {

    // CRÉATION
    public boolean ajouterJoueur(Joueur j) throws SQLException {
        String sql = "INSERT INTO Joueur (pseudo, nom, prenom, date_naissance, nationalite, niveau) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = ConnexionBDD.getConnection().prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, j.getPseudo());
            ps.setString(2, j.getNom());
            ps.setString(3, j.getPrenom());
            ps.setDate(4, Date.valueOf(j.getDateNaissance()));
            ps.setString(5, j.getNationalite());
            ps.setString(6, j.getNiveau());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) j.setIdJoueur(keys.getInt(1));
                }
                return true;
            }
            return false;
        }
    }

    // LECTURE — liste complète
    public List<Joueur> listerJoueurs() throws SQLException {
        String sql = "SELECT * FROM Joueur ORDER BY pseudo";
        List<Joueur> liste = new ArrayList<>();
        try (PreparedStatement ps = ConnexionBDD.getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) liste.add(mapper(rs));
        }
        return liste;
    }

    // LECTURE — par pseudo (exact)
    public Joueur rechercherParPseudo(String pseudo) throws SQLException {
        String sql = "SELECT * FROM Joueur WHERE pseudo = ?";
        try (PreparedStatement ps = ConnexionBDD.getConnection().prepareStatement(sql)) {
            ps.setString(1, pseudo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapper(rs);
            }
        }
        return null;
    }

    // LECTURE — par mot-clé (correspondance partielle sur pseudo, nom, prénom)
    public List<Joueur> rechercherParMotCle(String motCle) throws SQLException {
        String sql = "SELECT * FROM Joueur "
                   + "WHERE pseudo LIKE ? OR nom LIKE ? OR prenom LIKE ? "
                   + "ORDER BY pseudo";
        List<Joueur> liste = new ArrayList<>();
        String pattern = "%" + motCle + "%";
        try (PreparedStatement ps = ConnexionBDD.getConnection().prepareStatement(sql)) {
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) liste.add(mapper(rs));
            }
        }
        return liste;
    }

    // LECTURE — par identifiant
    public Joueur rechercherParId(int idJoueur) throws SQLException {
        String sql = "SELECT * FROM Joueur WHERE id_joueur = ?";
        try (PreparedStatement ps = ConnexionBDD.getConnection().prepareStatement(sql)) {
            ps.setInt(1, idJoueur);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapper(rs);
            }
        }
        return null;
    }

    // MISE À JOUR
    public boolean modifierJoueur(Joueur j) throws SQLException {
        String sql = "UPDATE Joueur SET pseudo=?, nom=?, prenom=?, date_naissance=?, "
                   + "nationalite=?, niveau=? WHERE id_joueur=?";
        try (PreparedStatement ps = ConnexionBDD.getConnection().prepareStatement(sql)) {
            ps.setString(1, j.getPseudo());
            ps.setString(2, j.getNom());
            ps.setString(3, j.getPrenom());
            ps.setDate(4, Date.valueOf(j.getDateNaissance()));
            ps.setString(5, j.getNationalite());
            ps.setString(6, j.getNiveau());
            ps.setInt(7, j.getIdJoueur());
            return ps.executeUpdate() > 0;
        }
    }

    // SUPPRESSION
    public boolean supprimerJoueur(int idJoueur) throws SQLException {
        String sql = "DELETE FROM Joueur WHERE id_joueur = ?";
        try (PreparedStatement ps = ConnexionBDD.getConnection().prepareStatement(sql)) {
            ps.setInt(1, idJoueur);
            return ps.executeUpdate() > 0;
        }
    }

    // Palmarès : tournois joués + victoires (équipe vainqueur)
    public void afficherPalmares(int idJoueur) throws SQLException {
        String sql =
            "SELECT t.nom AS tournoi, j.nom AS jeu, "
            + "       t.statut, t.dotation, "
            + "       COUNT(DISTINCT m.id_match) AS matchs_joues, "
            + "       SUM(CASE WHEN m.id_equipe_vainqueur = r.id_equipe THEN 1 ELSE 0 END) AS victoires "
            + "FROM Statistique s "
            + "JOIN Match_Esport m  ON s.id_match   = m.id_match "
            + "JOIN Phase p         ON m.id_phase   = p.id_phase "
            + "JOIN Tournoi t       ON p.id_tournoi = t.id_tournoi "
            + "JOIN Jeu j           ON t.id_jeu     = j.id_jeu "
            + "JOIN Roster r        ON r.id_joueur  = s.id_joueur "
            + "                    AND r.id_jeu     = t.id_jeu "
            + "                    AND (r.id_equipe = m.id_equipe1 OR r.id_equipe = m.id_equipe2) "
            + "WHERE s.id_joueur = ? "
            + "GROUP BY t.id_tournoi "
            + "ORDER BY t.date_debut DESC";
        try (PreparedStatement ps = ConnexionBDD.getConnection().prepareStatement(sql)) {
            ps.setInt(1, idJoueur);
            try (ResultSet rs = ps.executeQuery()) {
                System.out.println();
                System.out.printf("%-30s %-12s %-10s %-12s %-8s %-8s%n",
                        "Tournoi", "Jeu", "Statut", "Dotation(€)", "Matchs", "Victoires");
                System.out.println("-".repeat(85));
                boolean found = false;
                while (rs.next()) {
                    found = true;
                    System.out.printf("%-30s %-12s %-10s %-12.0f %-8d %-8d%n",
                            rs.getString("tournoi"),
                            rs.getString("jeu"),
                            rs.getString("statut"),
                            rs.getDouble("dotation"),
                            rs.getInt("matchs_joues"),
                            rs.getInt("victoires"));
                }
                if (!found) System.out.println("  Aucun palmarès trouvé pour ce joueur.");
            }
        }
    }

    private Joueur mapper(ResultSet rs) throws SQLException {
        return new Joueur(
                rs.getInt("id_joueur"),
                rs.getString("pseudo"),
                rs.getString("nom"),
                rs.getString("prenom"),
                rs.getDate("date_naissance").toLocalDate(),
                rs.getString("nationalite"),
                rs.getString("niveau"));
    }
}
