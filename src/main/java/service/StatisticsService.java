package service;

import model.Produit;
import model.ProduitDAO;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Service de statistiques pour l'analyse des produits
 * Calcul de KPIs, produits à risque, distribution par catégorie, etc.
 */
public class StatisticsService {
    private final ProduitDAO produitDAO;
    private final int daysBeforeExpiration;
    private final int minStockQuantity;

    public StatisticsService(ProduitDAO produitDAO, int daysBeforeExpiration, int minStockQuantity) {
        this.produitDAO = produitDAO;
        this.daysBeforeExpiration = daysBeforeExpiration;
        this.minStockQuantity = minStockQuantity;
    }

    public StatisticsService(ProduitDAO produitDAO) {
        this(produitDAO, 7, 10); // Valeurs par défaut
    }

    /**
     * Récupère le nombre total de produits
     */
    public int getTotalProducts() {
        return produitDAO.getAll().size();
    }

    /**
     * Calcule la quantité totale de stock
     */
    public int getTotalStock() {
        return produitDAO.getAll().stream()
                .mapToInt(Produit::getQuantite)
                .sum();
    }

    /**
     * Récupère les produits proches de l'expiration
     */
    public List<Produit> getExpiringProducts() {
        LocalDate threshold = LocalDate.now().plusDays(daysBeforeExpiration);
        return produitDAO.getAll().stream()
                .filter(p -> p.getDateExpiration() != null &&
                           !p.getDateExpiration().isAfter(threshold) &&
                           p.getDateExpiration().isAfter(LocalDate.now()))
                .sorted(Comparator.comparing(Produit::getDateExpiration))
                .toList();
    }

    /**
     * Récupère les produits expirés
     */
    public List<Produit> getExpiredProducts() {
        return produitDAO.getAll().stream()
                .filter(p -> p.getDateExpiration() != null &&
                           p.getDateExpiration().isBefore(LocalDate.now()))
                .sorted(Comparator.comparing(Produit::getDateExpiration))
                .toList();
    }

    /**
     * Récupère les produits à réapprovisionner (stock faible)
     */
    public List<Produit> getLowStockProducts() {
        return produitDAO.getAll().stream()
                .filter(p -> p.getQuantite() <= minStockQuantity)
                .sorted(Comparator.comparing(Produit::getQuantite))
                .toList();
    }

    /**
     * Nombre de produits proches de l'expiration
     */
    public int getExpiringProductCount() {
        return getExpiringProducts().size();
    }

    /**
     * Nombre de produits expirés
     */
    public int getExpiredProductCount() {
        return getExpiredProducts().size();
    }

    /**
     * Nombre de produits en rupture de stock
     */
    public int getLowStockProductCount() {
        return getLowStockProducts().size();
    }

    /**
     * Stock moyen par produit
     */
    public double getAverageStock() {
        List<Produit> all = produitDAO.getAll();
        if (all.isEmpty()) return 0;
        return (double) getTotalStock() / all.size();
    }

    /**
     * Distribution des produits par unité (ex: kg, l, m², etc.)
     */
    public Map<String, Integer> getProductsByUnit() {
        Map<String, Integer> distribution = new HashMap<>();
        produitDAO.getAll().forEach(p ->
            distribution.put(p.getUnite(), distribution.getOrDefault(p.getUnite(), 0) + 1)
        );
        return distribution;
    }

    /**
     * Produits triés par quantité (du plus au moins stocké)
     */
    public List<Produit> getProductsSortedByQuantity(boolean descending) {
        List<Produit> all = produitDAO.getAll();
        if (descending) {
            return all.stream()
                    .sorted(Comparator.comparing(Produit::getQuantite).reversed())
                    .toList();
        } else {
            return all.stream()
                    .sorted(Comparator.comparing(Produit::getQuantite))
                    .toList();
        }
    }

    /**
     * Produits triés par date d'expiration (plus proches d'abord)
     */
    public List<Produit> getProductsSortedByExpiration() {
        return produitDAO.getAll().stream()
                .filter(p -> p.getDateExpiration() != null)
                .sorted(Comparator.comparing(Produit::getDateExpiration))
                .toList();
    }

    /**
     * Calcule les jours restants avant expiration pour chaque produit
     */
    public Map<Produit, Long> getDaysBeforeExpiration() {
        Map<Produit, Long> map = new HashMap<>();
        LocalDate today = LocalDate.now();
        produitDAO.getAll().forEach(p -> {
            if (p.getDateExpiration() != null) {
                long days = ChronoUnit.DAYS.between(today, p.getDateExpiration());
                map.put(p, days);
            }
        });
        return map;
    }

    /**
     * Rapport de santé générale du stock (%)
     * 100% = tout en ordre, 0% = problèmes critiques
     */
    public double getHealthScore() {
        int total = getTotalProducts();
        if (total == 0) return 100;

        int problems = getExpiringProductCount() + getExpiredProductCount() + getLowStockProductCount();
        return Math.max(0, 100 - (problems * 100.0 / total));
    }

    /**
     * Stock moyen par unité
     */
    public Map<String, Double> getAverageStockByUnit() {
        Map<String, List<Produit>> grouped = new HashMap<>();
        produitDAO.getAll().forEach(p ->
            grouped.computeIfAbsent(p.getUnite(), k -> new ArrayList<>()).add(p)
        );

        Map<String, Double> result = new HashMap<>();
        grouped.forEach((unit, products) -> {
            double avg = products.stream()
                    .mapToInt(Produit::getQuantite)
                    .average()
                    .orElse(0);
            result.put(unit, avg);
        });
        return result;
    }

    /**
     * Produits avec prix unitaire (si disponible)
     */
    public List<Produit> getProductsWithPrice() {
        return produitDAO.getAll().stream()
                .filter(p -> p.getPrixUnitaire() > 0)
                .sorted(Comparator.comparing(Produit::getPrixUnitaire).reversed())
                .toList();
    }

    /**
     * Valeur totale du stock (quantité * prix unitaire)
     */
    public double getTotalStockValue() {
        return produitDAO.getAll().stream()
                .mapToDouble(p -> p.getQuantite() * p.getPrixUnitaire())
                .sum();
    }

    /**
     * Produit le plus cher
     */
    public Optional<Produit> getMostExpensiveProduct() {
        return produitDAO.getAll().stream()
                .max(Comparator.comparing(Produit::getPrixUnitaire));
    }

    /**
     * Produit le moins cher (avec prix > 0)
     */
    public Optional<Produit> getCheapestProduct() {
        return produitDAO.getAll().stream()
                .filter(p -> p.getPrixUnitaire() > 0)
                .min(Comparator.comparing(Produit::getPrixUnitaire));
    }
}

