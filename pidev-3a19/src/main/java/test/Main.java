package test;

import model.Diagnostique;
import model.Probleme;
import model.User;
import Services.DiagnostiqueService;
import Services.ProblemeService;
import Services.UserService;
import Utils.Mydatabase;

import java.time.LocalDateTime;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        Mydatabase.getInstance();

        ProblemeService problemeService = new ProblemeService();
        DiagnostiqueService diagnostiqueService = new DiagnostiqueService();
        UserService userService = new UserService();

        // --------- Probleme / Diagnostique ----------
        Probleme probleme = new Probleme(
                "Maladie des cultures",
                "Taches jaunes sur les feuilles de ble dans la parcelle nord",
                "Moyenne",
                LocalDateTime.now(),
                "EN_ATTENTE",
                "parcelle_nord.jpg;feuilles.jpg"
        );
        problemeService.ajouterProbleme(probleme);

        List<Probleme> problemes = problemeService.afficherProblemes();
        System.out.println("Liste des problemes : ");
        System.out.println(problemes);

        int idProbleme = problemes.get(problemes.size() - 1).getId();

        Probleme problemeExistant = new Probleme(
                idProbleme,
                "Maladie des cultures",
                "Taches jaunes sur ble - parcelle nord, symptomes confirmes",
                "Haute",
                LocalDateTime.now(),
                "EN_COURS",
                "parcelle_nord.jpg;feuilles.jpg"
        );
        problemeService.modifierProbleme(problemeExistant);

        System.out.println("Problemes EN_COURS : ");
        System.out.println(problemeService.afficherProblemesParEtat("EN_COURS"));

        Diagnostique diagnostique = new Diagnostique(
                idProbleme,
                "Rouille jaune (Puccinia striiformis)",
                "Traitement fongicide recommande ; eviter exces d'azote ; variete resistante pour la prochaine campagne",
                LocalDateTime.now(),
                "VALIDE"
        );
        diagnostiqueService.ajouterDiagnostique(diagnostique);

        List<Diagnostique> diagnostiques = diagnostiqueService.afficherDiagnostiques();
        System.out.println("Liste des diagnostiques : ");
        System.out.println(diagnostiques);

        int idDiagnostique = diagnostiques.get(diagnostiques.size() - 1).getId();

        Diagnostique diagnostiqueExistant = new Diagnostique(
                idDiagnostique,
                idProbleme,
                "Rouille jaune - diagnostic confirme",
                "Traitement fongicide ; rotation et variete resistante",
                LocalDateTime.now(),
                "VALIDE"
        );
        diagnostiqueService.modifierDiagnostique(diagnostiqueExistant);

        System.out.println("Diagnostique pour le probleme " + idProbleme + " : ");
        System.out.println(diagnostiqueService.afficherDiagnostiqueParProbleme(idProbleme));

        // --------- User ----------
        User nouveauUser = new User(
                "Farmer One",
                "farmer1@example.com",
                "password123",
                "FERMIER"
        );
        userService.ajouterUser(nouveauUser);

        List<User> users = userService.afficherUsers();
        System.out.println("Liste des users : ");
        System.out.println(users);

        int idUser = users.get(users.size() - 1).getId();

        User userModifie = new User(
                idUser,
                "Farmer One Modifie",
                "farmer1@example.com",
                "password123",
                "FERMIER"
        );
        userService.modifierUser(userModifie);

        System.out.println("User trouve par email : ");
        System.out.println(userService.trouverParEmail("farmer1@example.com"));

        System.out.println("Authentification (email/mot de passe corrects) : ");
        System.out.println(userService.authentifier("farmer1@example.com", "password123"));

        userService.supprimerUser(idUser);

        // Nettoyage des donnees de test probleme/diagnostique
        diagnostiqueService.supprimerDiagnostique(idDiagnostique);
        problemeService.supprimerProbleme(idProbleme);
    }
}
