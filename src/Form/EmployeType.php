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
            ->add('nom', TextType::class, [
                'label' => 'Nom',
                'attr' => [
                    'placeholder' => 'Ex: Ben Ali',
                    'class' => 'input',
                ],
            ])
            ->add('prenom', TextType::class, [
                'label' => 'Prénom',
                'attr' => [
                    'placeholder' => 'Ex: Karim',
                    'class' => 'input',
                ],
            ])
            ->add('poste', ChoiceType::class, [
                'label' => 'Poste',
                'choices' => [
                    'Technicien terrain' => 'Technicien terrain',
                    'Responsable achat' => 'Responsable achat',
                    'Agronome' => 'Agronome',
                    'Superviseur logistique' => 'Superviseur logistique',
                    'Autre' => 'Autre',
                ],
                'placeholder' => 'Choisir un poste',
                'attr' => [
                    'class' => 'select',
                ],
            ])
            ->add('email', EmailType::class, [
                'label' => 'Email',
                'required' => false,
                'attr' => [
                    'placeholder' => 'ex: employe@farmtech.tn',
                    'class' => 'input',
                ],
            ]);
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => Employe::class,
        ]);
    }
}
