package Iservices;

import model.Diagnostique;

import java.util.List;

public interface IDiagnostiqueService {

    void ajouterDiagnostique(Diagnostique d);

    void modifierDiagnostique(Diagnostique d);

    void supprimerDiagnostique(int id);

    List<Diagnostique> afficherDiagnostiques();

    Diagnostique afficherDiagnostiqueParProbleme(int idProbleme);
}

