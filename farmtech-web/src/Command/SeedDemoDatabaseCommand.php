<?php

declare(strict_types=1);

namespace App\Command;

use App\Entity\Article;
use App\Entity\Affectation;
use App\Entity\Blog;
use App\Entity\Commentaire;
use App\Entity\Diagnostique;
use App\Entity\Employe;
use App\Entity\Materiel;
use App\Entity\Plantation;
use App\Entity\Probleme;
use App\Entity\Produit;
use App\Entity\ProduitHistorique;
use App\Entity\Production;
use App\Entity\Promotion;
use App\Entity\Recommandation;
use App\Entity\Utilisateur;
use Doctrine\DBAL\Connection;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Component\Console\Attribute\AsCommand;
use Symfony\Component\Console\Command\Command;
use Symfony\Component\Console\Input\InputInterface;
use Symfony\Component\Console\Input\InputOption;
use Symfony\Component\Console\Output\OutputInterface;
use Symfony\Component\Console\Style\SymfonyStyle;
use Symfony\Component\HttpKernel\KernelInterface;
use Symfony\Component\PasswordHasher\Hasher\UserPasswordHasherInterface;

#[AsCommand(
    name: 'app:db:seed-demo',
    description: 'Supprime les données métier et charge un jeu de démonstration (utilisateurs, stock, blog, support, promotions…).',
)]
final class SeedDemoDatabaseCommand extends Command
{
    private const DEMO_PASSWORD = 'demo123';

    /** @var list<string> */
    private const PURGE_TABLES = [
        'commentaire',
        'article',
        'blog',
        'diagnostique',
        'probleme',
        'produit_historique',
        'favoris',
        'promotion_produit',
        'promotion',
        'affectation',
        'recommandation',
        'materiel',
        'production',
        'plantation',
        'produit',
        'employe',
        'utilisateur',
        'messenger_messages',
    ];

    public function __construct(
        private readonly EntityManagerInterface $em,
        private readonly UserPasswordHasherInterface $passwordHasher,
        private readonly KernelInterface $kernel,
    ) {
        parent::__construct();
    }

    protected function configure(): void
    {
        $this->addOption('force', null, InputOption::VALUE_NONE, 'Confirme l’effacement des données existantes (obligatoire hors dev).');
    }

    protected function execute(InputInterface $input, OutputInterface $output): int
    {
        $io = new SymfonyStyle($input, $output);

        if ('prod' === $this->kernel->getEnvironment()) {
            $io->error('Cette commande est interdite en environnement production.');

            return Command::FAILURE;
        }

        $force = (bool) $input->getOption('force');
        if ('dev' !== $this->kernel->getEnvironment() && !$force) {
            $io->error('En dehors de l’environnement « dev », utilisez l’option --force.');

            return Command::FAILURE;
        }

        if (!$force && !$io->confirm('Effacer les données métier actuelles et charger la démo ?', false)) {
            $io->note('Annulé.');

            return Command::SUCCESS;
        }

        $conn = $this->em->getConnection();
        $this->purge($conn, $io);
        $this->em->clear();

        $this->seed($io);

        $io->success('Base de démonstration chargée. Comptes : admin@farmtech.tn / fermiers @email.tn — mot de passe : '.self::DEMO_PASSWORD);

        return Command::SUCCESS;
    }

    private function purge(Connection $conn, SymfonyStyle $io): void
    {
        $io->section('Purge des tables métier');
        $conn->executeStatement('SET FOREIGN_KEY_CHECKS=0');
        foreach (self::PURGE_TABLES as $table) {
            if (!$this->tableExists($conn, $table)) {
                continue;
            }
            $conn->executeStatement('DELETE FROM `'.$table.'`');
            $io->text('Vidé : '.$table);
        }
        $conn->executeStatement('SET FOREIGN_KEY_CHECKS=1');
    }

    private function tableExists(Connection $conn, string $table): bool
    {
        $db = $conn->fetchOne('SELECT DATABASE()');

        return (int) $conn->fetchOne(
            'SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = ? AND table_name = ?',
            [$db, $table]
        ) > 0;
    }

