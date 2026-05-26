package vue;

import dao.EquipeDAO;
import dao.MatchDAO;
import dao.TournoiDAO;
import modele.Match;
import modele.Phase;
import modele.Statistique;
import modele.Tournoi;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;

public class MenuMatch {

    private final MatchDAO   matchDAO   = new MatchDAO();
    private final TournoiDAO tournoiDAO = new TournoiDAO();
    private final EquipeDAO  equipeDAO  = new EquipeDAO();
    private final Scanner sc;

    public MenuMatch(Scanner sc) {
        this.sc = sc;
    }

    public void afficher() {
        boolean retour = false;
        while (!retour) {
            System.out.println();
            System.out.println("  ---- Gestion des matchs ----");
            System.out.println("  1. Créer un nouveau match");
            System.out.println("  2. Saisir / modifier le résultat d'un match");
            System.out.println("  3. Saisir les statistiques d'un joueur");
            System.out.println("  4. Afficher les matchs d'un tournoi");
            System.out.println("  5. Afficher les stats d'un match");
            System.out.println("  6. Retour");
            System.out.print("  Votre choix : ");
            String choix = sc.nextLine().trim();
            System.out.println();
            switch (choix) {
                case "1" -> creerMatch();
                case "2" -> saisirResultat();
                case "3" -> saisirStatistiques();
                case "4" -> afficherMatchsTournoi();
                case "5" -> afficherStatsMatch();
                case "6" -> retour = true;
                default  -> System.out.println("  Choix invalide.");
            }
        }
    }

    private void creerMatch() {
        System.out.println("  -- Nouveau match --");
        try {
            // Choisir le tournoi et la phase
            List<Tournoi> tournois = tournoiDAO.listerTournois();
            tournois.forEach(t -> System.out.println("  " + t));
            System.out.print("  Id du tournoi : ");
            int idTournoi = Integer.parseInt(sc.nextLine().trim());

            List<Phase> phases = tournoiDAO.listerPhases(idTournoi);
            if (phases.isEmpty()) {
                System.out.println("  Ce tournoi n'a pas encore de phases. Créez-en d'abord.");
                return;
            }
            phases.forEach(p -> System.out.println("  " + p));
            System.out.print("  Id de la phase : ");
            int idPhase = Integer.parseInt(sc.nextLine().trim());

            // Choisir les équipes
            System.out.println("  -- Équipes --");
            equipeDAO.listerEquipes().forEach(e -> System.out.println("  " + e));
            System.out.print("  Id équipe 1 : ");
            int idE1 = Integer.parseInt(sc.nextLine().trim());
            System.out.print("  Id équipe 2 : ");
            int idE2 = Integer.parseInt(sc.nextLine().trim());

            Match m = new Match();
            m.setIdPhase(idPhase);
            m.setIdEquipe1(idE1);
            m.setIdEquipe2(idE2);
            m.setDateMatch(LocalDateTime.now());
            m.setScoreEquipe1(0);
            m.setScoreEquipe2(0);

            if (matchDAO.creerMatch(m)) {
                System.out.println("  Match créé avec l'id " + m.getIdMatch());
            } else {
                System.out.println("  Échec de la création.");
            }
        } catch (NumberFormatException e) {
            System.out.println("  Id invalide.");
        } catch (SQLException e) {
            System.out.println("  Erreur SQL : " + e.getMessage());
        }
    }

    private void saisirResultat() {
        System.out.print("  Id du match : ");
        try {
            int idMatch = Integer.parseInt(sc.nextLine().trim());
            Match m = matchDAO.rechercherParId(idMatch);
            if (m == null) { System.out.println("  Match introuvable."); return; }
            System.out.println("  Match actuel : " + m);

            System.out.print("  Score " + m.getNomEquipe1() + " : ");
            int s1 = Integer.parseInt(sc.nextLine().trim());
            System.out.print("  Score " + m.getNomEquipe2() + " : ");
            int s2 = Integer.parseInt(sc.nextLine().trim());

            Integer vainqueur = null;
            if (s1 > s2) vainqueur = m.getIdEquipe1();
            else if (s2 > s1) vainqueur = m.getIdEquipe2();
            else {
                System.out.print("  Égalité — id de l'équipe vainqueure (0 = aucune) : ");
                int v = Integer.parseInt(sc.nextLine().trim());
                if (v != 0) vainqueur = v;
            }

            if (matchDAO.saisirResultat(idMatch, s1, s2, vainqueur)) {
                System.out.println("  Résultat enregistré.");
            } else {
                System.out.println("  Échec de la mise à jour.");
            }
        } catch (NumberFormatException e) {
            System.out.println("  Valeur invalide.");
        } catch (SQLException e) {
            System.out.println("  Erreur SQL : " + e.getMessage());
        }
    }

    private void saisirStatistiques() {
        System.out.print("  Id du match : ");
        try {
            int idMatch = Integer.parseInt(sc.nextLine().trim());
            Match m = matchDAO.rechercherParId(idMatch);
            if (m == null) { System.out.println("  Match introuvable."); return; }
            System.out.println("  Match : " + m);

            boolean continuer = true;
            while (continuer) {
                System.out.print("  Id du joueur (0 pour terminer) : ");
                int idJoueur = Integer.parseInt(sc.nextLine().trim());
                if (idJoueur == 0) break;

                System.out.print("  Kills   : ");
                int kills = Integer.parseInt(sc.nextLine().trim());
                System.out.print("  Deaths  : ");
                int deaths = Integer.parseInt(sc.nextLine().trim());
                System.out.print("  Assists : ");
                int assists = Integer.parseInt(sc.nextLine().trim());
                System.out.print("  Score de performance : ");
                BigDecimal score = new BigDecimal(sc.nextLine().trim());

                Statistique s = new Statistique(idJoueur, idMatch, kills, deaths, assists, score);
                if (matchDAO.saisirStatistiques(s)) System.out.println("  Stats enregistrées.");
                else System.out.println("  Échec.");
            }
        } catch (NumberFormatException e) {
            System.out.println("  Valeur invalide.");
        } catch (SQLException e) {
            System.out.println("  Erreur SQL : " + e.getMessage());
        }
    }

    private void afficherMatchsTournoi() {
        try {
            tournoiDAO.listerTournois().forEach(t -> System.out.println("  " + t));
            System.out.print("  Id du tournoi : ");
            int idTournoi = Integer.parseInt(sc.nextLine().trim());
            List<Match> matchs = matchDAO.listerMatchsTournoi(idTournoi);
            if (matchs.isEmpty()) { System.out.println("  Aucun match pour ce tournoi."); return; }
            matchs.forEach(m -> System.out.println("  " + m));
        } catch (NumberFormatException e) {
            System.out.println("  Id invalide.");
        } catch (SQLException e) {
            System.out.println("  Erreur SQL : " + e.getMessage());
        }
    }

    private void afficherStatsMatch() {
        System.out.print("  Id du match : ");
        try {
            int idMatch = Integer.parseInt(sc.nextLine().trim());
            List<Statistique> stats = matchDAO.listerStatistiquesMatch(idMatch);
            if (stats.isEmpty()) { System.out.println("  Aucune stat pour ce match."); return; }
            System.out.printf("  %-20s %6s %6s %6s %8s%n", "Joueur", "Kills", "Deaths", "Assists", "Score");
            System.out.println("  " + "-".repeat(52));
            stats.forEach(s -> System.out.println("  " + s));
        } catch (NumberFormatException e) {
            System.out.println("  Id invalide.");
        } catch (SQLException e) {
            System.out.println("  Erreur SQL : " + e.getMessage());
        }
    }
}
