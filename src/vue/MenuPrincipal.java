package vue;

import util.ConnexionBDD;

import java.sql.SQLException;
import java.util.Scanner;

public class MenuPrincipal {

    private final Scanner sc = new Scanner(System.in);

    private final MenuJoueur      menuJoueur      = new MenuJoueur(sc);
    private final MenuEquipe      menuEquipe      = new MenuEquipe(sc);
    private final MenuTournoi     menuTournoi     = new MenuTournoi(sc);
    private final MenuMatch       menuMatch       = new MenuMatch(sc);
    private final MenuConsultation menuConsultation = new MenuConsultation(sc);

    public void lancer() {
        afficherBanniere();
        boolean running = true;
        while (running) {
            afficherMenu();
            String choix = sc.nextLine().trim();
            System.out.println();
            switch (choix) {
                case "1" -> menuJoueur.afficher();
                case "2" -> menuEquipe.afficher();
                case "3" -> menuTournoi.afficher();
                case "4" -> menuMatch.afficher();
                case "5" -> menuConsultation.classementTournoi();
                case "6" -> menuConsultation.palmareesJoueur();
                case "7" -> menuConsultation.afficher();
                case "8" -> running = false;
                default  -> System.out.println("  Choix invalide. Veuillez saisir un nombre entre 1 et 8.");
            }
        }
        quitter();
    }

    private void afficherBanniere() {
        System.out.println();
        System.out.println("  ========================================");
        System.out.println("   Plateforme E-sport — Base de données  ");
        System.out.println("  ========================================");
    }

    private void afficherMenu() {
        System.out.println();
        System.out.println("  ========================================");
        System.out.println("   Plateforme E-sport --- Menu Principal  ");
        System.out.println("  ========================================");
        System.out.println("  1. Gestion des joueurs");
        System.out.println("  2. Gestion des équipes");
        System.out.println("  3. Gestion des tournois");
        System.out.println("  4. Saisir le résultat d'un match");
        System.out.println("  5. Classement d'un tournoi");
        System.out.println("  6. Statistiques d'un joueur");
        System.out.println("  7. Consultation avancée");
        System.out.println("  8. Quitter");
        System.out.println("  ========================================");
        System.out.print("  Votre choix : ");
    }

    private void quitter() {
        System.out.println();
        System.out.println("  Fermeture de la connexion...");
        try {
            ConnexionBDD.fermer();
        } catch (SQLException e) {
            System.err.println("  Erreur lors de la fermeture : " + e.getMessage());
        }
        System.out.println("  Au revoir !");
        sc.close();
    }

    // Rendre les méthodes de consultation accessibles pour les raccourcis du menu principal
    public MenuConsultation getMenuConsultation() { return menuConsultation; }
}
