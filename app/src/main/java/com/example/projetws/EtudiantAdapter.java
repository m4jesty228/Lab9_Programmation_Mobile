package com.example.projetws;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.projetws.beans.Etudiant;
import java.util.List;

/**
 * Adaptateur RecyclerView pour afficher la liste des étudiants.
 * Gère l'affichage, le popup de modification/suppression
 * et les callbacks vers l'activité parente.
 */
public class EtudiantAdapter extends RecyclerView.Adapter<EtudiantAdapter.EtudiantViewHolder> {

    // Contexte de l'activité parente
    private Context context;

    // Liste des étudiants à afficher
    private List<Etudiant> etudiants;

    // Interface de communication vers ListEtudiant.java
    private OnEtudiantActionListener listener;

    /**
     * Interface définissant les actions possibles sur un étudiant.
     * Implémentée par ListEtudiant.java pour gérer les appels réseau.
     */
    public interface OnEtudiantActionListener {
        void onSupprimer(Etudiant etudiant);
        void onModifier(Etudiant etudiant, String nom, String prenom, String ville, String sexe);
    }

    // Constructeur — reçoit le contexte, la liste et le listener
    public EtudiantAdapter(Context context, List<Etudiant> etudiants, OnEtudiantActionListener listener) {
        this.context = context;
        this.etudiants = etudiants;
        this.listener = listener;
    }

    /**
     * Crée et retourne un ViewHolder à partir du layout item_etudiant.xml.
     */
    @NonNull
    @Override
    public EtudiantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_etudiant, parent, false);
        return new EtudiantViewHolder(view);
    }

    /**
     * Remplit les vues d'un ViewHolder avec les données de l'étudiant correspondant.
     * Attache aussi le listener de clic pour afficher le popup.
     */
    @Override
    public void onBindViewHolder(@NonNull EtudiantViewHolder holder, int position) {
        Etudiant etudiant = etudiants.get(position);

        // Affichage du nom complet et des infos secondaires
        holder.tvNomPrenom.setText(etudiant.getNom() + " " + etudiant.getPrenom());
        holder.tvVilleSexe.setText("📍 " + etudiant.getVille() + "  |  " + etudiant.getSexe());

        // Au clic sur un élément — affiche le popup modifier/supprimer
        holder.itemView.setOnClickListener(v -> afficherPopup(etudiant));
    }

    @Override
    public int getItemCount() {
        return etudiants.size();
    }

    /**
     * Met à jour la liste après une opération réseau (ajout, modif, suppression).
     * Notifie le RecyclerView pour rafraîchir l'affichage.
     */
    public void mettreAJourListe(List<Etudiant> nouvelleListe) {
        this.etudiants = nouvelleListe;
        notifyDataSetChanged();
    }

    /**
     * Affiche un popup avec les options Modifier et Supprimer.
     * La suppression déclenche une alerte de confirmation.
     */
    private void afficherPopup(Etudiant etudiant) {
        // Options du popup
        String[] options = {"✏️ Modifier", "🗑️ Supprimer"};

        new AlertDialog.Builder(context)
                .setTitle(etudiant.getNom() + " " + etudiant.getPrenom())
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        // Option Modifier — affiche le formulaire de modification
                        afficherFormulaireModification(etudiant);
                    } else {
                        // Option Supprimer — demande confirmation
                        afficherConfirmationSuppression(etudiant);
                    }
                })
                .show();
    }

    /**
     * Affiche un formulaire de modification pré-rempli avec les données actuelles.
     * Envoie les nouvelles valeurs au listener si l'utilisateur confirme.
     */
    private void afficherFormulaireModification(Etudiant etudiant) {

        // Création dynamique des champs de saisie
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);

        // Champ Nom pré-rempli
        EditText etNom = new EditText(context);
        etNom.setHint("Nom");
        etNom.setText(etudiant.getNom());
        layout.addView(etNom);

        // Champ Prénom pré-rempli
        EditText etPrenom = new EditText(context);
        etPrenom.setHint("Prénom");
        etPrenom.setText(etudiant.getPrenom());
        layout.addView(etPrenom);

        // Champ Ville pré-rempli
        EditText etVille = new EditText(context);
        etVille.setHint("Ville");
        etVille.setText(etudiant.getVille());
        layout.addView(etVille);

        // Champ Sexe pré-rempli
        EditText etSexe = new EditText(context);
        etSexe.setHint("Sexe (homme/femme)");
        etSexe.setText(etudiant.getSexe());
        layout.addView(etSexe);

        new AlertDialog.Builder(context)
                .setTitle("Modifier l'étudiant")
                .setView(layout)
                .setPositiveButton("Enregistrer", (dialog, which) -> {
                    // Envoie les nouvelles données au listener (ListEtudiant.java)
                    listener.onModifier(
                            etudiant,
                            etNom.getText().toString().trim(),
                            etPrenom.getText().toString().trim(),
                            etVille.getText().toString().trim(),
                            etSexe.getText().toString().trim()
                    );
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    /**
     * Affiche une alerte de confirmation avant la suppression définitive.
     */
    private void afficherConfirmationSuppression(Etudiant etudiant) {
        new AlertDialog.Builder(context)
                .setTitle("Confirmation")
                .setMessage("Voulez-vous vraiment supprimer " + etudiant.getNom() + " ?")
                .setPositiveButton("Supprimer", (dialog, which) -> {
                    // Confirme la suppression — notifie le listener
                    listener.onSupprimer(etudiant);
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    /**
     * ViewHolder — contient les références aux vues d'un élément de la liste.
     */
    static class EtudiantViewHolder extends RecyclerView.ViewHolder {
        TextView tvNomPrenom, tvVilleSexe;

        public EtudiantViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNomPrenom = itemView.findViewById(R.id.tvNomPrenom);
            tvVilleSexe = itemView.findViewById(R.id.tvVilleSexe);
        }
    }
}