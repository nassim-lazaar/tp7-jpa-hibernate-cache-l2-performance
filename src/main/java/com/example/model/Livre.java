package com.example.model;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;

@Entity
@Table(name = "livres")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Livre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "titre_livre", nullable = false)
    private String titre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auteur_id")
    private Auteur auteur;

    public Livre() {
    }

    public Livre(String titreOuvrage) {
        this.titre = titreOuvrage;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long idLivre) {
        this.id = idLivre;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titreOuvrage) {
        this.titre = titreOuvrage;
    }

    public Auteur getAuteur() {
        return auteur;
    }

    public void setAuteur(Auteur ecrivain) {
        this.auteur = ecrivain;
    }

    @Override
    public String toString() {
        return "Livre{" +
                "id=" + id +
                ", titre='" + titre + '\'' +
                '}';
    }
}