    private function seed(SymfonyStyle $io): void
    {
        $io->section('Insertion des données');
        $conn = $this->em->getConnection();

        $admin = $this->createUser('Admin FARMTECH', 'admin@farmtech.tn', Utilisateur::ROLE_ADMIN_DB);
        $admin2 = $this->createUser('Dr. Karim Ben Salem', 'karim.bensalem@farmtech.tn', Utilisateur::ROLE_ADMIN_DB);
        $farmer1 = $this->createUser('Mohamed Amine', 'mohamed.amine@email.tn', Utilisateur::ROLE_FARMER_DB);
        $farmer2 = $this->createUser('Fatma Trabelsi', 'fatma.trabelsi@email.tn', Utilisateur::ROLE_FARMER_DB);
        $farmer3 = $this->createUser('Ali Mansour', 'ali.mansour@email.tn', Utilisateur::ROLE_FARMER_DB);
        foreach ([$admin, $admin2, $farmer1, $farmer2, $farmer3] as $u) {
            $this->em->persist($u);
        }
        $this->em->flush();

        $produits = $this->seedProduits();
        $this->em->flush();

        $plantations = $this->seedPlantations();
        $this->em->flush();

        $this->seedProduction();
        $this->em->flush();

        $materiels = $this->seedMateriels($produits);
        $this->em->flush();

        $employes = $this->seedEmployes();
        $this->em->flush();

        $this->seedAffectations($materiels, $employes);
        $this->em->flush();

        $this->seedRecommandations($produits, $materiels);
        $this->em->flush();

        $this->seedPromotions($produits);
        $this->em->flush();

        $blog = $this->seedBlog($admin, $farmer1);
        $this->em->flush();

        $this->seedProblemesAndDiagnostics(
            [$farmer1, $farmer2, $farmer3],
            [$admin, $admin2],
            $plantations,
            $produits
        );
        $this->em->flush();

        $this->seedProduitHistorique($produits);
        $this->em->flush();

        $this->seedFavoris($conn, $produits);
        $this->em->flush();

        $io->listing([
            count($produits).' produits',
            count($plantations).' plantations',
            'Blog « '.$blog->getTitleBlog().' » + articles + commentaires',
            'Problèmes / diagnostics, promotions, matériels, affectations',
        ]);
    }

    private function createUser(string $nom, string $email, string $role): Utilisateur
    {
        $u = new Utilisateur();
        $u->setNom($nom);
        $u->setEmail($email);
        $u->setRole($role);
        $u->setMotDePasse($this->passwordHasher->hashPassword($u, self::DEMO_PASSWORD));

        return $u;
    }

    /**
     * @return list<Produit>
     */
    private function seedProduits(): array
    {
        $rows = [
            ['Tomates cerises', 150, 'kg', 14, 'uploads/products/tomates_cerises.png', 4.5],
            ['Pommes de terre Bintje', 500, 'kg', 45, 'uploads/products/pommes_de_terre.png', 1.2],
            ['Poivrons doux', 80, 'kg', 10, 'uploads/products/poivrons.png', 3.8],
            ['Engrais NPK', 200, 'sac', 365, 'uploads/products/engrais.png', 42.0],
            ['Fongicide cuivrique', 50, 'L', 180, 'uploads/products/fongicide.png', 28.5],
            ['Semences blé', 1000, 'kg', 730, 'uploads/products/semences_ble.png', 1.8],
            ['Plants de tomate', 200, 'unité', 30, 'uploads/products/plants_tomate.png', 0.9],
            ['Semences de maïs', 500, 'kg', 730, 'uploads/products/semences_mais.png', 2.2],
        ];
        $list = [];
        foreach ($rows as [$nom, $qte, $unite, $expDays, $img, $prix]) {
            $p = new Produit();
            $p->setNom($nom);
            $p->setQuantite($qte);
            $p->setUnite($unite);
            $p->setDateExpiration(new \DateTimeImmutable('+'.$expDays.' days'));
            $p->setImagePath($img);
            $p->setPrix($prix);
            $this->em->persist($p);
            $list[] = $p;
        }

        return $list;
    }

