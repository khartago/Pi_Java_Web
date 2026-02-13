package Entites;

import java.time.LocalDateTime;

public class Diagnostique {

    private int id;
    private int idProbleme;
    private String cause;
    private String solutionProposee;
    private LocalDateTime dateDiagnostique;
    private String resultat;

    public Diagnostique() {
    }

    public Diagnostique(int id, int idProbleme, String cause, String solutionProposee, LocalDateTime dateDiagnostique, String resultat) {
        this.id = id;
        this.idProbleme = idProbleme;
        this.cause = cause;
        this.solutionProposee = solutionProposee;
        this.dateDiagnostique = dateDiagnostique;
        this.resultat = resultat;
    }

    public Diagnostique(int idProbleme, String cause, String solutionProposee, LocalDateTime dateDiagnostique, String resultat) {
        this.idProbleme = idProbleme;
        this.cause = cause;
        this.solutionProposee = solutionProposee;
        this.dateDiagnostique = dateDiagnostique;
        this.resultat = resultat;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdProbleme() {
        return idProbleme;
    }

    public void setIdProbleme(int idProbleme) {
        this.idProbleme = idProbleme;
    }

    public String getCause() {
        return cause;
    }

    public void setCause(String cause) {
        this.cause = cause;
    }

    public String getSolutionProposee() {
        return solutionProposee;
    }

    public void setSolutionProposee(String solutionProposee) {
        this.solutionProposee = solutionProposee;
    }

    public LocalDateTime getDateDiagnostique() {
        return dateDiagnostique;
    }

    public void setDateDiagnostique(LocalDateTime dateDiagnostique) {
        this.dateDiagnostique = dateDiagnostique;
    }

    public String getResultat() {
        return resultat;
    }

    public void setResultat(String resultat) {
        this.resultat = resultat;
    }

    @Override
    public String toString() {
        return "Diagnostique{" +
                "id=" + id +
                ", idProbleme=" + idProbleme +
                ", cause='" + cause + '\'' +
                ", solutionProposee='" + solutionProposee + '\'' +
                ", dateDiagnostique=" + dateDiagnostique +
                ", resultat='" + resultat + '\'' +
                '}';
    }
}

