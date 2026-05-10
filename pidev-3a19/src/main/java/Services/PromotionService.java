package Services;

import model.Produit;
import model.Promotion;
import model.PromotionDAO;

import java.util.List;

/**
 * Service Promotion — miroir du PromotionService Symfony.
 */
public class PromotionService {

    private final PromotionDAO promotionDAO;

    public PromotionService() {
        this.promotionDAO = new PromotionDAO();
    }

    public PromotionService(PromotionDAO promotionDAO) {
        this.promotionDAO = promotionDAO;
    }

    // ------------------------------------------------------------------ //
    //  Logique métier
    // ------------------------------------------------------------------ //

    /**
     * Retourne la meilleure promotion active pour un produit et une quantité.
     * Miroir de getBestPromotionForProduct() Symfony.
     */
    public Promotion getBestPromotionForProduct(Produit produit, int quantity) {
        if (produit.getPrixUnitaire() <= 0) return null;

        List<Promotion> promotions = promotionDAO.findActiveForProduct(
                produit.getIdProduit(), quantity);
        if (promotions.isEmpty()) return null;

        double basePrice   = produit.getPrixUnitaire();
        Promotion best     = null;
        double bestSavings = -1.0;

        for (Promotion promo : promotions) {
            double savings = basePrice - promo.applyTo(basePrice, quantity);
            if (savings > bestSavings) {
                bestSavings = savings;
                best = promo;
            }
        }
        return best;
    }

    public Promotion getBestPromotionForProduct(Produit produit) {
        return getBestPromotionForProduct(produit, 1);
    }

    public double getPromoPrice(Produit produit, int quantity) {
        Promotion best = getBestPromotionForProduct(produit, quantity);
        if (best == null) return produit.getPrixUnitaire();
        return best.applyTo(produit.getPrixUnitaire(), quantity);
    }

    // ------------------------------------------------------------------ //
    //  CRUD
    // ------------------------------------------------------------------ //

    public List<Promotion> getAllPromotions() {
        return promotionDAO.getAll();
    }

    /** @return null si succès, message d'erreur sinon */
    public String saveWithError(Promotion p) {
        if (p.getIdPromotion() == 0) return promotionDAO.insertWithError(p);
        return promotionDAO.updateWithError(p);
    }

    public boolean save(Promotion p) {
        return saveWithError(p) == null;
    }

    public boolean delete(int idPromotion) {
        return promotionDAO.delete(idPromotion);
    }
}