    /**
     * @return list<Plantation>
     */
    private function seedPlantations(): array
    {
        $slots = [
            ['Tomate', 'Cerise', 5, 'Printemps'],
            ['Pomme de terre', 'Bintje', 10, 'Printemps'],
            ['Poivron', 'Doux', 3, 'Été'],
            ['Tomate', 'Ronde', 4, 'Printemps'],
            ['Pomme de terre', 'Charlotte', 8, 'Printemps'],
            ['Poivron', 'Piment', 2, 'Été'],
            ['Tomate', 'Cœur de bœuf', 6, 'Été'],
            ['Pomme de terre', 'Agata', 12, 'Printemps'],
        ];
        $list = [];
        $i = 0;
        foreach ($slots as [$nom, $var, $qty, $saison]) {
            $pl = new Plantation();
            $pl->setNomPlant($nom);
            $pl->setVariete($var);
            $pl->setQuantite($qty);
            $pl->setDatePlante(new \DateTime());
            $pl->setSaison($saison);
            $pl->setEtat('EN_ATTENTE');
            $pl->setStage(1);
            $pl->setWaterCount(0);
            $pl->setLastWaterTime(0);
            $pl->setStatus('ALIVE');
            $pl->setGrowthSpeed(1.0);
            $pl->setSlotIndex($i++);
            $this->em->persist($pl);
            $list[] = $pl;
        }

        return $list;
    }

    private function seedProduction(): void
    {
        foreach (
            [
                [120.5, 'Premium', 'Vendu', '-3 days'],
                [85.0, 'Standard', 'En stock', '-1 day'],
                [45.0, 'Premium', 'En attente', 'today'],
            ] as [$qty, $qual, $etat, $when]
        ) {
            $pr = new Production();
            $pr->setQuantiteProduite($qty);
            $pr->setDateRecolte(new \DateTime($when));
            $pr->setQualite($qual);
            $pr->setEtat($etat);
            $this->em->persist($pr);
        }
    }

    /**
     * @param list<Produit> $produits
     *
     * @return list<Materiel>
     */
    private function seedMateriels(array $produits): array
    {
        $defs = [
            ['Tracteur John Deere', 'Bon', '2023-03-15', 45000.0, 0],
            ['Pulvérisateur 500L', 'Neuf', '2024-01-10', 3500.0, 4],
            ['Semoir pneumatique', 'Bon', '2022-09-20', 12000.0, 5],
        ];
        $out = [];
        foreach ($defs as [$nom, $etat, $date, $cout, $idx]) {
            $m = new Materiel();
            $m->setNom($nom);
            $m->setEtat($etat);
            $m->setDateAchat(new \DateTimeImmutable($date));
            $m->setCout($cout);
            $m->setProduit($produits[$idx]);
            $this->em->persist($m);
            $out[] = $m;
        }

        return $out;
    }

    /**
     * @return list<Employe>
     */
    private function seedEmployes(): array
    {
        $e1 = new Employe();
        $e1->setNom('Ben Ali');
        $e1->setPrenom('Sami');
        $e1->setPoste('Technicien agricole');
        $e1->setEmail('sami.benali@farmtech.tn');
        $e2 = new Employe();
        $e2->setNom('Jebali');
        $e2->setPrenom('Emna');
        $e2->setPoste('Responsable stock');
        $e2->setEmail('emna.jebali@farmtech.tn');
        $this->em->persist($e1);
        $this->em->persist($e2);

        return [$e1, $e2];
    }

    /**
     * @param list<Materiel>  $materiels
     * @param list<Employe>   $employes
     */
    private function seedAffectations(array $materiels, array $employes): void
    {
        $a = new Affectation();
        $a->setMateriel($materiels[1]);
        $a->setEmploye($employes[0]);
        $a->setDateAffectation(new \DateTimeImmutable('-10 days'));
        $a->setNote('Pulvérisateur prêt pour la campagne printemps.');
        $this->em->persist($a);
    }

