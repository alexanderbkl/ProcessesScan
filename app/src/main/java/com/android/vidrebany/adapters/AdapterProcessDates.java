package com.android.vidrebany.adapters;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static com.android.vidrebany.AccountActivity.isValidIndex;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.vidrebany.ProcessDateDetailsActivity;
import com.android.vidrebany.R;
import com.android.vidrebany.models.ModelDates;
import com.google.firebase.database.*;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class AdapterProcessDates extends RecyclerView.Adapter<AdapterProcessDates.MyHolder> {

    private final Context context;
    private final List<ModelDates> datesList;
    private final String process;
    private String code;
    private int sum = 0;

    public AdapterProcessDates(Context context, List<ModelDates> datesList, String process) {
        this.context = context;
        this.datesList = datesList;
        this.process = process;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_process_dates, viewGroup, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder myHolder, int i) {
        final int[] suma = {0};

        String safeProcess = process != null ? process : "montaje";
        String safeDate = datesList.get(i).getDate() != null ? datesList.get(i).getDate() : "01 01 1983";
        DatabaseReference processesRef = FirebaseDatabase.getInstance().getReference().child("processes").child(safeProcess).child(safeDate).child(safeDate);
        processesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {

                for(DataSnapshot data: snapshot.getChildren()){
                    if (data.child("code").getValue() != null) {
                        code = Objects.requireNonNull(data.child("code").getValue()).toString();
                        String[] codeparts = code.split("X");
                        int puntuacio;
                        if (isValidIndex(codeparts, 1)) {
                            puntuacio = Integer.parseInt(codeparts[1]);
                        } else {
                            puntuacio = 0;
                        }
                        suma[0] += puntuacio;

                    }


                }
                System.out.println(Arrays.toString(suma).substring(1, Arrays.toString(suma).length() - 1));
                myHolder.puntuacio.setText("Puntuació total: "+Arrays.toString(suma).substring(1, Arrays.toString(suma).length() - 1));
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference().child("processes").child(safeProcess).child(safeDate).child(safeDate);
        ordersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                sum = 0;
                sum += snapshot.getChildrenCount();
                myHolder.ordersTotalTv.setText("Nº ordres: "+sum);
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        myHolder.date.setText(datesList.get(i).getDate());

        myHolder.datesLayout.setOnClickListener(view -> {
                Intent intent = new Intent (context, ProcessDateDetailsActivity.class);
                intent.putExtra("date", datesList.get(i).getDate());
                intent.putExtra("process", process);
                intent.putExtra("puntuacio", Arrays.toString(suma).substring(1, Arrays.toString(suma).length() - 1));
                intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                context.getApplicationContext().startActivity(intent);
        });


    }

    @Override
    public int getItemCount() {
        return datesList.size();
    }

    static class MyHolder extends RecyclerView.ViewHolder {
        final TextView date;
        final TextView puntuacio;
        final TextView ordersTotalTv;
        final LinearLayout datesLayout;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            date = itemView.findViewById(R.id.dateTv);
            puntuacio = itemView.findViewById(R.id.puntuacio);
            datesLayout = itemView.findViewById(R.id.datesLayout);
            ordersTotalTv = itemView.findViewById(R.id.ordersTotalTv);
        }
    }
}
