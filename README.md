# 📱 LAB 9 — Consommer un Web Service PHP depuis Android avec Volley

> **Auteur :** DOSSAH Landry  
> **Cours :** Programmation Mobile — ENSA Marrakech  
> **Date :** 02 Mai 2026  
> **Environnement :** Windows 11 · XAMPP 3.3.0 · Android Studio · Java · PHP 8 · MySQL

---

## Introduction

Ce lab met en place une architecture **client-serveur complète** entre une application Android et un backend PHP.  
L'objectif est de construire, exposer et consommer un **Web Service REST** capable de gérer une liste d'étudiants — de la base de données jusqu'à l'écran de l'émulateur.

Le lab couvre trois domaines complémentaires :
1. **Côté serveur** — Création d'une base MySQL et développement d'un Web Service PHP retournant du JSON
2. **Côté client Android** — Consommation du service via **Volley** et parsing de la réponse avec **Gson**
3. **Challenge** — Implémentation d'un CRUD complet avec RecyclerView, popup modifier/supprimer et rechargement automatique

---

## Architecture du projet

```
[Application Android]
        ↕  HTTP (Volley)
[Web Service PHP — localhost/projet/ws/]
        ↕  PDO
[Base de données MySQL — school1.Etudiant]
```

---

## Stack technique

| Côté | Technologie | Rôle |
|------|-------------|------|
| Base de données | MySQL (XAMPP) | Stockage des étudiants |
| Backend | PHP 8 + PDO | Web Service REST / JSON |
| Frontend | Android (Java) | Interface utilisateur |
| HTTP Client | Volley 1.2.1 | Requêtes GET et POST |
| JSON Parser | Gson 2.10.1 | Désérialisation JSON → Java |
| Test API | Postman | Validation des endpoints |

---

## Partie 1 — Base de données MySQL

### Démarrage de XAMPP
><img width="502" height="327" alt="apache et mysql activé" src="https://github.com/user-attachments/assets/6d150131-242d-41b9-a953-affeb49e74a5" />

Apache et MySQL ont été démarrés depuis le **XAMPP Control Panel** :

> ✅ Apache tourne sur le port **80/443**, MySQL sur **3306**.

### Création de la base et de la table

Via **phpMyAdmin** → onglet SQL :

> <img width="550" height="333" alt="Création de la base de données, table etudiant et enregistrements tests" src="https://github.com/user-attachments/assets/7fb12040-4f19-4083-873f-16ab07480a3c" />

---
### Résultat
> <img width="460" height="91" alt="Affichage des étudiants crées" src="https://github.com/user-attachments/assets/eca2fef5-ad9d-4864-b859-673fd40f94c1" />

## Partie 2 — Web Service PHP

### Structure du projet

```
C:\xampp\htdocs\projet\
├── classes\
│   └── Etudiant.php          # Classe modèle
├── connexion\
│   └── Connexion.php         # Connexion PDO à MySQL
├── dao\
│   └── IDao.php              # Interface CRUD
├── service\
│   └── EtudiantService.php   # Logique métier
└── ws\
    ├── loadEtudiant.php      # GET — liste tous les étudiants
    ├── createEtudiant.php    # POST — ajoute un étudiant
    ├── deleteEtudiant.php    # POST — supprime un étudiant
    └── updateEtudiant.php    # POST — modifie un étudiant
```

### Fichiers PHP

**`connexion/Connexion.php`** — Connexion PDO unique, réutilisée par le service :

```php
<?php
/**
 * Gestion de la connexion à la base MySQL via PDO.
 * PDO offre des requêtes préparées qui protègent contre les injections SQL.
 */
class Connexion {
    private $connexion;

    public function __construct() {
        try {
            // DSN : pilote mysql, hôte local, base school1, encodage UTF-8
            $this->connexion = new PDO(
                "mysql:host=localhost;dbname=school1;charset=utf8",
                "root",   // Utilisateur par défaut XAMPP
                ""        // Pas de mot de passe en local
            );
            // Active le mode exception pour attraper les erreurs SQL
            $this->connexion->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
        } catch (PDOException $e) {
            die('Erreur de connexion : ' . $e->getMessage());
        }
    }

    // Getter — retourne l'objet PDO aux classes qui en ont besoin
    public function getConnexion() {
        return $this->connexion;
    }
}
?>
```

