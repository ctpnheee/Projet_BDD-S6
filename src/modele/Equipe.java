package modele;

import java.time.LocalDate;

public class Equipe {

    private int idEquipe;
    private String nom;
    private String cheminLogo;
    private LocalDate dateCreation;
    private String pays;

    public Equipe() {}

    public Equipe(int idEquipe, String nom, String cheminLogo,
                  LocalDate dateCreation, String pays) {
        this.idEquipe     = idEquipe;
        this.nom          = nom;
        this.cheminLogo   = cheminLogo;
        this.dateCreation = dateCreation;
        this.pays         = pays;
    }

    public int getIdEquipe()                   { return idEquipe; }
    public void setIdEquipe(int idEquipe)       { this.idEquipe = idEquipe; }

    public String getNom()                     { return nom; }
    public void setNom(String nom)             { this.nom = nom; }

    public String getCheminLogo()              { return cheminLogo; }
    public void setCheminLogo(String chemin)   { this.cheminLogo = chemin; }

    public LocalDate getDateCreation()                   { return dateCreation; }
    public void setDateCreation(LocalDate dateCreation)  { this.dateCreation = dateCreation; }

    public String getPays()                { return pays; }
    public void setPays(String pays)       { this.pays = pays; }

    @Override
    public String toString() {
        return String.format("[%d] %-25s  %s  (créée le %s)", idEquipe, nom, pays, dateCreation);
    }
}
