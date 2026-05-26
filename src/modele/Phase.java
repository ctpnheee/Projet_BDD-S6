package modele;

public class Phase {

    private int idPhase;
    private String libelle;
    private int numeroOrdre;
    private int idTournoi;

    public Phase() {}

    public Phase(int idPhase, String libelle, int numeroOrdre, int idTournoi) {
        this.idPhase     = idPhase;
        this.libelle     = libelle;
        this.numeroOrdre = numeroOrdre;
        this.idTournoi   = idTournoi;
    }

    public int getIdPhase()                    { return idPhase; }
    public void setIdPhase(int idPhase)        { this.idPhase = idPhase; }

    public String getLibelle()                 { return libelle; }
    public void setLibelle(String libelle)     { this.libelle = libelle; }

    public int getNumeroOrdre()                { return numeroOrdre; }
    public void setNumeroOrdre(int n)          { this.numeroOrdre = n; }

    public int getIdTournoi()                  { return idTournoi; }
    public void setIdTournoi(int idTournoi)    { this.idTournoi = idTournoi; }

    @Override
    public String toString() {
        return String.format("[%d] %s (ordre %d, tournoi %d)",
                idPhase, libelle, numeroOrdre, idTournoi);
    }
}
