<?php

namespace App\Service;

use App\Entity\Diagnostique;
use App\Entity\Probleme;
use App\Repository\DiagnostiqueRepository;

/**
 * Transitions automatiques de {@see Probleme::etat} liées aux diagnostics et au feedback fermier.
 */
final class ProblemeWorkflowService
{
    public function __construct(
        private readonly DiagnostiqueRepository $diagnostiqueRepository,
    ) {
    }

    public function applyAfterDiagnosticChange(Probleme $probleme): void
    {
        $hasApproved = $this->diagnostiqueRepository->countApprovedForProbleme((int) $probleme->getId()) > 0;
        $etat = $probleme->getEtat();

        if ('CLOTURE' === $etat) {
            return;
        }

        if ($hasApproved) {
            if (\in_array($etat, ['EN_ATTENTE', 'REOUVERT'], true)) {
                $probleme->setEtat('DIAGNOSTIQUE_DISPONIBLE');
            }

            return;
        }

        if (\in_array($etat, ['DIAGNOSTIQUE_DISPONIBLE', 'REOUVERT'], true)) {
            $probleme->setEtat('EN_ATTENTE');
        }
    }

    public function applyAfterFarmerFeedback(Diagnostique $diagnostique): void
    {
        $probleme = $diagnostique->getProbleme();
        if (null === $probleme) {
            return;
        }

        $fb = $diagnostique->getFeedbackFermier();
        if ('RESOLU' === $fb) {
            $probleme->setEtat('CLOTURE');
        } elseif ('NON_RESOLU' === $fb) {
            $probleme->setEtat('REOUVERT');
        }
    }
}
