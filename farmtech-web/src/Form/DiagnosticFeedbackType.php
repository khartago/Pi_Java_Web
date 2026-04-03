<?php

namespace App\Form;

use App\Entity\Diagnostique;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\Extension\Core\Type\ChoiceType;
use Symfony\Component\Form\Extension\Core\Type\TextareaType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;

class DiagnosticFeedbackType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder
            ->add('feedbackFermier', ChoiceType::class, [
                'label' => 'Le diagnostic vous a-t-il aidé ?',
                'required' => true,
                'choices' => ['Résolu' => 'RESOLU', 'Non résolu' => 'NON_RESOLU'],
                'placeholder' => 'Choisir…',
                'attr' => ['class' => 'form-control'],
            ])
            ->add('feedbackCommentaire', TextareaType::class, [
                'label' => 'Commentaire (optionnel)',
                'required' => false,
                'attr' => ['rows' => 3, 'class' => 'form-control'],
            ]);
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => Diagnostique::class,
        ]);
    }
}
