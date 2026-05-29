package vue;

import dao.JoueurDAO;
import modele.Joueur;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class MenuJoueur {

    private final JoueurDAO dao = new JoueurDAO();
    private final Scanner sc;

    public MenuJoueur(Scanner sc) {
        this.sc = sc;
    }

    public void afficher() {
        boolean retour = false;
        while (!retour) {
            System.out.println();
            System.out.println("  ---- Gestion des joueurs ----");
            System.out.println("  1. Ajouter un joueur");
            System.out.println("  2. Lister tous les joueurs");
            System.out.println("  3. Rechercher par pseudo");
            System.out.println("  4. Modifier un joueur");
            System.out.println("  5. Supprimer un joueur");
            System.out.println("  6. Retour");
            System.out.print("  Votre choix : ");
            String choix = sc.nextLine().trim();
            System.out.println();
            switch (choix) {
                case "1" -> ajouterJoueur();
                case "2" -> listerJoueurs();
                case "3" -> rechercherJoueur();
                case "4" -> modifierJoueur();
                case "5" -> supprimerJoueur();
                case "6" -> retour = true;
                default  -> System.out.println("  Choix invalide.");
            }
        }
    }

    private void ajouterJoueur() {
        System.out.println("  -- Nouveau joueur --");
        try {
            Joueur j = new Joueur();
            System.out.print("  Pseudo        : "); j.setPseudo(sc.nextLine().trim());
            System.out.print("  Nom           : "); j.setNom(sc.nextLine().trim());
            System.out.print("  Prénom        : "); j.setPrenom(sc.nextLine().trim());
            System.out.print("  Date naissance (YYYY-MM-DD) : ");
            j.setDateNaissance(parseDate(sc.nextLine().trim()));
            System.out.print("  Nationalité   : "); j.setNationalite(sc.nextLine().trim());
            System.out.print("  Niveau (Elo/rang) : "); j.setNiveau(sc.nextLine().trim());

            if (dao.ajouterJoueur(j)) {
                System.out.println("   Joueur ajouté avec l'id " + j.getIdJoueur());
            } else {
                System.out.println("  Échec de l'ajout.");
            }
        } catch (DateTimeParseException e) {
            System.out.println("  Format de date invalide (attendu : YYYY-MM-DD).");
        } catch (SQLException e) {
            System.out.println("  Erreur SQL : " + e.getMessage());
        }
    }

    private void listerJoueurs() {
        try {
            List<Joueur> liste = dao.listerJoueurs();
            if (liste.isEmpty()) {
                System.out.println("  Aucun joueur en base.");
                return;
            }
            liste.forEach(j -> System.out.println("  " + j));
        } catch (SQLException e) {
            System.out.println("  Erreur SQL : " + e.getMessage());
        }
    }

    private void rechercherJoueur() {
        System.out.print("  Pseudo (exact) : ");
        String pseudo = sc.nextLine().trim();
        try {
            Joueur j = dao.rechercherParPseudo(pseudo);
            if (j == null) System.out.println("  Joueur introuvable.");
            else System.out.println("  " + j);
        } catch (SQLException e) {
            System.out.println("  Erreur SQL : " + e.getMessage());
        }
    }

    private void modifierJoueur() {
        System.out.print("  Id du joueur à modifier : ");
        try {
            int id = Integer.parseInt(sc.nextLine().trim());
            Joueur j = dao.rechercherParId(id);
            if (j == null) {
                System.out.println("  Joueur introuvable.");
                return;
            }
            System.out.println("  Joueur actuel : " + j);
            System.out.println("  (Entrée vide = conserver la valeur actuelle)");
            System.out.print("  Nouveau pseudo [" + j.getPseudo() + "] : ");
            String v = sc.nextLine().trim(); if (!v.isEmpty()) j.setPseudo(v);
            System.out.print("  Nouveau nom   [" + j.getNom() + "] : ");
            v = sc.nextLine().trim(); if (!v.isEmpty()) j.setNom(v);
            System.out.print("  Nouveau prénom [" + j.getPrenom() + "] : ");
            v = sc.nextLine().trim(); if (!v.isEmpty()) j.setPrenom(v);
            System.out.print("  Nouvelle date naissance [" + j.getDateNaissance() + "] : ");
            v = sc.nextLine().trim(); if (!v.isEmpty()) j.setDateNaissance(parseDate(v));
            System.out.print("  Nouvelle nationalité [" + j.getNationalite() + "] : ");
            v = sc.nextLine().trim(); if (!v.isEmpty()) j.setNationalite(v);
            System.out.print("  Nouveau niveau [" + j.getNiveau() + "] : ");
            v = sc.nextLine().trim(); if (!v.isEmpty()) j.setNiveau(v);

            if (dao.modifierJoueur(j)) System.out.println("  Joueur mis à jour.");
            else System.out.println("  Aucune modification effectuée.");
        } catch (NumberFormatException e) {
            System.out.println("  Id invalide.");
        } catch (DateTimeParseException e) {
            System.out.println("  Format de date invalide (YYYY-MM-DD).");
        } catch (SQLException e) {
            System.out.println("  Erreur SQL : " + e.getMessage());
        }
    }

    private void supprimerJoueur() {
        System.out.print("  Id du joueur à supprimer : ");
        try {
            int id = Integer.parseInt(sc.nextLine().trim());
            System.out.print("  Confirmer la suppression du joueur #" + id + " ? (o/N) : ");
            if (!sc.nextLine().trim().equalsIgnoreCase("o")) {
                System.out.println("  Annulé.");
                return;
            }

            int nbRosters = dao.compterRosters(id);
            if (nbRosters > 0) {
                System.out.println("  Ce joueur figure dans " + nbRosters
                        + " roster(s) d'équipe : sa suppression directe est interdite.");
                System.out.print("  Le retirer de ces roster(s) puis le supprimer ? (o/N) : ");
                if (!sc.nextLine().trim().equalsIgnoreCase("o")) {
                    System.out.println("  Annulé.");
                    return;
                }
                if (dao.supprimerJoueurAvecRosters(id))
                    System.out.println("  Joueur (et ses rosters) supprimé.");
                else
                    System.out.println("  Joueur introuvable.");
                return;
            }

            if (dao.supprimerJoueur(id)) System.out.println("  Joueur supprimé.");
            else System.out.println("  Joueur introuvable ou suppression impossible.");
        } catch (NumberFormatException e) {
            System.out.println("  Id invalide.");
        } catch (SQLException e) {
            System.out.println("  Erreur SQL : " + e.getMessage());
        }
    }

    private LocalDate parseDate(String s) {
        return LocalDate.parse(s);
    }
}
