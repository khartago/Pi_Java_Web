package Services;

import model.Produit;
import model.ProduitDAO;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Statistics for products: KPIs, charts data, stock value.
 */
public class StatisticsService {

    private final ProduitDAO produitDAO;

    public StatisticsService(ProduitDAO produitDAO) {
        this.produitDAO = produitDAO;
    }

    public int getTotalProducts() {
        return produitDAO.getAll().size();
    }

    public int getTotalStock() {
        return produitDAO.getAll().stream()
                .mapToInt(Produit::getQuantite)
                .sum();
    }

    public double getAverageStock() {
        List<Produit> all = produitDAO.getAll();
        if (all.isEmpty()) return 0;
        return (double) getTotalStock() / all.size();
    }

    /** Score 0–100: fewer expired/expiring/low stock = higher. */
    public double getHealthScore() {
        List<Produit> all = produitDAO.getAll();
        if (all.isEmpty()) return 100.0;
        int total = all.size();
        int expired = getExpiredProductCount();
        int expiring = getExpiringProductCount();
        int lowStock = getLowStockProductCount();
        int bad = expired + expiring + lowStock;
        if (bad >= total) return 0;
        return 100.0 * (total - bad) / total;
    }

    public int getExpiredProductCount() {
        return (int) produitDAO.getAll().stream()
                .filter(p -> p.getDateExpiration() != null && p.getDateExpiration().isBefore(LocalDate.now()))
                .count();
    }

    public int getExpiringProductCount() {
        LocalDate now = LocalDate.now();
        LocalDate in7 = now.plusDays(7);
        return (int) produitDAO.getAll().stream()
                .filter(p -> p.getDateExpiration() != null
                        && !p.getDateExpiration().isBefore(now)
                        && !p.getDateExpiration().isAfter(in7))
                .count();
    }

    public int getLowStockProductCount() {
        return (int) produitDAO.getAll().stream()
                .filter(p -> p.getQuantite() < 5)
                .count();
    }

    public double getTotalStockValue() {
        return produitDAO.getAll().stream()
                .mapToDouble(p -> p.getQuantite() * p.getPrixUnitaire())
                .sum();
    }

    public List<Produit> getProductsSortedByQuantity(boolean descending) {
        Comparator<Produit> byQty = Comparator.comparingInt(Produit::getQuantite);
        return produitDAO.getAll().stream()
                .sorted(descending ? byQty.reversed() : byQty)
                .collect(Collectors.toList());
    }

    public Map<String, Long> getProductsByUnit() {
        return produitDAO.getAll().stream()
                .collect(Collectors.groupingBy(
                        p -> p.getUnite() == null || p.getUnite().isBlank() ? "-" : p.getUnite(),
                        Collectors.counting()));
    }
}
