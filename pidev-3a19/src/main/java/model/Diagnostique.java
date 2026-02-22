package model;

import java.time.LocalDateTime;

public class Diagnostique {

    private int id;
    private int idProbleme;
    private String cause;
    private String solutionProposee;
    private LocalDateTime dateDiagnostique;
    private String resultat;
    private String medicament;
    /** true = visible par le fermier après acceptation admin, false = brouillon / en attente de révision */
    private boolean approuve;

    public Diagnostique() {
    }

    public Diagnostique(int id, int idProbleme, String cause, String solutionProposee, LocalDateTime dateDiagnostique, String resultat, String medicament, boolean approuve) {
        this.id = id;
        this.idProbleme = idProbleme;
        this.cause = cause;
        this.solutionProposee = solutionProposee;
        this.dateDiagnostique = dateDiagnostique;
        this.resultat = resultat;
        this.medicament = medicament;
        this.approuve = approuve;
    }

    public Diagnostique(int id, int idProbleme, String cause, String solutionProposee, LocalDateTime dateDiagnostique, String resultat, String medicament) {
        this(id, idProbleme, cause, solutionProposee, dateDiagnostique, resultat, medicament, false);
    }

    public Diagnostique(int id, int idProbleme, String cause, String solutionProposee, LocalDateTime dateDiagnostique, String resultat) {
        this(id, idProbleme, cause, solutionProposee, dateDiagnostique, resultat, null);
    }

    public Diagnostique(int idProbleme, String cause, String solutionProposee, LocalDateTime dateDiagnostique, String resultat, String medicament) {
        this.idProbleme = idProbleme;
        this.cause = cause;
        this.solutionProposee = solutionProposee;
        this.dateDiagnostique = dateDiagnostique;
        this.resultat = resultat;
        this.medicament = medicament;
        this.approuve = false;
    }

    public Diagnostique(int idProbleme, String cause, String solutionProposee, LocalDateTime dateDiagnostique, String resultat) {
        this(idProbleme, cause, solutionProposee, dateDiagnostique, resultat, null);
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

    public String getMedicament() {
        return medicament;
    }

    public void setMedicament(String medicament) {
        this.medicament = medicament;
    }

    public boolean isApprouve() {
        return approuve;
    }

    public void setApprouve(boolean approuve) {
        this.approuve = approuve;
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
                ", medicament='" + medicament + '\'' +
                ", approuve=" + approuve +
                '}';
    }
}

