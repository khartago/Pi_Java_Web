<?php

namespace App\Tests\Service;

use App\Entity\Diagnostique;
use App\Service\DiagnostiqueManager;
use PHPUnit\Framework\TestCase;

final class DiagnostiqueManagerTest extends TestCase
{
    public function testValidDiagnostique(): void
    {
        $d = new Diagnostique();
        $d->setCause('Cause valide')->setSolutionProposee('Solution valide');
        $this->assertTrue((new DiagnostiqueManager())->validate($d));
    }

    public function testDiagnostiqueWithoutCauseOuSolution(): void
    {
        $this->expectException(\InvalidArgumentException::class);
        $d = new Diagnostique();
        $d->setCause('')->setSolutionProposee('Une solution');
        (new DiagnostiqueManager())->validate($d);
    }

    public function testDiagnostiqueWithFeedbackDateBeforeDiagnostique(): void
    {
        $this->expectException(\InvalidArgumentException::class);
        $d = new Diagnostique();
        $d->setCause('Cause')->setSolutionProposee('Solution');
        $d->setDateDiagnostique(new \DateTimeImmutable('2026-05-10 12:00:00'));
        $d->setDateFeedback(new \DateTimeImmutable('2026-05-01 12:00:00'));
        (new DiagnostiqueManager())->validate($d);
    }
}
