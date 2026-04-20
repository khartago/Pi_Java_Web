<?php

namespace App\Form;

use App\Entity\Production;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\Extension\Core\Type\DateType;
use Symfony\Component\Form\Extension\Core\Type\NumberType;
use Symfony\Component\Form\Extension\Core\Type\TextType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;

class ProductionType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder
            ->add('quantiteProduite', NumberType::class, [
                'label' => 'Quantite produite',
                'attr' => ['class' => 'form-control'],
            ])
            ->add('dateRecolte', DateType::class, [
                'label' => 'Date recolte',
                'widget' => 'single_text',
                'attr' => ['class' => 'form-control'],
            ])
            ->add('qualite', TextType::class, [
                'label' => 'Qualite',
                'attr' => ['class' => 'form-control'],
            ])
            ->add('etat', TextType::class, [
                'label' => 'Etat',
                'attr' => ['class' => 'form-control'],
            ]);
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => Production::class,
        ]);
    }
}
