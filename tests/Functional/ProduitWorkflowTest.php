<?php

namespace App\Tests\Functional;

use App\Entity\Materiel;
use App\Entity\Produit;
use App\Repository\MaterielRepository;
use App\Repository\ProduitRepository;

class ProduitWorkflowTest extends DatabaseWebTestCase
{
    public function testIndexShowsEmptyStateWhenNoProduitExists(): void
    {
        $this->client->request('GET', '/produits');

        self::assertResponseIsSuccessful();
        self::assertSelectorTextContains('h3', 'Aucun produit à afficher');
    }

    public function testCreateProduitThenCreateMaterielFromProductContext(): void
    {
        $crawler = $this->client->request('GET', '/produits/nouveau');
        self::assertResponseIsSuccessful();

        $this->client->submitForm('Créer le produit', [
            'produit[nom]' => 'Lait fermier',
            'produit[quantite]' => 24,
            'produit[unite]' => 'l',
            'produit[dateExpiration]' => '2026-05-10',
        ]);

        self::assertResponseRedirects();
        $this->client->followRedirect();
        self::assertSelectorTextContains('.detail-panel__title', 'Lait fermier');

        $produit = static::getContainer()->get(ProduitRepository::class)->findOneBy(['nom' => 'Lait fermier']);
        self::assertNotNull($produit);

        $this->client->request('GET', sprintf('/produits/%d/materiels/nouveau', $produit->getIdProduit()));
        $this->client->submitForm('Ajouter le matériel', [
            'materiel[nom]' => 'Cuve inox',
            'materiel[etat]' => 'bon',
            'materiel[dateAchat]' => '2026-04-05',
            'materiel[cout]' => '2100.50',
        ]);

        self::assertResponseRedirects(sprintf('/produits/%d', $produit->getIdProduit()));
        $this->client->followRedirect();
        self::assertSelectorTextContains('table tbody tr td', 'Cuve inox');
    }

    public function testDeleteProduitAlsoDeletesLinkedMateriels(): void
    {
        $produit = (new Produit())
            ->setNom('Engrais premium')
            ->setQuantite(8)
            ->setUnite('kg');

        $materiel = (new Materiel())
            ->setNom('Doseur')
            ->setEtat('neuf')
            ->setDateAchat(new \DateTimeImmutable('2026-04-01'))
            ->setCout(95.5)
            ->setProduit($produit);

        $this->entityManager->persist($produit);
        $this->entityManager->persist($materiel);
        $this->entityManager->flush();

        $crawler = $this->client->request('GET', sprintf('/produits/%d', $produit->getIdProduit()));
        $form = $crawler->filter(sprintf('form[action="/produits/%d/supprimer"]', $produit->getIdProduit()))->form();
        $this->client->submit($form);

        self::assertResponseRedirects('/produits');
        $this->client->followRedirect();

        self::assertNull(static::getContainer()->get(ProduitRepository::class)->find($produit->getIdProduit()));
        self::assertNull(static::getContainer()->get(MaterielRepository::class)->find($materiel->getIdMateriel()));
    }

    public function testIndexSupportsSearchAndUnitFilters(): void
    {
        $this->entityManager->persist((new Produit())->setNom('Lait entier')->setQuantite(15)->setUnite('l'));
        $this->entityManager->persist((new Produit())->setNom('Semence blé')->setQuantite(40)->setUnite('kg'));
        $this->entityManager->flush();

        $this->client->request('GET', '/produits?recherche=lait&unite=l');

        self::assertResponseIsSuccessful();
        self::assertSelectorTextContains('.product-link__name', 'Lait entier');
        self::assertSelectorTextNotContains('body', 'Semence blé');
    }

    public function testInvalidFormsExposeValidationErrors(): void
    {
        $this->client->request('GET', '/produits/nouveau');
        $this->client->submitForm('Créer le produit', [
            'produit[nom]' => '',
            'produit[quantite]' => -1,
            'produit[unite]' => '',
            'produit[dateExpiration]' => '',
        ]);

        self::assertResponseStatusCodeSame(422);
        $content = (string) $this->client->getResponse()->getContent();
        self::assertStringContainsString('Le nom du produit est requis.', $content);
        self::assertStringContainsString('La quantité doit être positive ou nulle.', $content);
        self::assertStringContainsString('L’unité est requise.', $content);
    }

    public function testPrimaryCrudActionsRemainVisibleForCompactClients(): void
    {
        $produit = (new Produit())
            ->setNom('Pompe mobile')
            ->setQuantite(1)
            ->setUnite('piece');

        $this->entityManager->persist($produit);
        $this->entityManager->flush();

        $this->client->setServerParameter('HTTP_USER_AGENT', 'Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X)');
        $this->client->request('GET', sprintf('/produits/%d', $produit->getIdProduit()));

        self::assertResponseIsSuccessful();
        self::assertSelectorExists(sprintf('a[href="/produits/%d/modifier"]', $produit->getIdProduit()));
        self::assertSelectorExists(sprintf('a[href="/produits/%d/materiels/nouveau"]', $produit->getIdProduit()));
        self::assertSelectorExists(sprintf('form[action="/produits/%d/supprimer"]', $produit->getIdProduit()));
    }

    public function testQrCodeEndpointReturnsSvgForProduit(): void
    {
        $produit = (new Produit())
            ->setNom('Farine bio')
            ->setQuantite(12)
            ->setUnite('kg');

        $this->entityManager->persist($produit);
        $this->entityManager->flush();

        $this->client->request('GET', sprintf('/produits/%d/qr-code', $produit->getIdProduit()));

        self::assertResponseIsSuccessful();
        self::assertResponseHeaderSame('content-type', 'image/svg+xml');

        $content = (string) $this->client->getResponse()->getContent();
        self::assertStringContainsString('<svg', $content);
        self::assertGreaterThan(500, strlen($content));
    }

    public function testSendAlertEmailEndpointRedirectsWithValidCsrf(): void
    {
        $crawler = $this->client->request('GET', '/produits');
        self::assertResponseIsSuccessful();

        $form = $crawler->filter('form[action="/produits/alertes/email"]')->form();
        $this->client->submit($form);

        self::assertResponseRedirects();
    }
}
