package modele;

import java.time.LocalDateTime;

public class Match {

    private int idMatch;
    private LocalDateTime dateMatch;
    private int scoreEquipe1;
    private int scoreEquipe2;
    private int idPhase;
    private int idEquipe1;
    private int idEquipe2;
    private Integer idEquipeVainqueur;   // peut être null

    // libellés dénormalisés remplis par des requêtes JOIN
    private String nomEquipe1;
    private String nomEquipe2;
    private String nomVainqueur;
    private String libellePhase;

    public Match() {}

    public Match(int idMatch, LocalDateTime dateMatch, int scoreEquipe1, int scoreEquipe2,
                 int idPhase, int idEquipe1, int idEquipe2, Integer idEquipeVainqueur) {
        this.idMatch           = idMatch;
        this.dateMatch         = dateMatch;
        this.scoreEquipe1      = scoreEquipe1;
        this.scoreEquipe2      = scoreEquipe2;
        this.idPhase           = idPhase;
        this.idEquipe1         = idEquipe1;
        this.idEquipe2         = idEquipe2;
        this.idEquipeVainqueur = idEquipeVainqueur;
    }

    public int getIdMatch()                              { return idMatch; }
    public void setIdMatch(int idMatch)                  { this.idMatch = idMatch; }

    public LocalDateTime getDateMatch()                          { return dateMatch; }
    public void setDateMatch(LocalDateTime dateMatch)            { this.dateMatch = dateMatch; }

    public int getScoreEquipe1()                         { return scoreEquipe1; }
    public void setScoreEquipe1(int s)                   { this.scoreEquipe1 = s; }

    public int getScoreEquipe2()                         { return scoreEquipe2; }
    public void setScoreEquipe2(int s)                   { this.scoreEquipe2 = s; }

    public int getIdPhase()                              { return idPhase; }
    public void setIdPhase(int idPhase)                  { this.idPhase = idPhase; }

    public int getIdEquipe1()                            { return idEquipe1; }
    public void setIdEquipe1(int id)                     { this.idEquipe1 = id; }

    public int getIdEquipe2()                            { return idEquipe2; }
    public void setIdEquipe2(int id)                     { this.idEquipe2 = id; }

    public Integer getIdEquipeVainqueur()                { return idEquipeVainqueur; }
    public void setIdEquipeVainqueur(Integer id)         { this.idEquipeVainqueur = id; }

    public String getNomEquipe1()                        { return nomEquipe1; }
    public void setNomEquipe1(String n)                  { this.nomEquipe1 = n; }

    public String getNomEquipe2()                        { return nomEquipe2; }
    public void setNomEquipe2(String n)                  { this.nomEquipe2 = n; }

    public String getNomVainqueur()                      { return nomVainqueur; }
    public void setNomVainqueur(String n)                { this.nomVainqueur = n; }

    public String getLibellePhase()                      { return libellePhase; }
    public void setLibellePhase(String l)                { this.libellePhase = l; }

    @Override
    public String toString() {
        String e1 = (nomEquipe1 != null) ? nomEquipe1 : "E" + idEquipe1;
        String e2 = (nomEquipe2 != null) ? nomEquipe2 : "E" + idEquipe2;
        String v  = (nomVainqueur != null) ? nomVainqueur
                  : (idEquipeVainqueur != null ? "id=" + idEquipeVainqueur : "en cours");
        return String.format("[%d] %s  %s %d-%d %s  Vainqueur: %s",
                idMatch, dateMatch, e1, scoreEquipe1, scoreEquipe2, e2, v);
    }
}
