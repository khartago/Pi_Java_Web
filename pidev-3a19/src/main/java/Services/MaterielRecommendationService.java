package Services;

import model.Materiel;
import model.MaterielDAO;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Service de recommandation de matériels — miroir exact du Symfony.
 *
 * Logique :
 *   - Exclut les matériels déjà liés au produit courant
 *   - Exclut les matériels en état "panne"
 *   - Score : neuf=80, bon=65, autre=40
 *   - Trie par score décroissant, retourne les N premiers
 */
public class MaterielRecommendationService {

    private final MaterielDAO materielDAO;

    public MaterielRecommendationService() {
        this.materielDAO = new MaterielDAO();
    }

    public MaterielRecommendationService(MaterielDAO materielDAO) {
        this.materielDAO = materielDAO;
    }

    /**
     * Recommande jusqu'à {@code limit} matériels pour un produit donné.
     *
     * @param idProduit  identifiant du produit
     * @param limit      nombre max de recommandations (défaut 4 comme Symfony)
     * @return liste triée par score décroissant
     */
    public List<RecommendationItem> recommend(int idProduit, int limit) {
        List<Materiel> candidates = materielDAO.getAllExcludingProduit(idProduit);

        List<RecommendationItem> scored = new ArrayList<>();
        for (Materiel m : candidates) {
            String etat = m.getEtat() != null ? m.getEtat().toLowerCase() : "";

            // Exclure les matériels en panne (identique Symfony)
            if ("panne".equals(etat)) continue;

            int score = switch (etat) {
                case "neuf" -> 80;
                case "bon"  -> 65;
                default     -> 40;
            };

            scored.add(new RecommendationItem(m, score));
        }

        // Tri décroissant par score
        scored.sort(Comparator.comparingInt(RecommendationItem::score).reversed());

        return scored.subList(0, Math.min(limit, scored.size()));
    }

    /** Surcharge avec limit=4 (défaut Symfony). */
    public List<RecommendationItem> recommend(int idProduit) {
        return recommend(idProduit, 4);
    }

    // ------------------------------------------------------------------ //
    //  Record résultat
    // ------------------------------------------------------------------ //

    /**
     * Résultat d'une recommandation.
     *
     * @param materiel  le matériel recommandé
     * @param score     score de pertinence (40 / 65 / 80)
     */
    public record RecommendationItem(Materiel materiel, int score) {

        /** Libellé du badge affiché dans l'UI : "MATCH 80%" */
        public String badgeLabel() {
            return "MATCH " + score + "%";
        }

        /** Libellé de l'état capitalisé. */
        public String etatLabel() {
            String e = materiel.getEtat();
            if (e == null || e.isBlank()) return "—";
            return e.substring(0, 1).toUpperCase() + e.substring(1).toLowerCase();
        }
    }
}
