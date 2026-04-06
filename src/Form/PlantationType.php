<?php

namespace App\Form;

use App\Entity\Plantation;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\Extension\Core\Type\TextType;
use Symfony\Component\Form\Extension\Core\Type\IntegerType;
use Symfony\Component\Form\Extension\Core\Type\DateType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;

class PlantationType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder
            ->add('nomPlant', TextType::class)
            ->add('variete', TextType::class)
            ->add('quantite', IntegerType::class)
            ->add('datePlante', DateType::class, [
                'widget' => 'single_text',
            ])
            ->add('saison', TextType::class);
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => Plantation::class,
        ]);
    }
}