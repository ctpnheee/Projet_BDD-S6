package vue;

import dao.EquipeDAO;
import dao.JoueurDAO;
import dao.TournoiDAO;
import modele.Equipe;
import modele.Joueur;
import modele.Tournoi;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class MenuConsultation {

    private final JoueurDAO  joueurDAO  = new JoueurDAO();
    private final EquipeDAO  equipeDAO  = new EquipeDAO();
    private final TournoiDAO tournoiDAO = new TournoiDAO();
    private final Scanner sc;

    public MenuConsultation(Scanner sc) {
        this.sc = sc;
    }

    public void afficher() {
        boolean retour = false;
        while (!retour) {
            System.out.println();
            System.out.println("  ---- Consultation & statistiques ----");
            System.out.println("  1. Classement d'un tournoi");
            System.out.println("  2. Palmarès d'un joueur");
            System.out.println("  3. Statistiques globales d'une équipe");
            System.out.println("  4. Rechercher un joueur par mot-clé");
            System.out.println("  5. Rechercher une équipe par mot-clé");
            System.out.println("  6. Retour");
            System.out.print("  Votre choix : ");
            String choix = sc.nextLine().trim();
            System.out.println();
            switch (choix) {
                case "1" -> classementTournoi();
                case "2" -> palmareesJoueur();
                case "3" -> statsEquipe();
                case "4" -> rechercherJoueur();
                case "5" -> rechercherEquipe();
                case "6" -> retour = true;
                default  -> System.out.println("  Choix invalide.");
            }
        }
    }

    public void classementTournoi() {
        try {
            List<Tournoi> tournois = tournoiDAO.listerTournois();
            if (tournois.isEmpty()) { System.out.println("  Aucun tournoi en base."); return; }
            tournois.forEach(t -> System.out.println("  " + t));
            System.out.print("  Id du tournoi : ");
            int id = Integer.parseInt(sc.nextLine().trim());
            Tournoi t = tournoiDAO.rechercherParId(id);
            if (t == null) { System.out.println("  Tournoi introuvable."); return; }
            System.out.println("  Classement — " + t.getNom());
            tournoiDAO.afficherClassement(id);
        } catch (NumberFormatException e) {
            System.out.println("  Id invalide.");
        } catch (SQLException e) {
            System.out.println("  Erreur SQL : " + e.getMessage());
        }
    }

    public void palmareesJoueur() {
        try {
            System.out.print("  Pseudo du joueur : ");
            String pseudo = sc.nextLine().trim();
            Joueur j = joueurDAO.rechercherParPseudo(pseudo);
            if (j == null) { System.out.println("  Joueur introuvable."); return; }
            System.out.println("  Palmarès de " + j.getPseudo() + " (" + j.getPrenom() + " " + j.getNom() + ")");
            joueurDAO.afficherPalmares(j.getIdJoueur());
        } catch (SQLException e) {
            System.out.println("  Erreur SQL : " + e.getMessage());
        }
    }

    private void statsEquipe() {
        try {
            List<Equipe> equipes = equipeDAO.listerEquipes();
            if (equipes.isEmpty()) { System.out.println("  Aucune équipe en base."); return; }
            equipes.forEach(e -> System.out.println("  " + e));
            System.out.print("  Id de l'équipe : ");
            int id = Integer.parseInt(sc.nextLine().trim());
            equipeDAO.afficherStatsEquipe(id);
        } catch (NumberFormatException e) {
            System.out.println("  Id invalide.");
        } catch (SQLException e) {
            System.out.println("  Erreur SQL : " + e.getMessage());
        }
    }

    private void rechercherJoueur() {
        System.out.print("  Mot-clé (pseudo / nom / prénom) : ");
        String motCle = sc.nextLine().trim();
        try {
            List<Joueur> liste = joueurDAO.rechercherParMotCle(motCle);
            if (liste.isEmpty()) System.out.println("  Aucun résultat.");
            else liste.forEach(j -> System.out.println("  " + j));
        } catch (SQLException e) {
            System.out.println("  Erreur SQL : " + e.getMessage());
        }
    }

    private void rechercherEquipe() {
        System.out.print("  Mot-clé (nom / pays) : ");
        String motCle = sc.nextLine().trim();
        try {
            List<Equipe> liste = equipeDAO.rechercherParMotCle(motCle);
            if (liste.isEmpty()) System.out.println("  Aucun résultat.");
            else liste.forEach(e -> System.out.println("  " + e));
        } catch (SQLException e) {
            System.out.println("  Erreur SQL : " + e.getMessage());
        }
    }
}
