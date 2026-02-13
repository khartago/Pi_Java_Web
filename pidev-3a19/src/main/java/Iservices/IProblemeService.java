package Iservices;

import Entites.Probleme;

import java.util.List;

public interface IProblemeService {

    void ajouterProbleme(Probleme p);

    void modifierProbleme(Probleme p);

    void supprimerProbleme(int id);

    List<Probleme> afficherProblemes();

    List<Probleme> afficherProblemesParEtat(String etat);
}

