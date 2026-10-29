package com.example;

import com.example.model.Auteur;
import com.example.model.Livre;
import com.example.service.DataInitService;
import org.hibernate.Session;
import org.hibernate.stat.Statistics;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.List;

import static org.junit.Assert.*;

public class CacheAndGraphTest {

    private EntityManagerFactory emf;
    private EntityManager em;

    @Before
    public void setUp() {
        emf = Persistence.createEntityManagerFactory("hibernate-cache-pu");
        em = emf.createEntityManager();
        DataInitService.initData(em);
    }

    @After
    public void tearDown() {
        if (em != null && em.isOpen()) em.close();
        if (emf != null && emf.isOpen()) emf.close();
    }

    @Test
    public void testJoinFetch() {
        List<Auteur> auteurs = em.createQuery("SELECT DISTINCT a FROM Auteur a JOIN FETCH a.livres", Auteur.class).getResultList();
        assertFalse(auteurs.isEmpty());
        for (Auteur a : auteurs) {
            assertNotNull(a.getLivres());
            assertEquals(5, a.getLivres().size());
        }
    }

    @Test
    public void testEntityGraph() {
        EntityGraph<?> graph = em.getEntityGraph("Auteur.livres");
        List<Auteur> auteurs = em.createQuery("SELECT DISTINCT a FROM Auteur a", Auteur.class)
                .setHint("javax.persistence.fetchgraph", graph)
                .getResultList();
        assertFalse(auteurs.isEmpty());
        for (Auteur a : auteurs) {
            assertEquals(5, a.getLivres().size());
        }
    }

    @Test
    public void testSecondLevelCache() {
        Session session = em.unwrap(Session.class);
        Statistics stats = session.getSessionFactory().getStatistics();
        stats.setStatisticsEnabled(true);
        stats.clear();

        // Load entity in first EM -> Miss + Put into L2
        EntityManager em1 = emf.createEntityManager();
        Auteur a1 = em1.find(Auteur.class, 1L);
        assertNotNull(a1);
        em1.close();

        long puts = stats.getSecondLevelCachePutCount();
        assertTrue(puts >= 1);

        // Load entity in second EM -> Hit from L2
        EntityManager em2 = emf.createEntityManager();
        Auteur a2 = em2.find(Auteur.class, 1L);
        assertNotNull(a2);
        em2.close();

        long hits = stats.getSecondLevelCacheHitCount();
        assertTrue(hits >= 1);
    }
}
