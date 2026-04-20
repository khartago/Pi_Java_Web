<?php

namespace App\Form;

use App\Entity\Affectation;
use App\Entity\Employe;
use App\Entity\Materiel;
use Doctrine\ORM\EntityRepository;
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
                'query_builder' => static fn (EntityRepository $er) => $er->createQueryBuilder('m')
                    ->andWhere('m.etat != :panne')->setParameter('panne', 'panne')->orderBy('m.nom', 'ASC'),
            ])
            ->add('employe', EntityType::class, [
                'class' => Employe::class,
                'choice_label' => 'fullName',
                'query_builder' => static fn (EntityRepository $er) => $er->createQueryBuilder('e')
                    ->orderBy('e.nom', 'ASC')->addOrderBy('e.prenom', 'ASC'),
            ])
            ->add('dateAffectation', DateType::class, [
                'widget' => 'single_text',
                'input' => 'datetime_immutable',
                'data' => new \DateTimeImmutable(),
            ])
            ->add('note', TextareaType::class, ['required' => false]);
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults(['data_class' => Affectation::class]);
    }
}