    /**
     * @param list<Produit>   $produits
     * @param list<Materiel>  $materiels
     */
    private function seedRecommandations(array $produits, array $materiels): void
    {
        $r = new Recommandation();
        $r->setProduit($produits[0]);
        $r->setMateriel($materiels[1]);
        $r->setPriorite(4);
        $r->setRaison('Traitement préventif avant floraison.');
        $r->setActif(true);
        $this->em->persist($r);
    }

    /**
     * @param list<Produit> $produits
     */
    private function seedPromotions(array $produits): void
    {
        $p1 = new Promotion();
        $p1->setNom('Printemps -10 %');
        $p1->setDescription('Sur tomates et poivrons.');
        $p1->setTypeReduction(Promotion::TYPE_POURCENTAGE);
        $p1->setValeurReduction(10);
        $p1->setDateDebut(new \DateTimeImmutable('-2 days'));
        $p1->setDateFin(new \DateTimeImmutable('+30 days'));
        $p1->setQuantiteMin(1);
        $p1->setCumulable(false);
        $p1->setActif(true);
        $p1->addProduit($produits[0]);
        $p1->addProduit($produits[2]);
        $this->em->persist($p1);

        $p2 = new Promotion();
        $p2->setNom('Fidélité engrais');
        $p2->setDescription('Remise matérielle sur commande groupée.');
        $p2->setTypeReduction(Promotion::TYPE_MONTANT_FIXE);
        $p2->setValeurReduction(5);
        $p2->setDateDebut(new \DateTimeImmutable('today'));
        $p2->setDateFin(new \DateTimeImmutable('+14 days'));
        $p2->setQuantiteMin(2);
        $p2->setCumulable(true);
        $p2->setActif(true);
        // Promo « large » : aucun produit lié = toutes les lignes éligibles côté JavaFX
        $this->em->persist($p2);
    }

    private function seedBlog(Utilisateur $author, Utilisateur $commenter): Blog
    {
        $blog = new Blog();
        $blog->setTitleBlog('Agro-notes FARMTECH');
        $blog->setBlogTag('conseils,culture,bio');
        $blog->setUtilisateur($author);
        $this->em->persist($blog);
        $this->em->flush();

        $a1 = new Article();
        $a1->setTitle('Préparer la campagne tomates');
        $a1->setText('Rotation des cultures, analyse de sol et choix variétal adapté au séjour printanier.');
        $a1->setLikes(4);
        $a1->setDislikes(0);
        $a1->setEdited(false);
        $a1->setBlogId((int) $blog->getIdBlog());
        $this->em->persist($a1);

        $a2 = new Article();
        $a2->setTitle('Gestion de l’irrigation au goutte-à-goutte');
        $a2->setText('Réduire les pertes par évaporation et synchroniser avec la météo locale.');
        $a2->setLikes(7);
        $a2->setDislikes(1);
        $a2->setEdited(true);
        $a2->setBlogId((int) $blog->getIdBlog());
        $this->em->persist($a2);
        $this->em->flush();

        $c = new Commentaire();
        $c->setContenu('Très utile pour notre parcelle nord — merci !');
        $c->setArticle($a1);
        $c->setUtilisateur($commenter);
        $c->setDateCommentaire(new \DateTimeImmutable('-1 day'));
        $this->em->persist($c);

        return $blog;
    }

