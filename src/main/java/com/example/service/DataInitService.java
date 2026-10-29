package com.example.service;

import com.example.model.Auteur;
import com.example.model.Livre;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

public class DataInitService {

    public static void initData(EntityManager em) {
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        String[] auteursNoms = {
                "Victor Hugo", "Emile Zola", "Albert Camus", "Marcel Proust", "Gustave Flaubert",
                "Jean-Paul Sartre", "Antoine de Saint-Exupery", "Alexandre Dumas", "Jules Verne", "George Sand"
        };

        for (int i = 0; i < auteursNoms.length; i++) {
            Auteur auteur = new Auteur(auteursNoms[i]);
            for (int j = 1; j <= 5; j++) {
                Livre livre = new Livre("Oeuvre Vol." + j + " - " + auteursNoms[i]);
                auteur.ajouterLivre(livre);
            }
            em.persist(auteur);
        }

        tx.commit();
        em.clear();
        System.out.println("-> Donnees initialisees : 10 auteurs et 50 livres enregistres.");
    }
}
