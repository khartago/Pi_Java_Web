<?php

namespace App\Tests\Schema;

use App\Entity\Materiel;
use App\Entity\Produit;
use Doctrine\ORM\EntityManagerInterface;
use Doctrine\ORM\Tools\SchemaTool;
use Symfony\Bundle\FrameworkBundle\Test\KernelTestCase;

class SchemaMappingTest extends KernelTestCase
{
    public function testProduitAndMaterielUseLegacyTableAndColumnNames(): void
    {
        self::bootKernel();
        $entityManager = static::getContainer()->get(EntityManagerInterface::class);

        $produitMetadata = $entityManager->getClassMetadata(Produit::class);
        $materielMetadata = $entityManager->getClassMetadata(Materiel::class);

        self::assertSame('produit', $produitMetadata->getTableName());
        self::assertSame('idProduit', $produitMetadata->getColumnName('idProduit'));
        self::assertSame('nom', $produitMetadata->getColumnName('nom'));
        self::assertSame('quantite', $produitMetadata->getColumnName('quantite'));
        self::assertSame('unite', $produitMetadata->getColumnName('unite'));
        self::assertSame('dateExpiration', $produitMetadata->getColumnName('dateExpiration'));
        self::assertSame('imagePath', $produitMetadata->getColumnName('imagePath'));

        self::assertSame('materiel', $materielMetadata->getTableName());
        self::assertSame('idMateriel', $materielMetadata->getColumnName('idMateriel'));
        self::assertSame('nom', $materielMetadata->getColumnName('nom'));
        self::assertSame('etat', $materielMetadata->getColumnName('etat'));
        self::assertSame('dateAchat', $materielMetadata->getColumnName('dateAchat'));
        self::assertSame('cout', $materielMetadata->getColumnName('cout'));

        $association = $materielMetadata->getAssociationMapping('produit');
        $joinColumn = $association->joinColumns[0];
        self::assertSame('idProduit', $joinColumn->name);
        self::assertSame('idProduit', $joinColumn->referencedColumnName);
        self::assertSame('CASCADE', strtoupper((string) $joinColumn->onDelete));
    }

    public function testSchemaSqlContainsLegacyForeignKeyDefinition(): void
    {
        self::bootKernel();
        $entityManager = static::getContainer()->get(EntityManagerInterface::class);
        $schemaTool = new SchemaTool($entityManager);
        $sql = implode("\n", $schemaTool->getCreateSchemaSql($entityManager->getMetadataFactory()->getAllMetadata()));

        self::assertStringContainsStringIgnoringCase('CREATE TABLE produit', $sql);
        self::assertStringContainsStringIgnoringCase('CREATE TABLE materiel', $sql);
        self::assertStringContainsStringIgnoringCase('idProduit', $sql);
        self::assertStringContainsStringIgnoringCase('ON DELETE CASCADE', $sql);
    }
}
