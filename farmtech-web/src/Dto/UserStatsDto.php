<?php

namespace App\Dto;

final class UserStatsDto
{
    public readonly int $total;
    public readonly int $adminCount;
    public readonly int $farmerCount;

    /**
     * @param int|float|string $total
     * @param int|float|string $adminCount
     * @param int|float|string $farmerCount
     */
    public function __construct($total, $adminCount, $farmerCount)
    {
        $this->total = (int) $total;
        $this->adminCount = (int) $adminCount;
        $this->farmerCount = (int) $farmerCount;
    }
}
