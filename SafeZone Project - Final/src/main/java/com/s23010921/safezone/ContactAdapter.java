package com.s23010921.safezone;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> {

    private List<ContactModel> contactList;
    private Context context;

    // Constructor to accept Context
    public ContactAdapter(Context context, List<ContactModel> contactList) {
        this.context = context;
        this.contactList = contactList;
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_item, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        ContactModel contact = contactList.get(position);
        holder.tvName.setText(contact.getFullName());
        holder.tvNumber.setText(contact.getNumber());
        holder.tvPriority.setText("Priority - " + contact.getPriority());

        // Expand/Collapse functionality
        holder.ivExpand.setOnClickListener(v -> {
            if (holder.expandedView.getVisibility() == View.VISIBLE) {
                holder.expandedView.setVisibility(View.GONE);
                holder.tvPriority.setVisibility(View.GONE);
                holder.ivExpand.setRotation(0);
            } else {
                holder.expandedView.setVisibility(View.VISIBLE);
                holder.tvPriority.setVisibility(View.VISIBLE);
                holder.ivExpand.setRotation(180);
            }
        });


        // Edit button click listener
        holder.btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(context, EditContact.class);
            intent.putExtra("id", contact.getId());
            intent.putExtra("fname", contact.getFname());
            intent.putExtra("lname", contact.getLname());
            intent.putExtra("number", contact.getNumber());
            intent.putExtra("priority", contact.getPriority());
            context.startActivity(intent);
        });

        // Delete button click listener
        holder.btnDelete.setOnClickListener(v -> {
            // Add delete functionality here
            deleteContact(contact, position);
        });

        // Also make the entire item clickable for expand/collapse
        holder.itemView.setOnClickListener(v -> {
            if (holder.expandedView.getVisibility() == View.VISIBLE) {
                holder.expandedView.setVisibility(View.GONE);
                holder.tvPriority.setVisibility(View.GONE);
                holder.ivExpand.setRotation(0);
            } else {
                holder.expandedView.setVisibility(View.VISIBLE);
                holder.tvPriority.setVisibility(View.VISIBLE);
                holder.ivExpand.setRotation(180);
            }
        });
    }

    private void deleteContact(ContactModel contact, int position) {
        DBHelper dbHelper = new DBHelper(context);
        boolean isDeleted = dbHelper.deleteContact(contact.getId());

        if (isDeleted) {
            contactList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, contactList.size());
        }
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    public static class ContactViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvNumber, tvPriority;
        ImageView ivExpand, ivUserIcon;
        LinearLayout expandedView;
        com.google.android.material.button.MaterialButton btnEdit, btnDelete;

        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            // Original TextViews
            tvName = itemView.findViewById(R.id.tvName);
            tvNumber = itemView.findViewById(R.id.tvNumber);
            tvPriority = itemView.findViewById(R.id.tvPriority);

            // New views from the updated XML
            ivExpand = itemView.findViewById(R.id.ivExpand);
            ivUserIcon = itemView.findViewById(R.id.ivUserIcon);
            expandedView = itemView.findViewById(R.id.expandedView);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}