package tn.esprit.main;

import tn.esprit.entities.ProductionPlante;
import tn.esprit.services.ProductionPlanteService;

import java.sql.Date;

public class MainTestProductionPlanteCRUD {

    public static void main(String[] args) {

        try {
            ProductionPlanteService ps = new ProductionPlanteService();

            // ✅ 1) ADD
            ProductionPlante p = new ProductionPlante(
                    1500.5f,
                    Date.valueOf("2026-02-15"),
                    "Bonne",
                    "Récoltée"
            );
            ps.ajouter(p);

            // ✅ 2) DISPLAY
            System.out.println("📌 Liste production plant:");
            ps.afficher().forEach(System.out::println);

            // ⚠️ For UPDATE and DELETE you need an existing idProduction
            // Example:
            ProductionPlante p2 = new ProductionPlante(
                    1,
                    9999.9f,
                    Date.valueOf("2026-02-20"),
                    "Excellente",
                    "Vendu"
            );

            ps.modifier(p2);

            // ps.supprimer(1);

        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }
}
