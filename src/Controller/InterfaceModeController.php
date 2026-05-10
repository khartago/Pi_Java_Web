<?php

namespace App\Controller;

use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\RedirectResponse;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\Routing\Attribute\Route;

final class InterfaceModeController extends AbstractController
{
    #[Route('/interface/mode/{mode<admin|user>}', name: 'app_interface_mode_switch', methods: ['GET'])]
    public function switch(string $mode, Request $request): RedirectResponse
    {
        if ($request->hasSession()) {
            $request->getSession()->set('ui_mode', $mode);
        }
        $this->addFlash('success', sprintf('Interface %s activee.', $mode === 'admin' ? 'Admin' : 'User'));

        return $mode === 'user' ? $this->redirectToRoute('app_marketplace_index') : $this->redirectToRoute('app_produit_index');
    }
}
