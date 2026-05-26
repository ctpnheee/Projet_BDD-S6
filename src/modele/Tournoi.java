package modele;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Tournoi {

    private int idTournoi;
    private String nom;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private String type;       // "en_ligne" | "LAN"
    private BigDecimal dotation;
    private String statut;     // "a_venir" | "en_cours" | "termine"
    private int idJeu;
    private String nomJeu;     // rempli par des requêtes JOIN

    public Tournoi() {}

    public Tournoi(int idTournoi, String nom, LocalDate dateDebut, LocalDate dateFin,
                   String type, BigDecimal dotation, String statut, int idJeu) {
        this.idTournoi = idTournoi;
        this.nom       = nom;
        this.dateDebut = dateDebut;
        this.dateFin   = dateFin;
        this.type      = type;
        this.dotation  = dotation;
        this.statut    = statut;
        this.idJeu     = idJeu;
    }

    public int getIdTournoi()                   { return idTournoi; }
    public void setIdTournoi(int id)            { this.idTournoi = id; }

    public String getNom()                      { return nom; }
    public void setNom(String nom)              { this.nom = nom; }

    public LocalDate getDateDebut()                  { return dateDebut; }
    public void setDateDebut(LocalDate dateDebut)    { this.dateDebut = dateDebut; }

    public LocalDate getDateFin()                    { return dateFin; }
    public void setDateFin(LocalDate dateFin)        { this.dateFin = dateFin; }

    public String getType()                     { return type; }
    public void setType(String type)            { this.type = type; }

    public BigDecimal getDotation()                  { return dotation; }
    public void setDotation(BigDecimal dotation)     { this.dotation = dotation; }

    public String getStatut()                   { return statut; }
    public void setStatut(String statut)        { this.statut = statut; }

    public int getIdJeu()                       { return idJeu; }
    public void setIdJeu(int idJeu)             { this.idJeu = idJeu; }

    public String getNomJeu()                   { return nomJeu; }
    public void setNomJeu(String nomJeu)        { this.nomJeu = nomJeu; }

    @Override
    public String toString() {
        String jeuLabel = (nomJeu != null) ? nomJeu : "id=" + idJeu;
        return String.format("[%d] %-30s  %-8s  %-12s  dotation=%.0f€  statut=%s",
                idTournoi, nom, type, jeuLabel, dotation, statut);
    }
}
