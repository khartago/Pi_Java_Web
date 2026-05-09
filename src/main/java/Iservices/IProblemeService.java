package Iservices;

import model.Probleme;

import java.time.LocalDate;
import java.util.List;

public interface IProblemeService {

    /** Ajoute un problème et retourne son ID généré. */
    int ajouterProbleme(Probleme p);

    void modifierProbleme(Probleme p);

    void supprimerProbleme(int id);

    List<Probleme> afficherProblemes();

    /** Problèmes créés par un utilisateur (fermier). */
    List<Probleme> afficherProblemesParUtilisateur(int idUtilisateur);

    Probleme getProblemeById(int id);

    List<Probleme> afficherProblemesParEtat(String etat);

    /**
     * Filtres : null ou "Tous" = pas de filtre sur ce critère.
     * ordreTri : "date_desc", "date_asc", "gravite_desc", "type_asc".
     */
    List<Probleme> afficherProblemesFiltresEtTries(String type, String gravite, String etat,
                                                    LocalDate dateDebut, LocalDate dateFin, String ordreTri);

    /** Retourne les types distincts des problèmes existants (pour le filtre). */
    List<String> getTypesDistincts();
}

