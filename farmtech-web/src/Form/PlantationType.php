<?php

namespace App\Form;

use App\Entity\Plantation;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\Extension\Core\Type\DateType;
use Symfony\Component\Form\Extension\Core\Type\IntegerType;
use Symfony\Component\Form\Extension\Core\Type\TextType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;

class PlantationType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder
            ->add('nomPlant', TextType::class, [
                'label' => 'Nom',
                'attr' => ['class' => 'form-control'],
            ])
            ->add('variete', TextType::class, [
                'label' => 'Variete',
                'attr' => ['class' => 'form-control'],
            ])
            ->add('quantite', IntegerType::class, [
                'label' => 'Quantite',
                'attr' => ['class' => 'form-control'],
            ])
            ->add('datePlante', DateType::class, [
                'label' => 'Date plantation',
                'widget' => 'single_text',
                'attr' => ['class' => 'form-control'],
            ])
            ->add('saison', TextType::class, [
                'label' => 'Saison',
                'attr' => ['class' => 'form-control'],
            ]);
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => Plantation::class,
        ]);
    }
}
