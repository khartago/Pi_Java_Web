<?php

namespace App\Tests\Unit;

use App\Service\AssistantIntentRouter;
use PHPUnit\Framework\TestCase;

final class AssistantIntentRouterTest extends TestCase
{
    private AssistantIntentRouter $router;

    protected function setUp(): void
    {
        $this->router = new AssistantIntentRouter();
    }

    /** @dataProvider stockListProvider */
    public function testDetectsStockList(string $message): void
    {
        $intent = $this->router->detect($message);
        $this->assertSame('stock.list', $intent->name, "Expected stock.list for: $message");
    }

    /** @return list<array{string}> */
    public static function stockListProvider(): array
    {
        return [
            ["Qu'est-ce que j'ai en stock ?"],
            ["montre moi l'inventaire"],
            ["liste des produits"],
            ["mes stocks"],
            ["what do i have"],
            ["show me the stock"],
            ["list products"],
        ];
    }

    /** @dataProvider stockSearchProvider */
    public function testDetectsStockSearch(string $message, string $expectedQuery): void
    {
        $intent = $this->router->detect($message);
        $this->assertSame('stock.search', $intent->name, "Expected stock.search for: $message");
        $this->assertStringContainsStringIgnoringCase($expectedQuery, (string) $intent->param('query'));
    }

    /** @return list<array{string, string}> */
    public static function stockSearchProvider(): array
    {
        return [
            ['combien de blé ?', 'blé'],
            ["est-ce que j'ai du maïs", 'maïs'],
            ['how much wheat do i have', 'wheat'],
            ['do i have fertilizer', 'fertilizer'],
            ['cherche engrais', 'engrais'],
        ];
    }

    /** @dataProvider stockLowProvider */
    public function testDetectsStockLow(string $message): void
    {
        $intent = $this->router->detect($message);
        $this->assertSame('stock.low', $intent->name, "Expected stock.low for: $message");
    }

    /** @return list<array{string}> */
    public static function stockLowProvider(): array
    {
        return [
            ['Quels produits sont en rupture ?'],
            ['stock bas'],
            ['alerte stock'],
            ['low stock alert'],
            ['running out of seeds'],
            ['presque vide'],
        ];
    }

    /** @dataProvider stockExpiringProvider */
    public function testDetectsStockExpiring(string $message, int $expectedDays): void
    {
        $intent = $this->router->detect($message);
        $this->assertSame('stock.expiring', $intent->name, "Expected stock.expiring for: $message");
        $this->assertSame($expectedDays, $intent->param('withinDays'));
    }

    /** @return list<array{string, int}> */
    public static function stockExpiringProvider(): array
    {
        return [
            ["Qu'est-ce qui expire ce mois-ci ?", 30],
            ['produits qui périme bientôt', 30],
            ['what expires in 7 days', 7],
            ['expiring this week', 7],
            ['date limite dépassée bientôt', 30],
        ];
    }

    /** @dataProvider materielBrokenProvider */
    public function testDetectsMaterielBroken(string $message): void
    {
        $intent = $this->router->detect($message);
        $this->assertSame('materiel.broken', $intent->name, "Expected materiel.broken for: $message");
    }

    /** @return list<array{string}> */
    public static function materielBrokenProvider(): array
    {
        return [
            ['Quel matériel est en panne ?'],
            ['matériel cassé'],
            ['what equipment is broken'],
            ['equipment down'],
            ['my tractor is out of order'],
        ];
    }

    /** @dataProvider materielCostProvider */
    public function testDetectsMaterielCost(string $message): void
    {
        $intent = $this->router->detect($message);
        $this->assertSame('materiel.cost', $intent->name, "Expected materiel.cost for: $message");
    }

    /** @return list<array{string}> */
    public static function materielCostProvider(): array
    {
        return [
            ['coût matériel total'],
            ['combien coûte mon matériel'],
            ['equipment cost'],
            ['cost of equipment'],
        ];
    }

    public function testFallsBackToUnknown(): void
    {
        $intent = $this->router->detect('What is the capital of France?');
        $this->assertSame('unknown', $intent->name);
    }

    public function testParamHelperReturnsDefault(): void
    {
        $intent = $this->router->detect('unknown question');
        $this->assertNull($intent->param('withinDays'));
        $this->assertSame(99, $intent->param('withinDays', 99));
    }
}
