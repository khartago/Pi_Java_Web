package Iservices;

import model.personne;

import java.util.List;

public interface IpersonneServices {

    void ajouterPersonne(personne p);
    void modifierPersonne(personne p);

    void supprimerPersonne(int id);

    List<personne> afficherPersonne();

}
