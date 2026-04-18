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
            ->add('produit', EntityType::class, [
                'class'        => Produit::class,
                'choice_label' => 'nom',
                'label'        => 'Produit cible',
                'placeholder'  => '— Sélectionner un produit —',
                'query_builder' => static fn ($repo) => $repo->createQueryBuilder('p')->orderBy('p.nom', 'ASC'),
            ])
            ->add('materiel', EntityType::class, [
                'class'        => Materiel::class,
                'choice_label' => static fn (Materiel $m): string => $m->getNom().' ('.$m->getEtat().')',
                'label'        => 'Matériel recommandé',
                'placeholder'  => '— Sélectionner un matériel —',
                'query_builder' => static fn ($repo) => $repo->createQueryBuilder('m')
                    ->andWhere('m.etat != :panne')
                    ->setParameter('panne', 'panne')
                    ->orderBy('m.nom', 'ASC'),
            ])
            ->add('priorite', ChoiceType::class, [
                'label'   => 'Niveau de priorité',
                'choices' => [
                    '5 — Essentiel (obligatoire)'   => 5,
                    '4 — Important'                 => 4,
                    '3 — Recommandé (par défaut)'   => 3,
                    '2 — Utile'                     => 2,
                    '1 — Optionnel'                 => 1,
                ],
            ])
            ->add('raison', TextareaType::class, [
                'label'    => 'Raison / justification (optionnel)',
                'required' => false,
                'attr'     => [
                    'rows'        => 3,
                    'placeholder' => 'Ex : Irrigation indispensable pour la culture de tomate en été.',
                ],
            ])
            ->add('actif', CheckboxType::class, [
                'label'    => 'Activer cette recommandation',
                'required' => false,
            ])
        ;
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => Recommandation::class,
        ]);
    }
}
