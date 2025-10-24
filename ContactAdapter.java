package com.s23010921.safezone;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
        holder.tvNumber.setText("Phone: " + contact.getNumber());
        holder.tvPriority.setText("Priority: " + contact.getPriority());

        // Click listener to open EditContact activity
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, EditContact.class);
            intent.putExtra("id", contact.getId());
            intent.putExtra("fname", contact.getFname());
            intent.putExtra("lname", contact.getLname());
            intent.putExtra("number", contact.getNumber());
            intent.putExtra("priority", contact.getPriority());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    public static class ContactViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvNumber, tvPriority;

        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvNumber = itemView.findViewById(R.id.tvNumber);
            tvPriority = itemView.findViewById(R.id.tvPriority);
        }
    }
}
