package com.example.model;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "auteurs")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@NamedEntityGraph(
        name = "Auteur.livres",
        attributeNodes = @NamedAttributeNode("livres")
)
public class Auteur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nom_auteur", nullable = false)
    private String nom;

    @OneToMany(mappedBy = "auteur", cascade = CascadeType.ALL, orphanRemoval = true)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private List<Livre> livres = new ArrayList<>();

    public Auteur() {
    }

    public Auteur(String nomEcrivain) {
        this.nom = nomEcrivain;
    }

    public void ajouterLivre(Livre nouvelOuvrage) {
        if (nouvelOuvrage != null) {
            this.livres.add(nouvelOuvrage);
            nouvelOuvrage.setAuteur(this);
        }
    }

    public void retirerLivre(Livre ouvrageRetire) {
        if (ouvrageRetire != null) {
            this.livres.remove(ouvrageRetire);
            ouvrageRetire.setAuteur(null);
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long idAuteur) {
        this.id = idAuteur;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nomEcrivain) {
        this.nom = nomEcrivain;
    }

    public List<Livre> getLivres() {
        return livres;
    }

    public void setLivres(List<Livre> listeOuvrages) {
        this.livres = listeOuvrages;
    }

    @Override
    public String toString() {
        return "Auteur{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", totalLivres=" + (livres != null ? livres.size() : 0) +
                '}';
    }
}
