<?php

namespace App\Tests\Functional;

use App\Entity\Materiel;
use App\Entity\Produit;

class MarketplaceTest extends DatabaseWebTestCase
{
    public function testMarketplaceRendersCardsWithComputedCategoryAndMaterialCount(): void
    {
        $liquidProduct = (new Produit())
            ->setNom('Lait fermier')
            ->setQuantite(20)
            ->setUnite('l');

        $seedProduct = (new Produit())
            ->setNom('Semence ble')
            ->setQuantite(40)
            ->setUnite('kg');

        $linkedMateriel = (new Materiel())
            ->setNom('Cuve inox')
            ->setEtat('bon')
            ->setDateAchat(new \DateTimeImmutable('2026-04-02'))
            ->setCout(850.5)
            ->setProduit($liquidProduct);

        $liquidProduct->addMateriel($linkedMateriel);

        $this->entityManager->persist($liquidProduct);
        $this->entityManager->persist($seedProduct);
        $this->entityManager->persist($linkedMateriel);
        $this->entityManager->flush();

        $this->client->request('GET', '/marketplace');

        self::assertResponseIsSuccessful();
        $content = (string) $this->client->getResponse()->getContent();
        self::assertStringContainsString('Lait fermier', $content);
        self::assertStringContainsString('Semence ble', $content);
        self::assertStringContainsString('Liquides', $content);
        self::assertStringContainsString('Semences', $content);
        self::assertStringContainsString('Cuve inox', $content);
        self::assertStringContainsString('/images/product-placeholder.svg', $content);
        self::assertSelectorExists('.market-card__media img');
        self::assertSelectorExists('.market-chip');
    }

    public function testMarketplaceCategoryFilterNarrowsVisibleCards(): void
    {
        $liquidProduct = (new Produit())
            ->setNom('Lait entier')
            ->setQuantite(16)
            ->setUnite('l');

        $equipmentProduct = (new Produit())
            ->setNom('Pompe mobile')
            ->setQuantite(2)
            ->setUnite('piece');

        $this->entityManager->persist($liquidProduct);
        $this->entityManager->persist($equipmentProduct);
        $this->entityManager->flush();

        $this->client->request('GET', '/marketplace?categorie=Liquides');

        self::assertResponseIsSuccessful();
        self::assertSelectorTextContains('.market-card__title', 'Lait entier');
        self::assertSelectorTextNotContains('body', 'Pompe mobile');
    }

    public function testMarketplaceSupportsSearchAndUnitFilters(): void
    {
        $this->entityManager->persist((new Produit())->setNom('Engrais premium')->setQuantite(9)->setUnite('kg'));
        $this->entityManager->persist((new Produit())->setNom('Huile agricole')->setQuantite(12)->setUnite('l'));
        $this->entityManager->flush();

        $this->client->request('GET', '/marketplace?recherche=engrais&unite=kg');

        self::assertResponseIsSuccessful();
        self::assertSelectorTextContains('.market-card__title', 'Engrais premium');
        self::assertSelectorTextNotContains('body', 'Huile agricole');
    }
}
