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
use Symfony\Component\Validator\Constraints as Assert;
use Symfony\Component\Validator\Context\ExecutionContextInterface;

class DiagnostiqueType extends AbstractType
{
    private const MIN_TEXTE_LEN = 20;

    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        if (null === $options['probleme_locked_probleme']) {
            $builder->add('probleme', EntityType::class, [
                'class' => Probleme::class,
                'choice_label' => fn (Probleme $p) => '#'.$p->getId().' — '.$p->getType(),
                'label' => 'Problème',
                'attr' => ['class' => 'form-control'],
                'constraints' => [
                    new Assert\NotNull(message: 'Veuillez sélectionner un problème.'),
                ],
            ]);
        }

        $builder
            ->add('cause', TextareaType::class, [
                'label' => 'Cause',
                'attr' => ['rows' => 3, 'class' => 'form-control'],
                'constraints' => [
                    new Assert\NotBlank(message: 'La cause est obligatoire.'),
                    new Assert\Length(
                        min: self::MIN_TEXTE_LEN,
                        minMessage: 'La cause doit contenir au moins {{ limit }} caractères.',
                    ),
                ],
            ])
            ->add('solutionProposee', TextareaType::class, [
                'label' => 'Solution proposée',
                'attr' => ['rows' => 4, 'class' => 'form-control'],
                'constraints' => [
                    new Assert\NotBlank(message: 'La solution proposée est obligatoire.'),
                    new Assert\Length(
                        min: self::MIN_TEXTE_LEN,
                        minMessage: 'La solution proposée doit contenir au moins {{ limit }} caractères.',
                    ),
                ],
            ])
            ->add('dateDiagnostique', DateTimeType::class, [
                'label' => 'Date diagnostic',
                'widget' => 'single_text',
                'input' => 'datetime_immutable',
                'attr' => ['class' => 'form-control'],
                'constraints' => [
                    new Assert\NotNull(message: 'La date du diagnostic est obligatoire.'),
                    new Assert\Callback(static function (mixed $value, ExecutionContextInterface $context): void {
                        if (!$value instanceof \DateTimeInterface) {
                            return;
                        }
                        if ($value > new \DateTimeImmutable()) {
                            $context->buildViolation('La date du diagnostic ne peut pas être postérieure à la date du jour.')
                                ->addViolation();
                        }
                    }),
                ],
            ])
            ->add('resultat', TextType::class, [
                'label' => 'Résultat',
                'attr' => ['class' => 'form-control'],
                'constraints' => [
                    new Assert\NotBlank(message: 'Le résultat est obligatoire.'),
                    new Assert\Length(
                        min: 5,
                        max: 100,
                        minMessage: 'Le résultat doit contenir au moins {{ limit }} caractères.',
                        maxMessage: 'Le résultat ne peut pas dépasser {{ limit }} caractères.',
                    ),
                ],
            ])
            ->add('medicament', TextareaType::class, [
                'label' => 'Médicament / traitement',
                'required' => false,
                'attr' => ['rows' => 2, 'class' => 'form-control'],
                'constraints' => [
                    new Assert\Length(max: 2000, maxMessage: 'Ce texte ne peut pas dépasser {{ limit }} caractères.'),
                ],
            ])
            ->add('approuve', CheckboxType::class, ['label' => 'Approuvé (visible fermier)', 'required' => false]);

        if (!$options['hide_num_revision']) {
            $builder->add('numRevision', IntegerType::class, [
                'label' => 'N° révision',
                'attr' => ['class' => 'form-control'],
                'constraints' => [
                    new Assert\NotNull(message: 'Le numéro de révision est obligatoire.'),
                    new Assert\Positive(message: 'Le numéro de révision doit être un entier positif.'),
                ],
            ]);
        }

        if ($options['include_feedback']) {
            $builder
                ->add('feedbackFermier', ChoiceType::class, [
                    'label' => 'Feedback fermier',
                    'required' => false,
                    'choices' => ['Résolu' => 'RESOLU', 'Non résolu' => 'NON_RESOLU'],
                    'placeholder' => '—',
                    'attr' => ['class' => 'form-control'],
                    'constraints' => [
                        new Assert\Callback(static function (?string $value, ExecutionContextInterface $context): void {
                            if (null === $value || '' === $value) {
                                return;
                            }
                            if (!\in_array($value, ['RESOLU', 'NON_RESOLU'], true)) {
                                $context->buildViolation('Valeur de feedback invalide.')->addViolation();
                            }
                        }),
                    ],
                ])
                ->add('feedbackCommentaire', TextareaType::class, [
                    'label' => 'Commentaire feedback',
                    'required' => false,
                    'attr' => ['rows' => 2, 'class' => 'form-control'],
                    'constraints' => [
                        new Assert\Length(max: 2000, maxMessage: 'Le commentaire ne peut pas dépasser {{ limit }} caractères.'),
                    ],
                ]);
        }
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => Diagnostique::class,
            'include_feedback' => false,
            'probleme_locked_probleme' => null,
            'hide_num_revision' => false,
        ]);
        $resolver->setAllowedTypes('probleme_locked_probleme', ['null', Probleme::class]);
        $resolver->setAllowedTypes('hide_num_revision', 'bool');
    }
}
