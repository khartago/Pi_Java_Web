<?php

namespace App\Command;

use App\Service\AiAssistantClient;
use Psr\Log\LoggerInterface;
use Symfony\Component\Console\Attribute\AsCommand;
use Symfony\Component\Console\Command\Command;
use Symfony\Component\Console\Input\InputInterface;
use Symfony\Component\Console\Output\OutputInterface;

#[AsCommand(
    name: 'app:assistant:test',
    description: 'Test the assistant connection and log the response.',
)]
final class AssistantTestCommand extends Command
{
    public function __construct(
        private readonly AiAssistantClient $client,
        private readonly LoggerInterface $logger,
    ) {
        parent::__construct();
    }

    protected function execute(InputInterface $input, OutputInterface $output): int
    {
        try {
            $reply = $this->client->chat('You are a test assistant.', [
                ['role' => 'user', 'content' => 'Say hello in one sentence.'],
            ]);

            $output->writeln($reply);
            $this->logger->info('Assistant test reply.', ['reply' => $reply]);

            return Command::SUCCESS;
        } catch (\Throwable $exception) {
            $output->writeln('Assistant test failed: ' . $exception->getMessage());
            $this->logger->error('Assistant test failed.', ['error' => $exception->getMessage()]);

            return Command::FAILURE;
        }
    }
}
