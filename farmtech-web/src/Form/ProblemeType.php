<?php

namespace App\Form;

use App\Entity\Probleme;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\Extension\Core\Type\ChoiceType;
use Symfony\Component\Form\Extension\Core\Type\DateTimeType;
use Symfony\Component\Form\Extension\Core\Type\FileType;
use Symfony\Component\Form\Extension\Core\Type\TextareaType;
use Symfony\Component\Form\Extension\Core\Type\TextType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;
use Symfony\Component\Validator\Constraints\All;
use Symfony\Component\Validator\Constraints\Image;

class ProblemeType extends AbstractType
{
    public const GRAVITES = ['Faible' => 'Faible', 'Moyenne' => 'Moyenne', 'Critique' => 'Critique'];

    public const ETATS = [
        'En attente' => 'EN_ATTENTE',
        'Diagnostic disponible' => 'DIAGNOSTIQUE_DISPONIBLE',
        'Réouvert' => 'REOUVERT',
        'Clôturé' => 'CLOTURE',
    ];

    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder
            ->add('type', TextType::class, ['label' => 'Type de problème', 'attr' => ['class' => 'form-control']])
            ->add('description', TextareaType::class, ['label' => 'Description', 'attr' => ['rows' => 5, 'class' => 'form-control']])
            ->add('gravite', ChoiceType::class, [
                'label' => 'Gravité',
                'choices' => self::GRAVITES,
                'attr' => ['class' => 'form-control'],
            ])
            ->add('dateDetection', DateTimeType::class, [
                'label' => 'Date de détection',
                'widget' => 'single_text',
                'attr' => ['class' => 'form-control'],
            ]);

        if ($options['include_etat']) {
            $builder->add('etat', ChoiceType::class, [
                'label' => 'État',
                'choices' => self::ETATS,
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
        ]);
        $resolver->setAllowedTypes('include_etat', 'bool');
    }
}
