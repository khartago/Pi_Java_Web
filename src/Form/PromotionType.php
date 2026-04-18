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
use Symfony\Component\Form\Extension\Core\Type\TextareaType;
use Symfony\Component\Form\Extension\Core\Type\TextType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;

class PromotionType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder
            ->add('nom', TextType::class, [
                'label' => 'Nom de la promotion',
                'attr' => [
                    'placeholder' => 'Ex: Offre printemps',
                    'class' => 'input',
                ],
            ])
            ->add('description', TextareaType::class, [
                'label' => 'Description',
                'required' => false,
                'attr' => [
                    'placeholder' => 'Détails de la promotion (optionnel)',
                    'class' => 'input',
                    'rows' => 3,
                ],
            ])
            ->add('typeReduction', ChoiceType::class, [
                'label' => 'Type de réduction',
                'choices' => [
                    'Pourcentage' => Promotion::TYPE_POURCENTAGE,
                    'Montant fixe (€)' => Promotion::TYPE_MONTANT_FIXE,
                ],
                'attr' => [
                    'class' => 'select',
                ],
            ])
            ->add('valeurReduction', NumberType::class, [
                'label' => 'Valeur de la réduction',
                'scale' => 2,
                'attr' => [
                    'placeholder' => '0.00',
                    'class' => 'number-input',
                    'min' => 0,
                    'step' => '0.01',
                ],
            ])
            ->add('dateDebut', DateType::class, [
                'label' => 'Date de début',
                'widget' => 'single_text',
                'html5' => true,
                'input' => 'datetime_immutable',
                'attr' => [
                    'class' => 'date-input',
                ],
            ])
            ->add('dateFin', DateType::class, [
                'label' => 'Date de fin',
                'widget' => 'single_text',
                'html5' => true,
                'input' => 'datetime_immutable',
                'attr' => [
                    'class' => 'date-input',
                ],
            ])
            ->add('quantiteMin', IntegerType::class, [
                'label' => 'Quantité minimale',
                'empty_data' => '1',
                'attr' => [
                    'class' => 'number-input',
                    'min' => 0,
                ],
            ])
            ->add('cumulable', CheckboxType::class, [
                'label' => 'Cumulable avec d\'autres promotions',
                'required' => false,
            ])
            ->add('actif', CheckboxType::class, [
                'label' => 'Promotion active',
                'required' => false,
            ])
            ->add('produits', EntityType::class, [
                'label' => 'Produits ciblés',
                'class' => Produit::class,
                'choice_label' => 'nom',
                'multiple' => true,
                'expanded' => false,
                'by_reference' => false,
                'attr' => [
                    'class' => 'select',
                    'size' => 8,
                ],
                'query_builder' => static fn ($repo) => $repo->createQueryBuilder('p')->orderBy('p.nom', 'ASC'),
            ]);
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => Promotion::class,
        ]);
    }
}
