<?php

namespace App\Form;

use App\Entity\Diagnostique;
use App\Entity\Probleme;
use Symfony\Bridge\Doctrine\Form\Type\EntityType;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\Extension\Core\Type\CheckboxType;
use Symfony\Component\Form\Extension\Core\Type\ChoiceType;
use Symfony\Component\Form\Extension\Core\Type\DateTimeType;
use Symfony\Component\Form\Extension\Core\Type\IntegerType;
use Symfony\Component\Form\Extension\Core\Type\TextareaType;
use Symfony\Component\Form\Extension\Core\Type\TextType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;

class DiagnostiqueType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        if (null === $options['probleme_locked_probleme']) {
            $builder->add('probleme', EntityType::class, [
                'class' => Probleme::class,
                'choice_label' => fn (Probleme $p) => '#'.$p->getId().' — '.$p->getType(),
                'label' => 'Problème',
                'attr' => ['class' => 'form-control'],
            ]);
        }

        $builder
            ->add('cause', TextareaType::class, ['label' => 'Cause', 'attr' => ['rows' => 3, 'class' => 'form-control']])
            ->add('solutionProposee', TextareaType::class, ['label' => 'Solution proposée', 'attr' => ['rows' => 4, 'class' => 'form-control']])
            ->add('dateDiagnostique', DateTimeType::class, [
                'label' => 'Date diagnostic',
                'widget' => 'single_text',
                'attr' => ['class' => 'form-control'],
            ])
            ->add('resultat', TextType::class, ['label' => 'Résultat', 'attr' => ['class' => 'form-control']])
            ->add('medicament', TextareaType::class, ['label' => 'Médicament / traitement', 'required' => false, 'attr' => ['rows' => 2, 'class' => 'form-control']])
            ->add('approuve', CheckboxType::class, ['label' => 'Approuvé (visible fermier)', 'required' => false])
            ->add('numRevision', IntegerType::class, ['label' => 'N° révision', 'attr' => ['class' => 'form-control']]);

        if ($options['include_feedback']) {
            $builder
                ->add('feedbackFermier', ChoiceType::class, [
                    'label' => 'Feedback fermier',
                    'required' => false,
                    'choices' => ['Résolu' => 'RESOLU', 'Non résolu' => 'NON_RESOLU'],
                    'placeholder' => '—',
                    'attr' => ['class' => 'form-control'],
                ])
                ->add('feedbackCommentaire', TextareaType::class, ['label' => 'Commentaire feedback', 'required' => false, 'attr' => ['rows' => 2, 'class' => 'form-control']]);
        }
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => Diagnostique::class,
            'include_feedback' => false,
            'probleme_locked_probleme' => null,
        ]);
        $resolver->setAllowedTypes('probleme_locked_probleme', ['null', Probleme::class]);
    }
}
