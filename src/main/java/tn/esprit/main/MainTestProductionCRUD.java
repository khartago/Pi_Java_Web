package tn.esprit.main;

import tn.esprit.entities.Production;
import tn.esprit.services.ProductionService;

import java.sql.Date;

public class MainTestProductionCRUD {

    public static void main(String[] args) {

        try {
            ProductionService ps = new ProductionService();

            // ✅ 1) ADD (etat auto = EN_ATTENTE)
            Production p = new Production(
                    "taha",
                    "savera",
                    5000,
                    Date.valueOf("2026-02-14"),
                    "summer"
            );
            ps.ajouter(p);

            // ✅ 2) DISPLAY
            System.out.println("📌 Liste production:");
            ps.afficher().forEach(System.out::println);

            // ⚠️ For UPDATE and DELETE you need an existing id
            // Example:
            Production p2 = new Production(
                    9,
                    "pomme",
                    "golden",
                    8000,
                    Date.valueOf("2026-02-10"),
                    "winter",
                    "EN_ATTENTE"   // ou COMPLETE
            );

            //ps.modifier(p2);

            // ps.supprimer(1);

        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }
}
