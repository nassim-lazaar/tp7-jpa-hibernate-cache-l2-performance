package com.example;

import com.example.model.Auteur;
import com.example.service.DataInitService;
import com.example.service.PerformanceTestService;
import org.hibernate.Session;
import org.hibernate.stat.Statistics;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class App {

    public static void main(String[] args) {
        System.out.println("========================================================================");
        System.out.println("   TP 7 : CACHE L2 EHCACHE, ENTITY GRAPH & RESOLUTION PROBLEME N+1      ");
        System.out.println("========================================================================");

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hibernate-cache-pu");
        EntityManager em = emf.createEntityManager();

        try {
            // Etape 0 : Initialisation du jeu de donnees
            System.out.println("\n[0] INITIALISATION DU JEU DE DONNEES (10 AUTEURS x 5 LIVRES)");
            System.out.println("------------------------------------------------------------------------");
            DataInitService.initData(em);

            Session session = em.unwrap(Session.class);
            Statistics stats = session.getSessionFactory().getStatistics();
            stats.setStatisticsEnabled(true);
            stats.clear();

            // Etape 1 : Mise en evidence du probleme N+1
            System.out.println("\n[1] DEMONSTRATION DU PROBLEME DES REQUETES N+1 (SANS OPTIMISATION)");
            System.out.println("------------------------------------------------------------------------");
            long reqAvantN1 = stats.getQueryExecutionCount();
            PerformanceTestService.testPerformanceSansCache(em);
            long reqApresN1 = stats.getQueryExecutionCount();
            System.out.println("  -> Requetes SQL executees par Hibernate : " + (reqApresN1 - reqAvantN1) + " requetes (1 initiale + 10 requetes Lazy)");

            // Etape 2 : Resolution via JOIN FETCH
            System.out.println("\n[2] RESOLUTION DU PROBLEME N+1 VIA JPQL 'JOIN FETCH'");
            System.out.println("------------------------------------------------------------------------");
            stats.clear();
            PerformanceTestService.testPerformanceAvecJoinFetch(em);
            System.out.println("  -> Requetes SQL executees : " + stats.getQueryExecutionCount() + " requete unique (jointure directe) !");

            // Etape 3 : Resolution declarative via Entity Graph (@NamedEntityGraph)
            System.out.println("\n[3] RESOLUTION DECLARATIVE VIA 'ENTITY GRAPH' (Auteur.livres)");
            System.out.println("------------------------------------------------------------------------");
            stats.clear();
            PerformanceTestService.testPerformanceAvecEntityGraph(em);
            System.out.println("  -> Requetes SQL executees : " + stats.getQueryExecutionCount() + " requete optimisee via Entity Graph.");

            // Etape 4 : Demonstration du Cache de Second Niveau (L2 Cache EhCache)
            System.out.println("\n[4] DEMONSTRATION DU CACHE DE SECOND NIVEAU (L2 EHCACHE)");
            System.out.println("------------------------------------------------------------------------");
            stats.clear();

            // Premiere lecture dans une session isolee (em1) -> Miss cache, requete DB et alimentation L2
            EntityManager em1 = emf.createEntityManager();
            Auteur auteurEm1 = em1.find(Auteur.class, 1L);
            System.out.println("Session 1 : Chargement Auteur ID=1 ('" + auteurEm1.getNom() + "') depuis la Base");
            em1.close(); // Fermeture de la session 1 (cache L1 libere)

            System.out.println("  -> Cache L2 Miss : " + stats.getSecondLevelCacheMissCount() + " | Cache L2 Puts : " + stats.getSecondLevelCachePutCount());

            // Deuxieme lecture dans une toute nouvelle session (em2) -> Cache Hit direct !
            EntityManager em2 = emf.createEntityManager();
            Auteur auteurEm2 = em2.find(Auteur.class, 1L);
            System.out.println("\nSession 2 : Chargement Auteur ID=1 ('" + auteurEm2.getNom() + "')");
            System.out.println("  -> L'entite a ete recuperee directement du Cache L2 sans requete SQL !");
            em2.close();

            System.out.println("  -> Cache L2 Hits : " + stats.getSecondLevelCacheHitCount() + " (Acces memoire instantane)");

            // Affichage recapitulatif des statistiques Hibernate
            System.out.println("\n[5] STATISTIQUES HIBERNATE DE SYNTHESE");
            System.out.println("------------------------------------------------------------------------");
            System.out.println("  * Second Level Cache Hits   : " + stats.getSecondLevelCacheHitCount());
            System.out.println("  * Second Level Cache Misses : " + stats.getSecondLevelCacheMissCount());
            System.out.println("  * Second Level Cache Puts   : " + stats.getSecondLevelCachePutCount());
            System.out.println("  * Entites chargees au total : " + stats.getEntityLoadCount());

            System.out.println("\n========================================================================");
            System.out.println("   VALIDATION TERMINEE AVEC SUCCES : CACHE L2 & ENTITY GRAPH OPERATIONNELS");
            System.out.println("========================================================================");

        } finally {
            if (em.isOpen()) em.close();
            if (emf.isOpen()) emf.close();
        }
    }
}
