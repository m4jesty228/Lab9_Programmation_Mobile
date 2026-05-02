package com.example.projetws;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.projetws.beans.Etudiant;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Activité principale affichant la liste complète des étudiants.
 * Gère le chargement, la modification et la suppression via Volley.
 * Se rafraîchit automatiquement après chaque opération.
 */
public class ListEtudiant extends AppCompatActivity
        implements EtudiantAdapter.OnEtudiantActionListener {

    // URLs des Web Services PHP
    private static final String LOAD_URL   = "http://10.0.2.2/projet/ws/loadEtudiant.php";
    private static final String DELETE_URL = "http://10.0.2.2/projet/ws/deleteEtudiant.php";
    private static final String UPDATE_URL = "http://10.0.2.2/projet/ws/updateEtudiant.php";

    // Composants de l'interface
    private RecyclerView recyclerView;
    private EtudiantAdapter adapter;

    // Liste locale des étudiants chargés depuis le serveur
    private List<Etudiant> listeEtudiants = new ArrayList<>();

    // File de requêtes Volley
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_etudiant);

        // Initialisation du RecyclerView avec un layout vertical
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Création de l'adaptateur avec le listener (this = ListEtudiant)
        adapter = new EtudiantAdapter(this, listeEtudiants, this);
        recyclerView.setAdapter(adapter);

        // Initialisation de Volley
        requestQueue = Volley.newRequestQueue(this);

        // Chargement initial de la liste
        chargerEtudiants();
    }

    /**
     * Charge tous les étudiants depuis le Web Service PHP via GET.
     * Met à jour le RecyclerView avec la réponse JSON parsée par Gson.
     */
    private void chargerEtudiants() {
        StringRequest request = new StringRequest(
                Request.Method.GET,
                LOAD_URL,
                response -> {
                    Log.d("LISTE_CHARGEE", response);

                    // Désérialisation du tableau JSON en List<Etudiant>
                    Type type = new TypeToken<List<Etudiant>>(){}.getType();
                    listeEtudiants = new Gson().fromJson(response, type);

                    // Rafraîchissement du RecyclerView
                    adapter.mettreAJourListe(listeEtudiants);
                },
                error -> {
                    Log.e("VOLLEY_ERREUR", "Erreur chargement : " + error.getMessage());
                    Toast.makeText(this, "Erreur de chargement", Toast.LENGTH_SHORT).show();
                }
        );
        requestQueue.add(request);
    }

    /**
     * Callback déclenché par l'adaptateur quand l'utilisateur confirme la suppression.
     * Envoie l'ID de l'étudiant au Web Service et recharge la liste.
     */
    @Override
    public void onSupprimer(Etudiant etudiant) {
        StringRequest request = new StringRequest(
                Request.Method.POST,
                DELETE_URL,
                response -> {
                    Toast.makeText(this, "Étudiant supprimé !", Toast.LENGTH_SHORT).show();
                    // Rechargement automatique après suppression
                    chargerEtudiants();
                },
                error -> Log.e("VOLLEY_ERREUR", "Erreur suppression : " + error.getMessage())
        ) {
            @Override
            protected Map<String, String> getParams() {
                // Envoi de l'ID de l'étudiant à supprimer
                Map<String, String> params = new HashMap<>();
                params.put("id", String.valueOf(etudiant.getId()));
                return params;
            }
        };
        requestQueue.add(request);
    }

    /**
     * Callback déclenché par l'adaptateur quand l'utilisateur confirme la modification.
     * Envoie les nouvelles données au Web Service et recharge la liste.
     */
    @Override
    public void onModifier(Etudiant etudiant, String nom, String prenom, String ville, String sexe) {
        StringRequest request = new StringRequest(
                Request.Method.POST,
                UPDATE_URL,
                response -> {
                    Toast.makeText(this, "Étudiant modifié !", Toast.LENGTH_SHORT).show();
                    // Rechargement automatique après modification
                    chargerEtudiants();
                },
                error -> Log.e("VOLLEY_ERREUR", "Erreur modification : " + error.getMessage())
        ) {
            @Override
            protected Map<String, String> getParams() {
                // Envoi de toutes les données modifiées
                Map<String, String> params = new HashMap<>();
                params.put("id",     String.valueOf(etudiant.getId()));
                params.put("nom",    nom);
                params.put("prenom", prenom);
                params.put("ville",  ville);
                params.put("sexe",   sexe);
                return params;
            }
        };
        requestQueue.add(request);
    }
}
