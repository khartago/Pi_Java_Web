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
            ->add('nom', TextType::class, ['label' => 'Nom du materiel'])
            ->add('etat', ChoiceType::class, [
                'label' => 'Etat',
                'choices' => ['Neuf' => 'neuf', 'Usage' => 'usagé', 'Bon' => 'bon', 'En panne' => 'panne'],
            ])
            ->add('dateAchat', DateType::class, [
                'label' => 'Date achat',
                'widget' => 'single_text',
                'input' => 'datetime_immutable',
            ])
            ->add('cout', NumberType::class, ['label' => 'Cout', 'scale' => 2]);
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults(['data_class' => Materiel::class]);
    }
}