    /**
     * @param list<Utilisateur> $farmers
     * @param list<Utilisateur> $admins
     * @param list<Plantation>  $plantations
     * @param list<Produit>     $produits
     */
    private function seedProblemesAndDiagnostics(array $farmers, array $admins, array $plantations, array $produits): void
    {
        $snap = '{"temp":22.5,"description":"Partiellement nuageux","humidity":65}';

        $pb1 = new Probleme();
        $pb1->setUtilisateur($farmers[0]);
        $pb1->setType('Maladie fongique');
        $pb1->setDescription('Taches brunes sur les feuilles de tomates après période humide.');
        $pb1->setGravite('Moyenne');
        $pb1->setDateDetection(new \DateTimeImmutable('-5 days'));
        $pb1->setEtat('DIAGNOSTIQUE_DISPONIBLE');
        $pb1->setPhotos('uploads/problemes/demo_1_0.png;uploads/problemes/demo_1_1.png');
        $pb1->setIdPlantation($plantations[0]->getId());
        $pb1->setIdProduit($produits[0]->getIdProduit());
        $pb1->setMeteoSnapshot($snap);
        $pb1->setAdminAssignee($admins[1]);
        $this->em->persist($pb1);

        $pb2 = new Probleme();
        $pb2->setUtilisateur($farmers[1]);
        $pb2->setType('Ravageurs');
        $pb2->setDescription('Pucerons sur poivrons, colonies sous les feuilles.');
        $pb2->setGravite('Faible');
        $pb2->setDateDetection(new \DateTimeImmutable('-3 days'));
        $pb2->setEtat('CLOTURE');
        $pb2->setPhotos('uploads/problemes/demo_2_0.png');
        $pb2->setIdPlantation($plantations[2]->getId());
        $pb2->setIdProduit($produits[2]->getIdProduit());
        $pb2->setMeteoSnapshot($snap);
        $pb2->setAdminAssignee($admins[1]);
        $this->em->persist($pb2);

        $pb3 = new Probleme();
        $pb3->setUtilisateur($farmers[0]);
        $pb3->setType('Carence nutritive');
        $pb3->setDescription('Jaunissement feuilles basses sur pommes de terre.');
        $pb3->setGravite('Élevée');
        $pb3->setDateDetection(new \DateTimeImmutable('-2 days'));
        $pb3->setEtat('EN_ATTENTE');
        $pb3->setPhotos(null);
        $pb3->setIdPlantation($plantations[1]->getId());
        $pb3->setIdProduit($produits[1]->getIdProduit());
        $pb3->setMeteoSnapshot($snap);
        $this->em->persist($pb3);

        $pb4 = new Probleme();
        $pb4->setUtilisateur($farmers[2]);
        $pb4->setType('Maladie fongique');
        $pb4->setDescription('Mildiou suspecté sur tomates en serre.');
        $pb4->setGravite('Critique');
        $pb4->setDateDetection(new \DateTimeImmutable('-1 day'));
        $pb4->setEtat('REOUVERT');
        $pb4->setPhotos('uploads/problemes/demo_4_0.png');
        $pb4->setIdPlantation($plantations[3]->getId());
        $pb4->setIdProduit($produits[0]->getIdProduit());
        $pb4->setMeteoSnapshot($snap);
        $pb4->setAdminAssignee($admins[0]);
        $this->em->persist($pb4);

        $this->em->flush();

        $d1 = new Diagnostique();
        $d1->setProbleme($pb1);
        $d1->setCause('Alternariose favorisée par l’humidité.');
        $d1->setSolutionProposee('Retirer les feuilles atteintes, bouillie bordelaise, éviter de mouiller le feuillage.');
        $d1->setDateDiagnostique(new \DateTimeImmutable('-4 days'));
        $d1->setResultat('En attente');
        $d1->setMedicament('Bouillie bordelaise 20 g/L');
        $d1->setApprouve(true);
        $d1->setNumRevision(1);
        $d1->setAdminDiagnostiqueur($admins[1]);
        $this->em->persist($d1);

        $d2 = new Diagnostique();
        $d2->setProbleme($pb2);
        $d2->setCause('Pucerons (Aphis spp.).');
        $d2->setSolutionProposee('Savon noir 2 %, auxiliaires si disponibles.');
        $d2->setDateDiagnostique(new \DateTimeImmutable('-2 days'));
        $d2->setResultat('Résolu');
        $d2->setMedicament(null);
        $d2->setApprouve(true);
        $d2->setNumRevision(1);
        $d2->setFeedbackFermier('RESOLU');
        $d2->setFeedbackCommentaire('Savon noir efficace en deux applications.');
        $d2->setDateFeedback(new \DateTimeImmutable('-1 day'));
        $d2->setAdminDiagnostiqueur($admins[1]);
        $this->em->persist($d2);

        $d3 = new Diagnostique();
        $d3->setProbleme($pb3);
        $d3->setCause('Carence Mg/K, sol compacté.');
        $d3->setSolutionProposee('Engrais foliaire Mg-K, buttage, analyse de sol.');
        $d3->setDateDiagnostique(new \DateTimeImmutable());
        $d3->setResultat('En cours');
        $d3->setMedicament('Engrais foliaire Mg-K');
        $d3->setApprouve(false);
        $d3->setNumRevision(1);
        $this->em->persist($d3);

        $d4a = new Diagnostique();
        $d4a->setProbleme($pb4);
        $d4a->setCause('Mildiou (Phytophthora infestans).');
        $d4a->setSolutionProposee('Aération, fongicide systémique, arrosage au pied.');
        $d4a->setDateDiagnostique(new \DateTimeImmutable('-12 hours'));
        $d4a->setResultat('En attente');
        $d4a->setMedicament('Métalaxyl (AMM)');
        $d4a->setApprouve(true);
        $d4a->setNumRevision(1);
        $d4a->setFeedbackFermier('NON_RESOLU');
        $d4a->setFeedbackCommentaire('Persistance après premier traitement.');
        $d4a->setDateFeedback(new \DateTimeImmutable('-2 hours'));
        $d4a->setAdminDiagnostiqueur($admins[0]);
        $this->em->persist($d4a);

        $d4b = new Diagnostique();
        $d4b->setProbleme($pb4);
        $d4b->setCause('Mildiou résistant — révision protocole.');
        $d4b->setSolutionProposee('Alternance Métalaxyl + Mancozèbe tous les 5 jours.');
        $d4b->setDateDiagnostique(new \DateTimeImmutable());
        $d4b->setResultat('En attente');
        $d4b->setMedicament('Métalaxyl + Mancozèbe');
        $d4b->setApprouve(false);
        $d4b->setNumRevision(2);
        $d4b->setAdminDiagnostiqueur($admins[0]);
        $this->em->persist($d4b);
    }

