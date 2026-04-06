<?php

namespace App\Tests\Functional;

use Doctrine\ORM\EntityManagerInterface;
use Doctrine\ORM\Tools\SchemaTool;
use PDO;
use Symfony\Bundle\FrameworkBundle\KernelBrowser;
use Symfony\Bundle\FrameworkBundle\Test\WebTestCase;

abstract class DatabaseWebTestCase extends WebTestCase
{
    protected KernelBrowser $client;
    protected EntityManagerInterface $entityManager;

    protected function setUp(): void
    {
        parent::setUp();

        self::ensureKernelShutdown();
        $this->client = static::createClient();
        $this->entityManager = static::getContainer()->get(EntityManagerInterface::class);

        $this->rebuildSchema();
        $this->cleanUploadDirectory();
    }

    protected function tearDown(): void
    {
        $this->cleanUploadDirectory();
        $this->entityManager->close();
        unset($this->entityManager, $this->client);

        parent::tearDown();
    }

    private function rebuildSchema(): void
    {
        $this->ensureDatabaseExists();

        $metadata = $this->entityManager->getMetadataFactory()->getAllMetadata();
        $schemaTool = new SchemaTool($this->entityManager);

        if ($metadata === []) {
            return;
        }

        try {
            $schemaTool->dropSchema($metadata);
        } catch (\Throwable) {
        }

        $schemaTool->createSchema($metadata);
    }

    private function ensureDatabaseExists(): void
    {
        $params = $this->entityManager->getConnection()->getParams();
        $driver = (string) ($params['driver'] ?? '');

        if ($driver !== 'pdo_mysql') {
            return;
        }

        $host = (string) ($params['host'] ?? '127.0.0.1');
        $port = (int) ($params['port'] ?? 3306);
        $user = (string) ($params['user'] ?? 'root');
        $pass = (string) ($params['password'] ?? '');
        $databaseName = (string) ($params['dbname'] ?? '');

        if ($databaseName === '' && isset($params['url']) && is_string($params['url'])) {
            $parts = parse_url($params['url']);
            $databaseName = ltrim((string) ($parts['path'] ?? ''), '/');
        }

        if ($databaseName === '') {
            return;
        }

        $pdo = new PDO(
            sprintf('mysql:host=%s;port=%d;charset=utf8mb4', $host, $port),
            $user,
            $pass,
            [PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION],
        );

        $pdo->exec(sprintf(
            'CREATE DATABASE IF NOT EXISTS `%s` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci',
            str_replace('`', '``', $databaseName),
        ));
    }

    protected function cleanUploadDirectory(): void
    {
        $uploadDir = dirname(__DIR__, 2).'/public/uploads/products';
        $items = glob($uploadDir.'/*');

        if ($items === false) {
            return;
        }

        foreach ($items as $item) {
            if (is_file($item) && basename($item) !== '.gitignore') {
                @unlink($item);
            }
        }
    }
}
