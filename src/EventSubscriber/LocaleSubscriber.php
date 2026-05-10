<?php

namespace App\EventSubscriber;

use Symfony\Component\EventDispatcher\EventSubscriberInterface;
use Symfony\Component\HttpKernel\Event\RequestEvent;
use Symfony\Component\HttpKernel\KernelEvents;

class LocaleSubscriber implements EventSubscriberInterface
{
    private const SUPPORTED = ['fr', 'en', 'ar'];

    public static function getSubscribedEvents(): array
    {
        return [
            KernelEvents::REQUEST => [['onKernelRequest', 20]],
        ];
    }

    public function onKernelRequest(RequestEvent $event): void
    {
        if (!$event->isMainRequest()) {
            return;
        }

        $request = $event->getRequest();
        $session = $request->hasSession() ? $request->getSession() : null;
        $queryLocale = $request->query->get('_locale');

        if (is_string($queryLocale) && in_array($queryLocale, self::SUPPORTED, true)) {
            $request->setLocale($queryLocale);
            if ($session !== null) {
                $session->set('_locale', $queryLocale);
            }
            return;
        }

        if ($session !== null) {
            $storedLocale = $session->get('_locale');
            if (is_string($storedLocale) && in_array($storedLocale, self::SUPPORTED, true)) {
                $request->setLocale($storedLocale);
            }
        }
    }
}
