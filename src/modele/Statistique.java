package modele;

import java.math.BigDecimal;

public class Statistique {

    private int idJoueur;
    private int idMatch;
    private int nbKills;
    private int nbDeaths;
    private int nbAssists;
    private BigDecimal scorePerformance;

    // libellés dénormalisés
    private String pseudoJoueur;

    public Statistique() {}

    public Statistique(int idJoueur, int idMatch, int nbKills, int nbDeaths,
                       int nbAssists, BigDecimal scorePerformance) {
        this.idJoueur         = idJoueur;
        this.idMatch          = idMatch;
        this.nbKills          = nbKills;
        this.nbDeaths         = nbDeaths;
        this.nbAssists        = nbAssists;
        this.scorePerformance = scorePerformance;
    }

    public int getIdJoueur()                         { return idJoueur; }
    public void setIdJoueur(int id)                  { this.idJoueur = id; }

    public int getIdMatch()                          { return idMatch; }
    public void setIdMatch(int id)                   { this.idMatch = id; }

    public int getNbKills()                          { return nbKills; }
    public void setNbKills(int n)                    { this.nbKills = n; }

    public int getNbDeaths()                         { return nbDeaths; }
    public void setNbDeaths(int n)                   { this.nbDeaths = n; }

    public int getNbAssists()                        { return nbAssists; }
    public void setNbAssists(int n)                  { this.nbAssists = n; }

    public BigDecimal getScorePerformance()                    { return scorePerformance; }
    public void setScorePerformance(BigDecimal s)              { this.scorePerformance = s; }

    public String getPseudoJoueur()                  { return pseudoJoueur; }
    public void setPseudoJoueur(String p)            { this.pseudoJoueur = p; }

    @Override
    public String toString() {
        String label = (pseudoJoueur != null) ? pseudoJoueur : "joueur#" + idJoueur;
        return String.format("%-20s  K=%d  D=%d  A=%d  score=%.2f",
                label, nbKills, nbDeaths, nbAssists, scorePerformance);
    }
}
