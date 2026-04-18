<?php

namespace App\Command;

use App\Service\CriticalAlertNotifier;
use Symfony\Component\Console\Attribute\AsCommand;
use Symfony\Component\Console\Command\Command;
use Symfony\Component\Console\Input\InputInterface;
use Symfony\Component\Console\Output\OutputInterface;
use Symfony\Component\Console\Style\SymfonyStyle;

#[AsCommand(
    name: 'app:check-stock',
    description: 'Vérifie le stock et envoie une alerte email si nécessaire.',
)]
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
        $io->writeln('Vérification du stock...');

        try {
            $result = $this->notifier->sendCriticalAlerts(
                $this->alertFromEmail,
                $this->alertToEmail,
                $this->stockThreshold,
            );
        } catch (\Throwable $exception) {
            $io->error('Echec envoi alertes: '.$exception->getMessage());

            return Command::FAILURE;
        }

        if (!$result['sent']) {
            $io->success('Aucune alerte critique a envoyer.');

            return Command::SUCCESS;
        }

        $io->success(sprintf(
            'Alertes envoyees vers %s (%d stock bas, %d pannes).',
            $this->alertToEmail,
            $result['low_stock_count'],
            $result['panne_count'],
        ));

        return Command::SUCCESS;
    }
}