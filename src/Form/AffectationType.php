<?php

namespace App\Form;

use App\Entity\Affectation;
use App\Entity\Employe;
use App\Entity\Materiel;
use Doctrine\ORM\EntityRepository;
use Doctrine\ORM\QueryBuilder;
use Symfony\Bridge\Doctrine\Form\Type\EntityType;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\Extension\Core\Type\DateType;
use Symfony\Component\Form\Extension\Core\Type\TextareaType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;

class AffectationType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder
            ->add('materiel', EntityType::class, [
                'class' => Materiel::class,
                'choice_label' => 'nom',
                'label' => 'Matériel',
                'placeholder' => 'Choisir un matériel',
                'query_builder' => static function (EntityRepository $er): QueryBuilder {
                    return $er->createQueryBuilder('m')
                        ->andWhere('m.etat != :panne')
                        ->setParameter('panne', 'panne')
                        ->orderBy('m.nom', 'ASC');
                },
                'attr' => [
                    'class' => 'select',
                ],
            ])
            ->add('employe', EntityType::class, [
                'class' => Employe::class,
                'choice_label' => 'fullName',
                'label' => 'Employé',
                'placeholder' => 'Choisir un employé',
                'query_builder' => static function (EntityRepository $er): QueryBuilder {
                    return $er->createQueryBuilder('e')
                        ->orderBy('e.nom', 'ASC')
                        ->addOrderBy('e.prenom', 'ASC');
                },
                'attr' => [
                    'class' => 'select',
                ],
            ])
            ->add('dateAffectation', DateType::class, [
                'label' => 'Date d\'affectation',
                'widget' => 'single_text',
                'input' => 'datetime_immutable',
                'data' => new \DateTimeImmutable(),
                'attr' => [
                    'class' => 'date-input',
                ],
            ])
            ->add('note', TextareaType::class, [
                'label' => 'Note',
                'required' => false,
                'attr' => [
                    'placeholder' => 'Observations, conditions particulières…',
                    'class' => 'textarea',
                    'rows' => 4,
                ],
            ]);
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => Affectation::class,
        ]);
    }
}
