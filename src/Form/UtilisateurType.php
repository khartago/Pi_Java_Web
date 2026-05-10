<?php

namespace App\Form;

use App\Entity\Utilisateur;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\Extension\Core\Type\ChoiceType;
use Symfony\Component\Form\Extension\Core\Type\EmailType;
use Symfony\Component\Form\Extension\Core\Type\PasswordType;
use Symfony\Component\Form\Extension\Core\Type\TextType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;
use Symfony\Component\Validator\Constraints\Callback;
use Symfony\Component\Validator\Constraints\Length;
use Symfony\Component\Validator\Constraints\NotBlank;
use Symfony\Component\Validator\Context\ExecutionContextInterface;

class UtilisateurType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $pwdConstraints = $options['require_password']
            ? [new NotBlank(message: 'Mot de passe obligatoire.'), new Length(min: 3, max: 255)]
            : [
                new Length(max: 255),
                new Callback([
                    'callback' => static function (?string $value, ExecutionContextInterface $context): void {
                        if ($value !== null && $value !== '' && strlen($value) < 3) {
                            $context->buildViolation('Au moins 3 caractères si vous modifiez le mot de passe.')
                                ->addViolation();
                        }
                    },
                ]),
            ];
        $pwdOpts = [
            'label' => 'Mot de passe',
            'required' => $options['require_password'],
            'mapped' => false,
            'attr' => ['class' => 'form-control', 'autocomplete' => 'new-password'],
            'help' => $options['require_password'] ? null : 'Laisser vide pour ne pas changer.',
            'constraints' => $pwdConstraints,
        ];
        $builder
            ->add('nom', TextType::class, ['label' => 'Nom', 'attr' => ['class' => 'form-control']])
            ->add('email', EmailType::class, ['label' => 'Email', 'attr' => ['class' => 'form-control']])
            ->add('motDePasse', PasswordType::class, $pwdOpts)
            ->add('role', ChoiceType::class, [
                'label' => 'Rôle',
                'choices' => [
                    'Administrateur' => Utilisateur::ROLE_ADMIN_DB,
                    'Agriculteur' => Utilisateur::ROLE_FARMER_DB,
                ],
                'attr' => ['class' => 'form-control'],
            ]);
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => Utilisateur::class,
            'require_password' => true,
        ]);
    }
}
