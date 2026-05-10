<?php

namespace App\Dto;

final class PlantationStatsDto
{
    public readonly int $total;
    public readonly int $complete;
    public readonly int $attente;

    /**
     * @param int|float|string $total
     * @param int|float|string $complete
     * @param int|float|string $attente
     */
    public function __construct($total, $complete, $attente)
    {
        $this->total = (int) $total;
        $this->complete = (int) $complete;
        $this->attente = (int) $attente;
    }
}
