<?php

namespace App\Form;

use App\Entity\Comment;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\Extension\Core\Type\EmailType;
use Symfony\Component\Form\Extension\Core\Type\TextType;
use Symfony\Component\Form\Extension\Core\Type\TextareaType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;

class CommentType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder
            ->add('authorName', TextType::class, [
                'label' => 'Your Name',
                'attr' => ['placeholder' => 'John Doe']
            ])
            ->add('authorEmail', EmailType::class, [
                'label' => 'Your Email (optional)',
                'required' => false,
                'attr' => ['placeholder' => 'john@example.com']
            ])
            ->add('content', TextareaType::class, [
                'label' => 'Comment',
                'attr' => ['rows' => 4, 'placeholder' => 'Write your comment here...']
            ]);
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => Comment::class,
        ]);
    }
}