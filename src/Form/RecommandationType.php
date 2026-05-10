<?php

namespace App\Form;

use App\Entity\Materiel;
use App\Entity\Produit;
use App\Entity\Recommandation;
use Symfony\Bridge\Doctrine\Form\Type\EntityType;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\Extension\Core\Type\CheckboxType;
use Symfony\Component\Form\Extension\Core\Type\ChoiceType;
use Symfony\Component\Form\Extension\Core\Type\TextareaType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;

final class RecommandationType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder
            ->add('produit', EntityType::class, ['class' => Produit::class, 'choice_label' => 'nom'])
            ->add('materiel', EntityType::class, ['class' => Materiel::class, 'choice_label' => fn (Materiel $m) => $m->getNom().' ('.$m->getEtat().')'])
            ->add('priorite', ChoiceType::class, ['choices' => ['5 Essentiel' => 5, '4 Important' => 4, '3 Recommande' => 3, '2 Utile' => 2, '1 Optionnel' => 1]])
            ->add('raison', TextareaType::class, ['required' => false])
            ->add('actif', CheckboxType::class, ['required' => false]);
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults(['data_class' => Recommandation::class]);
    }
}
