<?php

namespace App\Form;

use App\Entity\Employe;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\Extension\Core\Type\ChoiceType;
use Symfony\Component\Form\Extension\Core\Type\EmailType;
use Symfony\Component\Form\Extension\Core\Type\TextType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;

class EmployeType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder
            ->add('nom', TextType::class)
            ->add('prenom', TextType::class)
            ->add('poste', ChoiceType::class, [
                'choices' => [
                    'Technicien terrain' => 'Technicien terrain',
                    'Responsable achat' => 'Responsable achat',
                    'Agronome' => 'Agronome',
                    'Superviseur logistique' => 'Superviseur logistique',
                    'Autre' => 'Autre',
                ],
            ])
            ->add('email', EmailType::class, ['required' => false]);
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults(['data_class' => Employe::class]);
    }
}
