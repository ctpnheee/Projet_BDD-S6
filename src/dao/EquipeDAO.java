package dao;

import modele.Equipe;
import util.ConnexionBDD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EquipeDAO {

    // Ajouter une équipe
    public boolean ajouterEquipe(Equipe e) throws SQLException {
        String sql = "INSERT INTO Equipe (nom, chemin_logo, date_creation, pays) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = ConnexionBDD.getConnection().prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, e.getNom());
            ps.setString(2, e.getCheminLogo());
            ps.setDate(3, Date.valueOf(e.getDateCreation()));
            ps.setString(4, e.getPays());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) e.setIdEquipe(keys.getInt(1));
                }
                return true;
            }
            return false;
        }
    }

    // Lister toutes les équipes
    public List<Equipe> listerEquipes() throws SQLException {
        String sql = "SELECT * FROM Equipe ORDER BY nom";
        List<Equipe> liste = new ArrayList<>();
        try (PreparedStatement ps = ConnexionBDD.getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) liste.add(mapper(rs));
        }
        return liste;
    }

    // Rechercher une équipe par ID
    public Equipe rechercherParId(int idEquipe) throws SQLException {
        String sql = "SELECT * FROM Equipe WHERE id_equipe = ?";
        try (PreparedStatement ps = ConnexionBDD.getConnection().prepareStatement(sql)) {
            ps.setInt(1, idEquipe);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapper(rs);
            }
        }
        return null;
    }

    // Rechercher une équipe par mot-clé (nom / pays)
    public List<Equipe> rechercherParMotCle(String motCle) throws SQLException {
        String sql = "SELECT * FROM Equipe WHERE nom LIKE ? OR pays LIKE ? ORDER BY nom";
        List<Equipe> liste = new ArrayList<>();
        String pattern = "%" + motCle + "%";
        try (PreparedStatement ps = ConnexionBDD.getConnection().prepareStatement(sql)) {
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) liste.add(mapper(rs));
            }
        }
        return liste;
    }

    // Inscrire une équipe à un tournoi
    public boolean inscrireEquipe(int idEquipe, int idTournoi) throws SQLException {
        String sql = "INSERT IGNORE INTO Participer (id_equipe, id_tournoi) VALUES (?, ?)";
        try (PreparedStatement ps = ConnexionBDD.getConnection().prepareStatement(sql)) {
            ps.setInt(1, idEquipe);
            ps.setInt(2, idTournoi);
            return ps.executeUpdate() > 0;
        }
    }

    // Statistiques globales d'une équipe (éliminations, morts, assistances agrégées)
    public void afficherStatsEquipe(int idEquipe) throws SQLException {
        String sql =
            "SELECT e.nom AS equipe, "
            + "       COUNT(DISTINCT m.id_match) AS matchs_joues, "
            + "       SUM(CASE WHEN m.id_equipe_vainqueur = ? THEN 1 ELSE 0 END) AS victoires, "
            + "       SUM(s.nb_kills)   AS total_kills, "
            + "       SUM(s.nb_deaths)  AS total_deaths, "
            + "       SUM(s.nb_assists) AS total_assists, "
            + "       ROUND(AVG(s.score_performance), 2) AS moy_perf "
            + "FROM Equipe e "
            + "JOIN Match_Esport m ON (m.id_equipe1 = e.id_equipe OR m.id_equipe2 = e.id_equipe) "
            + "JOIN Roster r       ON r.id_equipe = e.id_equipe "
            + "JOIN Statistique s  ON s.id_joueur = r.id_joueur AND s.id_match = m.id_match "
            + "WHERE e.id_equipe = ? "
            + "GROUP BY e.id_equipe";
        try (PreparedStatement ps = ConnexionBDD.getConnection().prepareStatement(sql)) {
            ps.setInt(1, idEquipe);
            ps.setInt(2, idEquipe);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    System.out.println();
                    System.out.println("  Équipe        : " + rs.getString("equipe"));
                    System.out.println("  Matchs joués  : " + rs.getInt("matchs_joues"));
                    System.out.println("  Victoires     : " + rs.getInt("victoires"));
                    System.out.println("  Total kills   : " + rs.getInt("total_kills"));
                    System.out.println("  Total deaths  : " + rs.getInt("total_deaths"));
                    System.out.println("  Total assists : " + rs.getInt("total_assists"));
                    System.out.println("  Moy. perf.    : " + rs.getDouble("moy_perf"));
                } else {
                    System.out.println("  Aucune statistique disponible pour cette équipe.");
                }
            }
        }
    }

    // Mapper un résultat de la base de données vers un objet Equipe
    private Equipe mapper(ResultSet rs) throws SQLException {
        return new Equipe(
                rs.getInt("id_equipe"),
                rs.getString("nom"),
                rs.getString("chemin_logo"),
                rs.getDate("date_creation").toLocalDate(),
                rs.getString("pays"));
    }
}
