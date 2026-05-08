<?php

namespace App\Command;

use App\Service\CriticalAlertNotifier;
use Symfony\Component\Console\Attribute\AsCommand;
use Symfony\Component\Console\Command\Command;
use Symfony\Component\Console\Input\InputInterface;
use Symfony\Component\Console\Output\OutputInterface;
use Symfony\Component\Console\Style\SymfonyStyle;

#[AsCommand(name: 'app:check-stock', description: 'Verifie le stock et envoie une alerte email si necessaire.')]
class CheckStockCommand extends Command
{
    public function __construct(
        private readonly CriticalAlertNotifier $notifier,
        private readonly string $alertFromEmail,
        private readonly string $alertToEmail,
        private readonly int $stockThreshold,
    ) {
        parent::__construct();
    }

    protected function execute(InputInterface $input, OutputInterface $output): int
    {
        $io = new SymfonyStyle($input, $output);
        try {
            $result = $this->notifier->sendCriticalAlerts($this->alertFromEmail, $this->alertToEmail, $this->stockThreshold);
        } catch (\Throwable $exception) {
            $io->error('Echec envoi alertes: '.$exception->getMessage());
            return Command::FAILURE;
        }
        $io->success($result['sent'] ? 'Alertes envoyees.' : 'Aucune alerte critique.');
        return Command::SUCCESS;
    }
}
