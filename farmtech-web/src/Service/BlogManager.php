<?php

namespace App\Service;

use App\Entity\Blog;
use Doctrine\ORM\EntityManagerInterface;

class BlogManager
{
    public function __construct(
        private readonly EntityManagerInterface $em
    ) {
    }

    public function save(Blog $blog): void
    {
        $this->em->persist($blog);
        $this->em->flush();
    }

    public function delete(Blog $blog): void
    {
        $this->em->remove($blog);
        $this->em->flush();
    }
}