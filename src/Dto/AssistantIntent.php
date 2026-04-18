<?php

namespace App\Dto;

final class AssistantIntent
{
    /** @param array<string, mixed> $params */
    public function __construct(
        public readonly string $name,
        public readonly array $params = [],
    ) {
    }

    public function is(string $name): bool
    {
        return $this->name === $name;
    }

    public function param(string $key, mixed $default = null): mixed
    {
        return $this->params[$key] ?? $default;
    }
}
