<?php

namespace App\Form;

use App\Entity\Produit;
use App\Entity\Promotion;
use Symfony\Bridge\Doctrine\Form\Type\EntityType;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\Extension\Core\Type\CheckboxType;
use Symfony\Component\Form\Extension\Core\Type\ChoiceType;
use Symfony\Component\Form\Extension\Core\Type\DateType;
use Symfony\Component\Form\Extension\Core\Type\IntegerType;
use Symfony\Component\Form\Extension\Core\Type\NumberType;
use Symfony\Component\Form\Extension\Core\Type\TextType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;

class PromotionType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder
            ->add('nom', TextType::class)
            ->add('typeReduction', ChoiceType::class, [
                'choices' => ['Pourcentage' => Promotion::TYPE_POURCENTAGE, 'Montant fixe' => Promotion::TYPE_MONTANT_FIXE],
            ])
            ->add('valeurReduction', NumberType::class, ['scale' => 2])
            ->add('dateDebut', DateType::class, ['widget' => 'single_text', 'input' => 'datetime_immutable'])
            ->add('dateFin', DateType::class, ['widget' => 'single_text', 'input' => 'datetime_immutable'])
            ->add('quantiteMin', IntegerType::class, ['empty_data' => '1'])
            ->add('cumulable', CheckboxType::class, ['required' => false])
            ->add('actif', CheckboxType::class, ['required' => false])
            ->add('produits', EntityType::class, [
                'class' => Produit::class,
                'choice_label' => 'nom',
                'multiple' => true,
                'by_reference' => false,
                'query_builder' => static fn ($repo) => $repo->createQueryBuilder('p')->orderBy('p.nom', 'ASC'),
            ]);
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults(['data_class' => Promotion::class]);
    }
}
