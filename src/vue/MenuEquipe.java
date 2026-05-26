package vue;

import dao.EquipeDAO;
import dao.TournoiDAO;
import modele.Equipe;
import modele.Tournoi;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class MenuEquipe {

    private final EquipeDAO equipeDAO  = new EquipeDAO();
    private final TournoiDAO tournoiDAO = new TournoiDAO();
    private final Scanner sc;

    public MenuEquipe(Scanner sc) {
        this.sc = sc;
    }

    public void afficher() {
        boolean retour = false;
        while (!retour) {
            System.out.println();
            System.out.println("  ---- Gestion des équipes ----");
            System.out.println("  1. Ajouter une équipe");
            System.out.println("  2. Lister toutes les équipes");
            System.out.println("  3. Rechercher par mot-clé");
            System.out.println("  4. Statistiques globales d'une équipe");
            System.out.println("  5. Inscrire une équipe à un tournoi");
            System.out.println("  6. Retour");
            System.out.print("  Votre choix : ");
            String choix = sc.nextLine().trim();
            System.out.println();
            switch (choix) {
                case "1" -> ajouterEquipe();
                case "2" -> listerEquipes();
                case "3" -> rechercherEquipe();
                case "4" -> statsEquipe();
                case "5" -> inscrireEquipe();
                case "6" -> retour = true;
                default  -> System.out.println("  Choix invalide.");
            }
        }
    }

    private void ajouterEquipe() {
        System.out.println("  -- Nouvelle équipe --");
        try {
            Equipe e = new Equipe();
            System.out.print("  Nom           : "); e.setNom(sc.nextLine().trim());
            System.out.print("  Pays          : "); e.setPays(sc.nextLine().trim());
            System.out.print("  Date création (YYYY-MM-DD) : ");
            e.setDateCreation(LocalDate.parse(sc.nextLine().trim()));
            System.out.print("  Chemin logo   : "); e.setCheminLogo(sc.nextLine().trim());

            if (equipeDAO.ajouterEquipe(e)) {
                System.out.println("  Équipe ajoutée avec l'id " + e.getIdEquipe());
            } else {
                System.out.println("  Échec de l'ajout.");
            }
        } catch (DateTimeParseException ex) {
            System.out.println("  Format de date invalide (YYYY-MM-DD).");
        } catch (SQLException ex) {
            System.out.println("  Erreur SQL : " + ex.getMessage());
        }
    }

    private void listerEquipes() {
        try {
            List<Equipe> liste = equipeDAO.listerEquipes();
            if (liste.isEmpty()) { System.out.println("  Aucune équipe en base."); return; }
            liste.forEach(e -> System.out.println("  " + e));
        } catch (SQLException e) {
            System.out.println("  Erreur SQL : " + e.getMessage());
        }
    }

    private void rechercherEquipe() {
        System.out.print("  Mot-clé : ");
        String motCle = sc.nextLine().trim();
        try {
            List<Equipe> liste = equipeDAO.rechercherParMotCle(motCle);
            if (liste.isEmpty()) System.out.println("  Aucun résultat.");
            else liste.forEach(e -> System.out.println("  " + e));
        } catch (SQLException e) {
            System.out.println("  Erreur SQL : " + e.getMessage());
        }
    }

    private void statsEquipe() {
        listerEquipes();
        System.out.print("  Id de l'équipe : ");
        try {
            int id = Integer.parseInt(sc.nextLine().trim());
            equipeDAO.afficherStatsEquipe(id);
        } catch (NumberFormatException e) {
            System.out.println("  Id invalide.");
        } catch (SQLException e) {
            System.out.println("  Erreur SQL : " + e.getMessage());
        }
    }

    private void inscrireEquipe() {
        try {
            System.out.println("  -- Équipes disponibles --");
            equipeDAO.listerEquipes().forEach(e -> System.out.println("  " + e));
            System.out.print("  Id de l'équipe : ");
            int idEquipe = Integer.parseInt(sc.nextLine().trim());

            System.out.println();
            System.out.println("  -- Tournois disponibles --");
            List<Tournoi> tournois = tournoiDAO.listerTournois();
            tournois.forEach(t -> System.out.println("  " + t));
            System.out.print("  Id du tournoi  : ");
            int idTournoi = Integer.parseInt(sc.nextLine().trim());

            if (equipeDAO.inscrireEquipe(idEquipe, idTournoi)) {
                System.out.println("  Équipe inscrite au tournoi.");
            } else {
                System.out.println("  Inscription échouée (déjà inscrite ?).");
            }
        } catch (NumberFormatException e) {
            System.out.println("  Id invalide.");
        } catch (SQLException e) {
            System.out.println("  Erreur SQL : " + e.getMessage());
        }
    }
}
