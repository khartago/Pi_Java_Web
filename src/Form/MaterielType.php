<?php

namespace App\Form;

use App\Entity\Materiel;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\Extension\Core\Type\ChoiceType;
use Symfony\Component\Form\Extension\Core\Type\DateType;
use Symfony\Component\Form\Extension\Core\Type\NumberType;
use Symfony\Component\Form\Extension\Core\Type\TextType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;

class MaterielType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder
            ->add('nom', TextType::class, [
                'label' => 'Nom du matériel',
                'attr' => [
                    'placeholder' => 'Ex: Pompe d’irrigation',
                    'class' => 'input',
                ],
            ])
            ->add('etat', ChoiceType::class, [
                'label' => 'État',
                'choices' => [
                    'Neuf' => 'neuf',
                    'Usagé' => 'usagé',
                    'Bon' => 'bon',
                ],
                'placeholder' => 'Choisir un état',
                'attr' => [
                    'class' => 'select',
                ],
            ])
            ->add('dateAchat', DateType::class, [
                'label' => 'Date d’achat',
                'widget' => 'single_text',
                'input' => 'datetime_immutable',
                'attr' => [
                    'class' => 'date-input',
                    'max' => (new \DateTimeImmutable('today'))->format('Y-m-d'),
                ],
            ])
            ->add('cout', NumberType::class, [
                'label' => 'Coût',
                'scale' => 2,
                'attr' => [
                    'placeholder' => '0.00',
                    'class' => 'number-input',
                    'min' => 0,
                    'step' => '0.01',
                ],
            ]);
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => Materiel::class,
        ]);
    }
}
