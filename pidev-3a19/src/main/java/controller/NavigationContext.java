package controller;

import model.Produit;

/**
 * Singleton class to store navigation context, such as the currently selected product.
 * This allows passing context between different views without tight coupling.
 */
public class NavigationContext {
    private static NavigationContext instance;
    private Produit selectedProduit;

    private NavigationContext() {
        // Private constructor for singleton pattern
    }

    /**
     * Gets the singleton instance of NavigationContext.
     *
     * @return the NavigationContext instance
     */
    public static NavigationContext getInstance() {
        if (instance == null) {
            instance = new NavigationContext();
        }
        return instance;
    }

    /**
     * Gets the currently selected product.
     *
     * @return the selected product, or null if none is selected
     */
    public Produit getSelectedProduit() {
        return selectedProduit;
    }

    /**
     * Sets the currently selected product.
     *
     * @param produit the product to set as selected
     */
    public void setSelectedProduit(Produit produit) {
        this.selectedProduit = produit;
    }

    /**
     * Clears the selected product.
     */
    public void clearSelectedProduit() {
        this.selectedProduit = null;
    }
}
