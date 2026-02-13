package Iservices;

import Entites.User;

import java.util.List;

public interface IUserService {

    void ajouterUser(User u);

    void modifierUser(User u);

    void supprimerUser(int id);

    List<User> afficherUsers();

    User trouverParEmail(String email);

    User authentifier(String email, String motDePasse);
}