    /**
     * @param list<Produit> $produits
     */
    private function seedProduitHistorique(array $produits): void
    {
        $h1 = new ProduitHistorique();
        $h1->setProduit($produits[0]);
        $h1->setTypeEvenement('ENTREE');
        $h1->setQuantiteAvant(null);
        $h1->setQuantiteApres(150);
        $h1->setDateEvenement(new \DateTimeImmutable('-7 days'));
        $h1->setCommentaire('Réception récolte tomates cerises');
        $this->em->persist($h1);

        $h2 = new ProduitHistorique();
        $h2->setProduit($produits[0]);
        $h2->setTypeEvenement('SORTIE');
        $h2->setQuantiteAvant(150);
        $h2->setQuantiteApres(120);
        $h2->setDateEvenement(new \DateTimeImmutable('-2 days'));
        $h2->setCommentaire('Vente coopérative');
        $this->em->persist($h2);

        $h3 = new ProduitHistorique();
        $h3->setProduit($produits[1]);
        $h3->setTypeEvenement('ENTREE');
        $h3->setQuantiteAvant(null);
        $h3->setQuantiteApres(500);
        $h3->setDateEvenement(new \DateTimeImmutable('-5 days'));
        $h3->setCommentaire('Stock pommes de terre');
        $this->em->persist($h3);
    }

    /**
     * @param list<Produit> $produits
     */
    private function seedFavoris(Connection $conn, array $produits): void
    {
        if (!$this->tableExists($conn, 'favoris')) {
            return;
        }
        $pid0 = (int) $produits[0]->getIdProduit();
        $pid2 = (int) $produits[2]->getIdProduit();
        $pid7 = (int) $produits[7]->getIdProduit();
        $conn->executeStatement(
            'INSERT INTO favoris (idProduit, dateAjout) VALUES (?, ?), (?, ?), (?, ?)',
            [
                $pid0, (new \DateTimeImmutable('-2 days'))->format('Y-m-d H:i:s'),
                $pid2, (new \DateTimeImmutable('-5 days'))->format('Y-m-d H:i:s'),
                $pid7, (new \DateTimeImmutable('-1 day'))->format('Y-m-d H:i:s'),
            ]
        );
    }
}
