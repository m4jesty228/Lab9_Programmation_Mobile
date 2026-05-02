package com.example.projetws;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.projetws.beans.Etudiant;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Activité permettant d'ajouter un étudiant via le Web Service PHP.
 * Utilise Volley pour envoyer une requête POST et Gson pour parser la réponse JSON.
 * L'adresse 10.0.2.2 est l'équivalent de localhost depuis l'émulateur Android.
 */
public class AddEtudiant extends AppCompatActivity implements View.OnClickListener {

    // Champs de saisie du formulaire
    private EditText nom, prenom;
    private Spinner ville;
    private RadioButton m, f;
    private Button add;

    // File de requêtes Volley — gère les appels HTTP en arrière-plan
    private RequestQueue requestQueue;

    // URL du Web Service PHP — 10.0.2.2 = localhost depuis l'émulateur
    private static final String INSERT_URL = "http://10.0.2.2/projet/ws/createEtudiant.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_etudiant);

        // Liaison des vues XML avec les variables Java
        nom = findViewById(R.id.nom);
        prenom = findViewById(R.id.prenom);
        ville = findViewById(R.id.ville);
        m = findViewById(R.id.m);
        f = findViewById(R.id.f);
        add = findViewById(R.id.add);

        // Initialisation de la file de requêtes Volley
        requestQueue = Volley.newRequestQueue(this);

        // Écoute du clic sur le bouton Ajouter
        add.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        // Vérifie que c'est bien le bouton Ajouter qui a été cliqué
        if (v == add) {
            envoyerEtudiant();
        }
    }

    /**
     * Envoie les données du formulaire au Web Service via une requête POST Volley.
     * En cas de succès, parse la réponse JSON avec Gson et affiche les étudiants dans Logcat.
     */
    private void envoyerEtudiant() {

        StringRequest request = new StringRequest(
                Request.Method.POST,
                INSERT_URL,

                // Callback succès — réponse reçue du serveur PHP
                response -> {
                    Log.d("REPONSE_SERVEUR", response);

                    // Gson désérialise le tableau JSON en Collection d'objets Etudiant
                    Type type = new TypeToken<Collection<Etudiant>>(){}.getType();
                    Collection<Etudiant> etudiants = new Gson().fromJson(response, type);

                    // Affiche chaque étudiant dans Logcat pour vérification
                    for (Etudiant e : etudiants) {
                        Log.d("ETUDIANT_RECU", e.toString());
                    }

                    // Message de confirmation à l'utilisateur
                    Toast.makeText(this, "Étudiant ajouté avec succès !", Toast.LENGTH_SHORT).show();
                },

                // Callback erreur — problème réseau ou serveur
                error -> Log.e("VOLLEY_ERREUR", "Erreur : " + error.getMessage())

        ) {
            /**
             * Retourne les paramètres POST envoyés au serveur PHP.
             * Correspond aux variables récupérées via $_POST en PHP.
             */
            @Override
            protected Map<String, String> getParams() {
                // Détermine le sexe selon le RadioButton sélectionné
                String sexe = m.isChecked() ? "homme" : "femme";

                Map<String, String> params = new HashMap<>();
                params.put("nom", nom.getText().toString().trim());
                params.put("prenom", prenom.getText().toString().trim());
                params.put("ville", ville.getSelectedItem().toString());
                params.put("sexe", sexe);
                return params;
            }
        };

        // Ajoute la requête à la file d'attente Volley
        requestQueue.add(request);
    }
}