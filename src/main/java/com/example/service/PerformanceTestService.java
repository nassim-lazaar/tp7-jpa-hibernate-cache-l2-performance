package com.example.service;

import com.example.model.Auteur;

import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;

public class PerformanceTestService {

    public static long testPerformanceSansCache(EntityManager em) {
        em.clear();
        long start = System.currentTimeMillis();
        TypedQuery<Auteur> query = em.createQuery("SELECT a FROM Auteur a", Auteur.class);
        List<Auteur> auteurs = query.getResultList();
        int totalLivres = 0;
        for (Auteur auteur : auteurs) {
            totalLivres += auteur.getLivres().size(); // Declenche 1 requete SELECT par auteur (Probleme N+1)
        }
        long duration = System.currentTimeMillis() - start;
        System.out.println("  [N+1 SANS OPTIMISATION] " + auteurs.size() + " auteurs, " + totalLivres + " livres charges en " + duration + " ms");
        return duration;
    }

    public static long testPerformanceAvecJoinFetch(EntityManager em) {
        em.clear();
        long start = System.currentTimeMillis();
        TypedQuery<Auteur> query = em.createQuery("SELECT DISTINCT a FROM Auteur a JOIN FETCH a.livres", Auteur.class);
        List<Auteur> auteurs = query.getResultList();
        int totalLivres = 0;
        for (Auteur auteur : auteurs) {
            totalLivres += auteur.getLivres().size(); // Deja charge en memoire via le JOIN FETCH
        }
        long duration = System.currentTimeMillis() - start;
        System.out.println("  [AVEC JOIN FETCH] " + auteurs.size() + " auteurs, " + totalLivres + " livres charges en 1 requete unique en " + duration + " ms");
        return duration;
    }

    public static long testPerformanceAvecEntityGraph(EntityManager em) {
        em.clear();
        long start = System.currentTimeMillis();
        EntityGraph<?> graph = em.getEntityGraph("Auteur.livres");
        TypedQuery<Auteur> query = em.createQuery("SELECT DISTINCT a FROM Auteur a", Auteur.class);
        query.setHint("javax.persistence.fetchgraph", graph);
        List<Auteur> auteurs = query.getResultList();
        int totalLivres = 0;
        for (Auteur auteur : auteurs) {
            totalLivres += auteur.getLivres().size();
        }
        long duration = System.currentTimeMillis() - start;
        System.out.println("  [AVEC ENTITY GRAPH] " + auteurs.size() + " auteurs charges declarativement en 1 requete en " + duration + " ms");
        return duration;
    }
}