**`classes/Etudiant.php`** — Classe modèle avec getters :

```php
<?php
/**
 * Représentation objet d'un étudiant.
 * Les getters sont utilisés par EtudiantService pour alimenter les requêtes préparées.
 */
class Etudiant {
    private $id, $nom, $prenom, $ville, $sexe;

    function __construct($id, $nom, $prenom, $ville, $sexe) {
        $this->id     = $id;
        $this->nom    = $nom;
        $this->prenom = $prenom;
        $this->ville  = $ville;
        $this->sexe   = $sexe;
    }

    function getNom()    { return $this->nom;    }
    function getPrenom() { return $this->prenom; }
    function getVille()  { return $this->ville;  }
    function getSexe()   { return $this->sexe;   }
}
?>
```

**`dao/IDao.php`** — Interface CRUD :

```php
<?php
/**
 * Interface définissant le contrat CRUD.
 * EtudiantService doit implémenter toutes ces méthodes.
 */
interface IDao {
    function create($o);
    function delete($o);
    function update($o);
    function findAll();
    function findById($id);
}
?>
```

**`service/EtudiantService.php`** — Logique métier complète :

```php
<?php
/**
 * Service principal gérant toutes les opérations sur la table Etudiant.
 * Utilise PDO avec des requêtes préparées pour sécuriser les données.
 */
include_once 'C:\xampp\htdocs\projet\classes\Etudiant.php';
include_once 'C:\xampp\htdocs\projet\connexion\Connexion.php';
include_once 'C:\xampp\htdocs\projet\dao\IDao.php';

class EtudiantService implements IDao {
    private $connexion;

    function __construct() {
        $this->connexion = new Connexion();
    }

    /**
     * INSERT — Ajoute un nouvel étudiant.
     * Les marqueurs :nom, :prenom... empêchent toute injection SQL.
     */
    public function create($o) {
        $query = "INSERT INTO Etudiant (nom, prenom, ville, sexe)
                  VALUES (:nom, :prenom, :ville, :sexe)";
        $stmt = $this->connexion->getConnexion()->prepare($query);
        $stmt->execute([
            ':nom'    => $o->getNom(),
            ':prenom' => $o->getPrenom(),
            ':ville'  => $o->getVille(),
            ':sexe'   => $o->getSexe()
        ]);
    }

    /**
     * DELETE — Supprime un étudiant par son ID.
     */
    public function deleteById($id) {
        $stmt = $this->connexion->getConnexion()
                     ->prepare("DELETE FROM Etudiant WHERE id = :id");
        $stmt->execute([':id' => $id]);
    }

    /**
     * UPDATE — Met à jour toutes les informations d'un étudiant existant.
     */
    public function updateById($id, $nom, $prenom, $ville, $sexe) {
        $query = "UPDATE Etudiant
                  SET nom=:nom, prenom=:prenom, ville=:ville, sexe=:sexe
                  WHERE id=:id";
        $stmt = $this->connexion->getConnexion()->prepare($query);
        $stmt->execute([
            ':id'     => $id,
            ':nom'    => $nom,
            ':prenom' => $prenom,
            ':ville'  => $ville,
            ':sexe'   => $sexe
        ]);
    }

    /**
     * SELECT * — Retourne tous les étudiants sous forme de tableau associatif.
     * FETCH_ASSOC produit directement les clés JSON attendues par Android (Gson).
     */
    public function findAllApi() {
        $req = $this->connexion->getConnexion()->query("SELECT * FROM Etudiant");
        return $req->fetchAll(PDO::FETCH_ASSOC);
    }

    // Méthodes de l'interface IDao non utilisées dans ce lab
    public function delete($o)    {}
    public function update($o)    {}
    public function findAll()     {}
    public function findById($id) {}
}
?>
```

**`ws/loadEtudiant.php`** — Endpoint GET :

```php
<?php
/**
 * Web Service GET — Retourne la liste complète des étudiants en JSON.
 * Appelé par Android au démarrage de ListEtudiant et après chaque opération.
 */
include_once 'C:\xampp\htdocs\projet\service\EtudiantService.php';
$es = new EtudiantService();

// Indique au client que la réponse est du JSON (important pour Volley/Gson)
header('Content-Type: application/json');
echo json_encode($es->findAllApi());
?>
```

