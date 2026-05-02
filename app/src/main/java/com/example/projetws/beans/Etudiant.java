package com.example.projetws.beans;

/**
 * Classe modèle représentant un étudiant.
 * Utilisée par Gson pour désérialiser la réponse JSON du Web Service.
 * Les noms des attributs doivent correspondre exactement aux clés JSON retournées par PHP.
 */
public class Etudiant {

    // Identifiant unique de l'étudiant (généré automatiquement par MySQL)
    private int id;

    // Informations personnelles de l'étudiant
    private String nom;
    private String prenom;
    private String ville;
    private String sexe;

    // Constructeur vide requis par Gson pour la désérialisation JSON
    public Etudiant() {}

    // Constructeur complet utilisé pour créer un étudiant manuellement
    public Etudiant(int id, String nom, String prenom, String ville, String sexe) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.ville = ville;
        this.sexe = sexe;
    }

    // Getters — permettent d'accéder aux données depuis d'autres classes
    public int getId() { return id; }
    public String getNom() { return nom; }
    public String getPrenom() { return prenom; }
    public String getVille() { return ville; }
    public String getSexe() { return sexe; }

    /**
     * Représentation textuelle de l'étudiant.
     * Affiché dans Logcat pour vérifier la désérialisation JSON.
     */
    @Override
    public String toString() {
        return "Etudiant{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", ville='" + ville + '\'' +
                ", sexe='" + sexe + '\'' +
                '}';
    }
}
