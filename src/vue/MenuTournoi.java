package vue;

import dao.TournoiDAO;
import modele.Phase;
import modele.Tournoi;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class MenuTournoi {

    private final TournoiDAO tournoiDAO = new TournoiDAO();
    private final Scanner sc;

    public MenuTournoi(Scanner sc) {
        this.sc = sc;
    }

    public void afficher() {
        boolean retour = false;
        while (!retour) {
            System.out.println();
            System.out.println("  ---- Gestion des tournois ----");
            System.out.println("  1. Créer un tournoi");
            System.out.println("  2. Lister tous les tournois");
            System.out.println("  3. Ajouter une phase à un tournoi");
            System.out.println("  4. Retour");
            System.out.print("  Votre choix : ");
            String choix = sc.nextLine().trim();
            System.out.println();
            switch (choix) {
                case "1" -> creerTournoi();
                case "2" -> listerTournois();
                case "3" -> ajouterPhase();
                case "4" -> retour = true;
                default  -> System.out.println("  Choix invalide.");
            }
        }
    }

    private void creerTournoi() {
        System.out.println("  -- Nouveau tournoi --");
        try {
            Tournoi t = new Tournoi();
            System.out.print("  Nom            : "); t.setNom(sc.nextLine().trim());
            System.out.print("  Date début (YYYY-MM-DD) : ");
            t.setDateDebut(LocalDate.parse(sc.nextLine().trim()));
            System.out.print("  Date fin   (YYYY-MM-DD) : ");
            t.setDateFin(LocalDate.parse(sc.nextLine().trim()));
            System.out.print("  Type (en_ligne / LAN)   : "); t.setType(sc.nextLine().trim());
            System.out.print("  Dotation (€)   : ");
            t.setDotation(new BigDecimal(sc.nextLine().trim()));
            System.out.print("  Statut (a_venir / en_cours / termine) : ");
            t.setStatut(sc.nextLine().trim());
            System.out.print("  Id du jeu      : ");
            t.setIdJeu(Integer.parseInt(sc.nextLine().trim()));

            if (tournoiDAO.creerTournoi(t)) {
                System.out.println("  Tournoi créé avec l'id " + t.getIdTournoi());
                System.out.print("  Ajouter les phases maintenant ? (o/N) : ");
                if (sc.nextLine().trim().equalsIgnoreCase("o")) {
                    ajouterPhasesTournoi(t.getIdTournoi());
                }
            } else {
                System.out.println("  Échec de la création.");
            }
        } catch (DateTimeParseException e) {
            System.out.println("  Format de date invalide (YYYY-MM-DD).");
        } catch (NumberFormatException e) {
            System.out.println("  Valeur numérique invalide.");
        } catch (SQLException e) {
            System.out.println("  Erreur SQL : " + e.getMessage());
        }
    }

    private void listerTournois() {
        try {
            List<Tournoi> liste = tournoiDAO.listerTournois();
            if (liste.isEmpty()) { System.out.println("  Aucun tournoi en base."); return; }
            liste.forEach(t -> System.out.println("  " + t));
        } catch (SQLException e) {
            System.out.println("  Erreur SQL : " + e.getMessage());
        }
    }

    private void ajouterPhase() {
        listerTournois();
        System.out.print("  Id du tournoi : ");
        try {
            int idTournoi = Integer.parseInt(sc.nextLine().trim());
            ajouterPhasesTournoi(idTournoi);
        } catch (NumberFormatException e) {
            System.out.println("  Id invalide.");
        }
    }

    public void ajouterPhasesTournoi(int idTournoi) {
        System.out.println("  Saisir les phases (libellé vide pour terminer) :");
        int ordre = 1;
        try {
            // Récupérer l'ordre courant maximum
            List<Phase> existing = tournoiDAO.listerPhases(idTournoi);
            if (!existing.isEmpty())
                ordre = existing.get(existing.size() - 1).getNumeroOrdre() + 1;

            while (true) {
                System.out.print("  Phase " + ordre + " libellé (ex: Phase de groupe) : ");
                String libelle = sc.nextLine().trim();
                if (libelle.isEmpty()) break;
                Phase p = new Phase(0, libelle, ordre, idTournoi);
                if (tournoiDAO.ajouterPhase(p)) {
                    System.out.println("  Phase ajoutée : " + p);
                    ordre++;
                } else {
                    System.out.println("  ✘ Échec.");
                }
            }
        } catch (SQLException e) {
            System.out.println("  Erreur SQL : " + e.getMessage());
        }
    }

    // Accès public pour les autres menus qui doivent choisir un tournoi
    public TournoiDAO getTournoiDAO() {
        return tournoiDAO;
    }
}