**`ws/createEtudiant.php`** — Endpoint POST ajout :

```php
<?php
/**
 * Web Service POST — Insère un nouvel étudiant et retourne la liste mise à jour.
 * Les données arrivent via $_POST depuis Android (Volley x-www-form-urlencoded).
 */
if ($_SERVER["REQUEST_METHOD"] == "POST") {
    include_once 'C:\xampp\htdocs\projet\service\EtudiantService.php';

    // extract() transforme les clés $_POST en variables locales ($nom, $prenom...)
    extract($_POST);

    $es = new EtudiantService();
    $es->create(new Etudiant(1, $nom, $prenom, $ville, $sexe));

    header('Content-Type: application/json');
    echo json_encode($es->findAllApi());
}
?>
```

**`ws/deleteEtudiant.php`** — Endpoint POST suppression :

```php
<?php
/**
 * Web Service POST — Supprime un étudiant par son ID.
 * Reçoit l'ID via $_POST['id'] et retourne la liste mise à jour.
 */
if ($_SERVER["REQUEST_METHOD"] == "POST") {
    include_once 'C:\xampp\htdocs\projet\service\EtudiantService.php';

    $id = $_POST['id'];

    $es = new EtudiantService();
    $es->deleteById($id);

    header('Content-Type: application/json');
    echo json_encode($es->findAllApi());
}
?>
```

**`ws/updateEtudiant.php`** — Endpoint POST modification :

```php
<?php
/**
 * Web Service POST — Modifie un étudiant existant.
 * Reçoit l'ID + les nouvelles valeurs et retourne la liste mise à jour.
 */
if ($_SERVER["REQUEST_METHOD"] == "POST") {
    include_once 'C:\xampp\htdocs\projet\service\EtudiantService.php';

    $id     = $_POST['id'];
    $nom    = $_POST['nom'];
    $prenom = $_POST['prenom'];
    $ville  = $_POST['ville'];
    $sexe   = $_POST['sexe'];

    $es = new EtudiantService();
    $es->updateById($id, $nom, $prenom, $ville, $sexe);

    header('Content-Type: application/json');
    echo json_encode($es->findAllApi());
}
?>
```

### Test avec Postman

**GET — Chargement de la liste :**

> <img width="531" height="541" alt="Un GET nous montre tous les étudiants crées" src="https://github.com/user-attachments/assets/f6fad1f6-8617-48b7-b429-10c86d41b916" />

> ✅ `loadEtudiant.php` retourne le tableau JSON avec statut **200 OK**.

**POST — Création d'un étudiant :**

> <img width="535" height="277" alt="Création d&#39;un nouveau étudiant via post" src="https://github.com/user-attachments/assets/1379c108-9f66-4d92-8991-2724e9cf49fb" />


> Postman envoie les paramètres en `x-www-form-urlencoded` — exactement le format qu'utilisera Volley.

**Réponse après POST :**

> <img width="539" height="140" alt="Réponse obtenu après le post" src="https://github.com/user-attachments/assets/a8538231-29b5-45b6-897b-9c809be2487b" />


> ✅ Le serveur retourne la liste complète incluant le nouvel étudiant ajouté.
---

## Partie 3 — Application Android

Le code source complet de l'application Android est disponible sur GitHub :

