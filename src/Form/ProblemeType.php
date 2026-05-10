<?php

namespace App\Form;

use App\Entity\Probleme;
use App\Entity\Utilisateur;
use Symfony\Bridge\Doctrine\Form\Type\EntityType;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\Extension\Core\Type\ChoiceType;
use Symfony\Component\Form\Extension\Core\Type\DateTimeType;
use Symfony\Component\Form\Extension\Core\Type\FileType;
use Symfony\Component\Form\Extension\Core\Type\TextareaType;
use Symfony\Component\Form\Extension\Core\Type\TextType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;
use Symfony\Component\Validator\Constraints\All;
use Symfony\Component\Validator\Constraints as Assert;
use Symfony\Component\Validator\Constraints\Image;
use Symfony\Component\Validator\Context\ExecutionContextInterface;

class ProblemeType extends AbstractType
{
    /** Longueur minimale pour les champs descriptifs (hors simple libellé court). */
    private const MIN_DESCRIPTION_LEN = 20;

    public const GRAVITES = [
        'Faible' => 'Faible',
        'Moyenne' => 'Moyenne',
        'Élevée' => 'Élevée',
        'Critique' => 'Critique',
    ];

    public const ETATS = [
        'En attente' => 'EN_ATTENTE',
        'Diagnostic disponible' => 'DIAGNOSTIQUE_DISPONIBLE',
        'Réouvert' => 'REOUVERT',
        'Clôturé' => 'CLOTURE',
    ];

    /** @return list<string> */
    public static function graviteValues(): array
    {
        return array_values(self::GRAVITES);
    }

    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder
            ->add('type', TextType::class, [
                'label' => 'Type de problème',
                'attr' => ['class' => 'form-control'],
                'constraints' => [
                    new Assert\NotBlank(message: 'Le type est obligatoire.'),
                    new Assert\Length(
                        min: 3,
                        max: 100,
                        minMessage: 'Le type doit contenir au moins {{ limit }} caractères.',
                        maxMessage: 'Le type ne peut pas dépasser {{ limit }} caractères.',
                    ),
                ],
            ])
            ->add('description', TextareaType::class, [
                'label' => 'Description',
                'attr' => ['rows' => 5, 'class' => 'form-control'],
                'constraints' => [
                    new Assert\NotBlank(message: 'La description est obligatoire.'),
                    new Assert\Length(
                        min: self::MIN_DESCRIPTION_LEN,
                        minMessage: 'La description doit contenir au moins {{ limit }} caractères (détaillez le constat sur le terrain).',
                    ),
                ],
            ])
            ->add('gravite', ChoiceType::class, [
                'label' => 'Gravité',
                'choices' => self::GRAVITES,
                'attr' => ['class' => 'form-control'],
                'constraints' => [
                    new Assert\NotBlank(message: 'La gravité est obligatoire.'),
                    new Assert\Choice(
                        choices: self::graviteValues(),
                        message: 'Choix de gravité invalide.',
                    ),
                ],
            ])
            ->add('dateDetection', DateTimeType::class, [
                'label' => 'Date de détection',
                'widget' => 'single_text',
                'input' => 'datetime_immutable',
                'attr' => ['class' => 'form-control'],
                'constraints' => [
                    new Assert\NotNull(message: 'La date de détection est obligatoire.'),
                    new Assert\Callback(static function (mixed $value, ExecutionContextInterface $context): void {
                        if (!$value instanceof \DateTimeInterface) {
                            return;
                        }
                        if ($value > new \DateTimeImmutable()) {
                            $context->buildViolation('La date de détection ne peut pas être postérieure à la date du jour.')
                                ->addViolation();
                        }
                    }),
                ],
            ]);

        if ($options['include_etat']) {
            $builder->add('etat', ChoiceType::class, [
                'label' => 'État',
                'choices' => self::ETATS,
                'attr' => ['class' => 'form-control'],
                'constraints' => [
                    new Assert\NotBlank(message: 'L’état est obligatoire.'),
                    new Assert\Choice(
                        choices: array_values(self::ETATS),
                        message: 'Choix d’état invalide.',
                    ),
                ],
            ]);
        }

        if ($options['include_plantation_produit']) {
            $plantationChoices = $options['plantation_choices'];
            $produitChoices = $options['produit_choices'];
            $pCh = ['—' => null];
            foreach ($plantationChoices as $id => $label) {
                $pCh[$label.' ('.$id.')'] = (int) $id;
            }
            $prCh = ['—' => null];
            foreach ($produitChoices as $id => $label) {
                $prCh[$label.' ('.$id.')'] = (int) $id;
            }
            $builder
                ->add('idPlantation', ChoiceType::class, [
                    'label' => 'Plantation (optionnel)',
                    'choices' => $pCh,
                    'required' => false,
                    'placeholder' => false,
                    'attr' => ['class' => 'form-control'],
                ])
                ->add('idProduit', ChoiceType::class, [
                    'label' => 'Produit (optionnel)',
                    'choices' => $prCh,
                    'required' => false,
                    'placeholder' => false,
                    'attr' => ['class' => 'form-control'],
                ]);
        }

        if ($options['include_admin_assignee']) {
            $builder->add('adminAssignee', EntityType::class, [
                'class' => Utilisateur::class,
                'label' => 'Administrateur assigné',
                'required' => false,
                'choice_label' => fn (Utilisateur $u) => $u->getNom().' — '.$u->getEmail(),
                'query_builder' => fn ($repo) => $repo->createQueryBuilder('u')
                    ->andWhere('u.role = :r')
                    ->setParameter('r', Utilisateur::ROLE_ADMIN_DB)
                    ->orderBy('u.nom', 'ASC'),
                'attr' => ['class' => 'form-control'],
            ]);
        }

        $builder
            ->add('photoFiles', FileType::class, [
                'label' => 'Photos',
                'mapped' => false,
                'required' => false,
                'multiple' => true,
                'help' => 'Sélectionnez une ou plusieurs images depuis votre appareil (JPG, PNG, WebP ou GIF, max. 5 Mo chacune).',
                'attr' => [
                    'class' => 'form-control',
                    'accept' => 'image/jpeg,image/png,image/webp,image/gif',
                ],
                'constraints' => [
                    new All([
                        'constraints' => [
                            new Image(
                                maxSize: '5M',
                                mimeTypes: ['image/jpeg', 'image/png', 'image/webp', 'image/gif'],
                                mimeTypesMessage: 'Format d’image non accepté.',
                            ),
                        ],
                    ]),
                ],
            ]);
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => Probleme::class,
            'include_etat' => false,
            'include_plantation_produit' => false,
            'include_admin_assignee' => false,
            'plantation_choices' => [],
            'produit_choices' => [],
        ]);
        $resolver->setAllowedTypes('include_etat', 'bool');
        $resolver->setAllowedTypes('include_plantation_produit', 'bool');
        $resolver->setAllowedTypes('include_admin_assignee', 'bool');
        $resolver->setAllowedTypes('plantation_choices', 'array');
        $resolver->setAllowedTypes('produit_choices', 'array');
    }
}
