<?php

namespace App\Form;

use App\Entity\Produit;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\Extension\Core\Type\ChoiceType;
use Symfony\Component\Form\Extension\Core\Type\DateType;
use Symfony\Component\Form\Extension\Core\Type\FileType;
use Symfony\Component\Form\Extension\Core\Type\IntegerType;
use Symfony\Component\Form\Extension\Core\Type\NumberType;
use Symfony\Component\Form\Extension\Core\Type\TextType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;
use Symfony\Component\Validator\Constraints\File;

class ProduitType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder
            ->add('nom', TextType::class, [
                'label' => 'Nom du produit',
                'attr' => [
                    'placeholder' => 'Ex: Engrais azoté',
                    'class' => 'input',
                ],
            ])
            ->add('quantite', IntegerType::class, [
                'label' => 'Quantité en stock',
                'attr' => [
                    'placeholder' => '0',
                    'class' => 'number-input',
                    'min' => 0,
                ],
            ])
            ->add('unite', ChoiceType::class, [
                'label' => 'Unité',
                'choices' => [
                    'Kilogramme (kg)' => 'kg',
                    'Litre (l)' => 'l',
                    'Pièce' => 'piece',
                ],
                'placeholder' => 'Choisir une unité',
                'attr' => [
                    'class' => 'select',
                ],
            ])
            ->add('dateExpiration', DateType::class, [
                'label' => 'Date d’expiration',
                'required' => false,
                'widget' => 'single_text',
                'input' => 'datetime_immutable',
                'attr' => [
                    'class' => 'date-input',
                ],
            ])
            ->add('prix', NumberType::class, [
                'label' => 'Prix unitaire (€)',
                'required' => false,
                'scale' => 2,
                'attr' => [
                    'placeholder' => '0.00',
                    'class' => 'number-input',
                    'min' => 0,
                    'step' => '0.01',
                ],
            ])
            ->add('imageFile', FileType::class, [
                'label' => 'Image produit',
                'mapped' => false,
                'required' => false,
                'help' => 'Formats acceptés : JPG, PNG ou JPEG. Taille maximale : 5 Mo.',
                'attr' => [
                    'accept' => '.jpg,.jpeg,.png',
                    'class' => 'input',
                ],
                'constraints' => [
                    new File(
                        maxSize: '5M',
                        mimeTypes: ['image/jpeg', 'image/png'],
                        maxSizeMessage: 'L’image doit peser moins de 5 Mo.',
                        mimeTypesMessage: 'L’image doit être au format JPG, JPEG ou PNG.'
                    ),
                ],
            ]);
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => Produit::class,
        ]);
    }
}
