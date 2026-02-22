package Iservices;

import model.Diagnostique;

import java.util.List;

public interface IDiagnostiqueService {

    void ajouterDiagnostique(Diagnostique d);

    void modifierDiagnostique(Diagnostique d);

    void supprimerDiagnostique(int id);

    List<Diagnostique> afficherDiagnostiques();

    Diagnostique afficherDiagnostiqueParProbleme(int idProbleme);

    /** Retourne le diagnostic uniquement s'il est approuvé (visible au fermier). */
    Diagnostique afficherDiagnostiqueParProblemeApprouve(int idProbleme);

    /** Marque le diagnostic comme approuvé pour qu'il soit visible au fermier. */
    void approuverDiagnostique(int idDiagnostique);
}