| Fichier | Lien |
|---------|------|
| `beans/Etudiant.java` | [📄 Voir le fichier](https://github.com/m4jesty228/Lab9_Programmation_Mobile/blob/main/app/src/main/java/com/example/projetws/beans/Etudiant.java) |
| `AddEtudiant.java` | [📄 Voir le fichier](https://github.com/m4jesty228/Lab9_Programmation_Mobile/blob/main/app/src/main/java/com/example/projetws/AddEtudiant.java) |
| `AndroidManifest.xml` | [📄 Voir le fichier](https://github.com/m4jesty228/Lab9_Programmation_Mobile/blob/main/app/src/main/AndroidManifest.xml) |
| `build.gradle` | [📄 Voir le fichier](https://github.com/m4jesty228/Lab9_Programmation_Mobile/blob/main/app/build.gradle.kts) |

### Résultats

**Logcat — Étudiants désérialisés par Gson :**

> <img width="1701" height="689" alt="Etudiant reçu affiché dans logcat" src="https://github.com/user-attachments/assets/b70658f7-18bd-42a9-88ff-3c24297cfb0b" />

> ✅ Le filtre `ETUDIANT_RECU` confirme que Gson a correctement mappé le JSON en objets Java.

**Application — Toast de confirmation :**

> <img width="170" height="380" alt="Message attestant l&#39;ajout de l&#39;étudiant" src="https://github.com/user-attachments/assets/9d27576d-a977-47e3-8ada-9fe273bdac29" />

> ✅ "Étudiant ajouté avec succès !" s'affiche après chaque POST réussi.

**phpMyAdmin — Étudiants créés via l'app :**

> <img width="460" height="91" alt="Affichage des étudiants crées" src="https://github.com/user-attachments/assets/f953a6f5-e802-4e7f-99d7-1e31ae4a3517" />

---

## Challenge — RecyclerView CRUD complet

Le code source du challenge est disponible sur GitHub :

| Fichier | Lien |
|---------|------|
| `ListEtudiant.java` | [📄 Voir le fichier](https://github.com/m4jesty228/Lab9_Programmation_Mobile/blob/main/app/src/main/java/com/example/projetws/ListEtudiant.java) |
| `EtudiantAdapter.java` | [📄 Voir le fichier](https://github.com/m4jesty228/Lab9_Programmation_Mobile/blob/main/app/src/main/java/com/example/projetws/EtudiantAdapter.java) |
| `activity_list_etudiant.xml` | [📄 Voir le fichier](https://github.com/m4jesty228/Lab9_Programmation_Mobile/blob/main/app/src/main/res/layout/activity_list_etudiant.xml) |
| `item_etudiant.xml` | [📄 Voir le fichier](https://github.com/m4jesty228/Lab9_Programmation_Mobile/blob/main/app/src/main/res/layout/item_etudiant.xml) |

### Fonctionnalités

| Fonctionnalité | Détail |
|----------------|--------|
| Liste RecyclerView | Affiche tous les étudiants chargés depuis le Web Service |
| Popup au clic | Dialog avec options ✏️ Modifier et 🗑️ Supprimer |
| Formulaire de modification | Champs pré-remplis, confirmé par "Enregistrer" |
| Confirmation suppression | AlertDialog avant suppression définitive |
| Rechargement automatique | La liste se rafraîchit après chaque opération |

### Résultats

**RecyclerView — Liste des étudiants :**

> <img width="170" height="386" alt="Challenge 1" src="https://github.com/user-attachments/assets/f372ebdf-afb9-4979-888b-0c91af136917" />

> 🎬 **Vidéo de démonstration :**

> https://github.com/user-attachments/assets/e6d39b62-a37b-4ba0-a201-560064c807ed

---


## Récapitulatif

| Partie | Composant | Statut |
|--------|-----------|--------|
| MySQL | Base `school1` + table `Etudiant` | ✅ |
| PHP — GET | `loadEtudiant.php` | ✅ |
| PHP — POST | `createEtudiant.php` | ✅ |
| PHP — DELETE | `deleteEtudiant.php` | ✅ |
| PHP — UPDATE | `updateEtudiant.php` | ✅ |
| Android | `AddEtudiant` — formulaire POST Volley | ✅ |
| Android | `ListEtudiant` — RecyclerView CRUD | ✅ |
| Android | Gson — désérialisation JSON | ✅ |

---

## Points clés retenus

- **10.0.2.2** est l'adresse IP réservée par Android pour accéder au `localhost` de la machine hôte depuis l'émulateur.
- **Volley** gère les appels réseau en arrière-plan, évitant de bloquer le thread principal (UI thread).
- **Gson + TypeToken** permet de désérialiser un tableau JSON directement en `List<Etudiant>` sans parsing manuel.
- **PDO avec requêtes préparées** protège automatiquement contre les injections SQL.
- Le chemin absolu est nécessaire dans XAMPP car les chemins relatifs `../` peuvent échouer selon le contexte d'exécution.
- **RecyclerView + Adapter** : le pattern ViewHolder optimise le scroll en mettant en cache les références aux vues.

---

> **Auteur :** DOSSAH Landry  
> **GitHub :** [m4jesty228/Lab9_Programmation_Mobile](https://github.com/m4jesty228/Lab9_Programmation_Mobile)  
> **Date :** 02 Mai 2026  
> **Cours :** Programmation Mobile — ENSA Marrakech
