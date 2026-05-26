package dao;

import modele.Match;
import modele.Statistique;
import util.ConnexionBDD;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MatchDAO {

    // CRÉATION — match
    public boolean creerMatch(Match m) throws SQLException {
        String sql = "INSERT INTO Match_Esport "
                   + "(date_match, score_equipe1, score_equipe2, id_phase, id_equipe1, id_equipe2, id_equipe_vainqueur) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = ConnexionBDD.getConnection().prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setTimestamp(1, Timestamp.valueOf(m.getDateMatch()));
            ps.setInt(2, m.getScoreEquipe1());
            ps.setInt(3, m.getScoreEquipe2());
            ps.setInt(4, m.getIdPhase());
            ps.setInt(5, m.getIdEquipe1());
            ps.setInt(6, m.getIdEquipe2());
            if (m.getIdEquipeVainqueur() != null)
                ps.setInt(7, m.getIdEquipeVainqueur());
            else
                ps.setNull(7, Types.INTEGER);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) m.setIdMatch(keys.getInt(1));
                }
                return true;
            }
            return false;
        }
    }

    // MISE À JOUR — saisir / mettre à jour le résultat d'un match existant
    public boolean saisirResultat(int idMatch, int scoreE1, int scoreE2,
                                  Integer idVainqueur) throws SQLException {
        String sql = "UPDATE Match_Esport SET score_equipe1=?, score_equipe2=?, "
                   + "id_equipe_vainqueur=? WHERE id_match=?";
        try (PreparedStatement ps = ConnexionBDD.getConnection().prepareStatement(sql)) {
            ps.setInt(1, scoreE1);
            ps.setInt(2, scoreE2);
            if (idVainqueur != null) ps.setInt(3, idVainqueur);
            else ps.setNull(3, Types.INTEGER);
            ps.setInt(4, idMatch);
            return ps.executeUpdate() > 0;
        }
    }

    // CRÉATION / MISE À JOUR — statistiques d'un joueur pour un match
    public boolean saisirStatistiques(Statistique s) throws SQLException {
        String sql =
            "INSERT INTO Statistique (id_joueur, id_match, nb_kills, nb_deaths, nb_assists, score_performance) "
            + "VALUES (?, ?, ?, ?, ?, ?) "
            + "ON DUPLICATE KEY UPDATE "
            + "nb_kills=VALUES(nb_kills), nb_deaths=VALUES(nb_deaths), "
            + "nb_assists=VALUES(nb_assists), score_performance=VALUES(score_performance)";
        try (PreparedStatement ps = ConnexionBDD.getConnection().prepareStatement(sql)) {
            ps.setInt(1, s.getIdJoueur());
            ps.setInt(2, s.getIdMatch());
            ps.setInt(3, s.getNbKills());
            ps.setInt(4, s.getNbDeaths());
            ps.setInt(5, s.getNbAssists());
            ps.setBigDecimal(6, s.getScorePerformance());
            return ps.executeUpdate() > 0;
        }
    }

    // LECTURE — matchs d'un tournoi
    public List<Match> listerMatchsTournoi(int idTournoi) throws SQLException {
        String sql =
            "SELECT m.*, "
            + "     e1.nom AS nom_e1, e2.nom AS nom_e2, "
            + "     COALESCE(ev.nom, 'En cours') AS nom_vainqueur, "
            + "     ph.libelle AS libelle_phase "
            + "FROM Match_Esport m "
            + "JOIN Phase ph     ON m.id_phase   = ph.id_phase "
            + "JOIN Equipe e1    ON m.id_equipe1 = e1.id_equipe "
            + "JOIN Equipe e2    ON m.id_equipe2 = e2.id_equipe "
            + "LEFT JOIN Equipe ev ON m.id_equipe_vainqueur = ev.id_equipe "
            + "WHERE ph.id_tournoi = ? "
            + "ORDER BY ph.numero_ordre, m.date_match";
        List<Match> liste = new ArrayList<>();
        try (PreparedStatement ps = ConnexionBDD.getConnection().prepareStatement(sql)) {
            ps.setInt(1, idTournoi);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Match m = mapperMatch(rs);
                    m.setNomEquipe1(rs.getString("nom_e1"));
                    m.setNomEquipe2(rs.getString("nom_e2"));
                    m.setNomVainqueur(rs.getString("nom_vainqueur"));
                    m.setLibellePhase(rs.getString("libelle_phase"));
                    liste.add(m);
                }
            }
        }
        return liste;
    }

    // LECTURE — matchs d'une phase
    public List<Match> listerMatchsPhase(int idPhase) throws SQLException {
        String sql =
            "SELECT m.*, "
            + "     e1.nom AS nom_e1, e2.nom AS nom_e2, "
            + "     COALESCE(ev.nom, 'En cours') AS nom_vainqueur, "
            + "     ph.libelle AS libelle_phase "
            + "FROM Match_Esport m "
            + "JOIN Phase ph     ON m.id_phase   = ph.id_phase "
            + "JOIN Equipe e1    ON m.id_equipe1 = e1.id_equipe "
            + "JOIN Equipe e2    ON m.id_equipe2 = e2.id_equipe "
            + "LEFT JOIN Equipe ev ON m.id_equipe_vainqueur = ev.id_equipe "
            + "WHERE m.id_phase = ? "
            + "ORDER BY m.date_match";
        List<Match> liste = new ArrayList<>();
        try (PreparedStatement ps = ConnexionBDD.getConnection().prepareStatement(sql)) {
            ps.setInt(1, idPhase);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Match m = mapperMatch(rs);
                    m.setNomEquipe1(rs.getString("nom_e1"));
                    m.setNomEquipe2(rs.getString("nom_e2"));
                    m.setNomVainqueur(rs.getString("nom_vainqueur"));
                    m.setLibellePhase(rs.getString("libelle_phase"));
                    liste.add(m);
                }
            }
        }
        return liste;
    }

    // LECTURE — statistiques d'un match (tous les joueurs)
    public List<Statistique> listerStatistiquesMatch(int idMatch) throws SQLException {
        String sql =
            "SELECT s.*, j.pseudo "
            + "FROM Statistique s "
            + "JOIN Joueur j ON s.id_joueur = j.id_joueur "
            + "WHERE s.id_match = ? "
            + "ORDER BY s.score_performance DESC";
        List<Statistique> liste = new ArrayList<>();
        try (PreparedStatement ps = ConnexionBDD.getConnection().prepareStatement(sql)) {
            ps.setInt(1, idMatch);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Statistique s = new Statistique(
                            rs.getInt("id_joueur"),
                            rs.getInt("id_match"),
                            rs.getInt("nb_kills"),
                            rs.getInt("nb_deaths"),
                            rs.getInt("nb_assists"),
                            rs.getBigDecimal("score_performance"));
                    s.setPseudoJoueur(rs.getString("pseudo"));
                    liste.add(s);
                }
            }
        }
        return liste;
    }

    // LECTURE — match par identifiant
    public Match rechercherParId(int idMatch) throws SQLException {
        String sql =
            "SELECT m.*, "
            + "     e1.nom AS nom_e1, e2.nom AS nom_e2, "
            + "     COALESCE(ev.nom, 'En cours') AS nom_vainqueur, "
            + "     ph.libelle AS libelle_phase "
            + "FROM Match_Esport m "
            + "JOIN Phase ph       ON m.id_phase   = ph.id_phase "
            + "JOIN Equipe e1      ON m.id_equipe1 = e1.id_equipe "
            + "JOIN Equipe e2      ON m.id_equipe2 = e2.id_equipe "
            + "LEFT JOIN Equipe ev ON m.id_equipe_vainqueur = ev.id_equipe "
            + "WHERE m.id_match = ?";
        try (PreparedStatement ps = ConnexionBDD.getConnection().prepareStatement(sql)) {
            ps.setInt(1, idMatch);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Match m = mapperMatch(rs);
                    m.setNomEquipe1(rs.getString("nom_e1"));
                    m.setNomEquipe2(rs.getString("nom_e2"));
                    m.setNomVainqueur(rs.getString("nom_vainqueur"));
                    m.setLibellePhase(rs.getString("libelle_phase"));
                    return m;
                }
            }
        }
        return null;
    }

    private Match mapperMatch(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("date_match");
        Integer vainqueur = rs.getObject("id_equipe_vainqueur") != null
                ? rs.getInt("id_equipe_vainqueur") : null;
        return new Match(
                rs.getInt("id_match"),
                ts != null ? ts.toLocalDateTime() : LocalDateTime.now(),
                rs.getInt("score_equipe1"),
                rs.getInt("score_equipe2"),
                rs.getInt("id_phase"),
                rs.getInt("id_equipe1"),
                rs.getInt("id_equipe2"),
                vainqueur);
    }
}
