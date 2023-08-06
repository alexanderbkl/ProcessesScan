package com.android.vidrebany.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.android.vidrebany.R;
import com.android.vidrebany.StandbyListActivity;
import com.android.vidrebany.models.ModelStandbyProcess;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdapterStandbyProcess extends RecyclerView.Adapter<AdapterStandbyProcess.MyHolder> {
    private final Context context;
    private final List<ModelStandbyProcess> standbyList;

    public AdapterStandbyProcess(Context context, List<ModelStandbyProcess> standbyList) {
        this.context = context;
        this.standbyList = standbyList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_standby_process, viewGroup, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder myHolder, int i) {

        String code = standbyList.get(i).getCode();
        Long startDate = standbyList.get(i).getStartedDate();
        String mark = standbyList.get(i).getMark();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy-HH:mm", Locale.GERMANY);
        String currentDateAndTime = sdf.format(new Date(startDate));


        myHolder.codeTv.setText(code);


        myHolder.startDateFieldTv.setText(currentDateAndTime);
        myHolder.markFieldTv.setText(mark);

        myHolder.resumeBtn.setOnClickListener(v -> {
            boolean result = StandbyListActivity.resumeProcess(code);
            if (result) {
                Toast.makeText(context, "Process resumed", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return standbyList.size();
    }

    static class MyHolder extends RecyclerView.ViewHolder {

        final CardView standbyLayout;
        final TextView codeTv, markFieldTv, startDateFieldTv;
        final Button resumeBtn;

        public MyHolder(@NonNull android.view.View itemView) {
            super(itemView);
            codeTv = itemView.findViewById(R.id.codeTv);
            resumeBtn = itemView.findViewById(R.id.resumeBtn);
            startDateFieldTv = itemView.findViewById(R.id.startDateFieldTv);
            markFieldTv = itemView.findViewById(R.id.markTv);
            standbyLayout = itemView.findViewById(R.id.standbyLayout);
        }
    }
}
