package modele;

import java.time.LocalDate;

public class Joueur {

    private int idJoueur;
    private String pseudo;
    private String nom;
    private String prenom;
    private LocalDate dateNaissance;
    private String nationalite;
    private String niveau;

    public Joueur() {}

    public Joueur(int idJoueur, String pseudo, String nom, String prenom,
                  LocalDate dateNaissance, String nationalite, String niveau) {
        this.idJoueur      = idJoueur;
        this.pseudo        = pseudo;
        this.nom           = nom;
        this.prenom        = prenom;
        this.dateNaissance = dateNaissance;
        this.nationalite   = nationalite;
        this.niveau        = niveau;
    }

    public int getIdJoueur()               { return idJoueur; }
    public void setIdJoueur(int id)        { this.idJoueur = id; }

    public String getPseudo()              { return pseudo; }
    public void setPseudo(String pseudo)   { this.pseudo = pseudo; }

    public String getNom()                 { return nom; }
    public void setNom(String nom)         { this.nom = nom; }

    public String getPrenom()              { return prenom; }
    public void setPrenom(String prenom)   { this.prenom = prenom; }

    public LocalDate getDateNaissance()                    { return dateNaissance; }
    public void setDateNaissance(LocalDate dateNaissance)  { this.dateNaissance = dateNaissance; }

    public String getNationalite()                   { return nationalite; }
    public void setNationalite(String nationalite)   { this.nationalite = nationalite; }

    public String getNiveau()              { return niveau; }
    public void setNiveau(String niveau)   { this.niveau = niveau; }

    @Override
    public String toString() {
        return String.format("[%d] %-20s %s %s  (%-15s) niveau=%s",
                idJoueur, pseudo, prenom, nom, nationalite, niveau);
    }
}
