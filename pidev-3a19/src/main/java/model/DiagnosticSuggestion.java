package model;

/**
 * DTO pour la suggestion de diagnostic retournée par l'API IA (cause, solution, médicament).
 */
public class DiagnosticSuggestion {

    private String cause;
    private String solutionProposee;
    private String medicament;

    public DiagnosticSuggestion() {
    }

    public DiagnosticSuggestion(String cause, String solutionProposee, String medicament) {
        this.cause = cause;
        this.solutionProposee = solutionProposee;
        this.medicament = medicament;
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

    public String getMedicament() {
        return medicament;
    }

    public void setMedicament(String medicament) {
        this.medicament = medicament;
    }
}
