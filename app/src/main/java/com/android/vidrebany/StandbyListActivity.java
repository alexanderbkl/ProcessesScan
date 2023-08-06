package com.android.vidrebany;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.android.vidrebany.adapters.AdapterStandbyProcess;
import com.android.vidrebany.models.ModelStandbyProcess;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class StandbyListActivity extends AppCompatActivity {

    private RecyclerView standByRecyclerView;
    private List<ModelStandbyProcess> standbyList;
    private TextView nameFieldTv, processFieldTv, numberTv;
    private String name;
    private String process;
    private static String number;
    private static FirebaseDatabase fd;
    private static DatabaseReference dr;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stand_by_list);


        nameFieldTv = findViewById(R.id.nameFieldTv);
        processFieldTv = findViewById(R.id.processFieldTv);
        numberTv = findViewById(R.id.numberTv);
        fd = FirebaseDatabase.getInstance();

        toolbar = findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                Intent intent = new Intent(StandbyListActivity.this, UsersActivity.class);
                startActivity(intent);
            } else {
                name = extras.getString("name");
                process = extras.getString("process");
                number = extras.getString("number");
                setContent(name, process, number);


            }
        } else {
            name = (String) savedInstanceState.getSerializable("name");
            process = (String) savedInstanceState.getSerializable("process");
            number = (String) savedInstanceState.getSerializable("number");
            setContent(name, process, number);
        }

        standByRecyclerView = findViewById(R.id.standByRecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        standByRecyclerView.setLayoutManager(layoutManager);

        standbyList = new ArrayList<>();



        loadStandbyList();
    }


    private void setContent(String name, String process, String number) {
        if (name != null && process != null && number != null) {
            if (process.equalsIgnoreCase("montaje")) {
                nameFieldTv.setText(name);
                processFieldTv.setText(process);
                numberTv.setText(number);
            }
        }
    }

    private void loadStandbyList() {
        DatabaseReference datesDetailsRef = FirebaseDatabase.getInstance().getReference().child("users").child(number).child("standby");
        //standby orders look like following:
        /*
         * user's number
         * -> standby
         *   -> date
         *       -> date value
         *       -> date
         *           -> code
         *               -> code value (string)
         *               -> started date (string)
         *               -> process (string)
         */
        datesDetailsRef.addValueEventListener(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot dataSnapshot) {
                standbyList.clear();
                for (com.google.firebase.database.DataSnapshot ds : dataSnapshot.getChildren()) {
                        String code = "" + ds.child("code").getValue();
                        Long startedDate = Long.valueOf("" + ds.child("started").getValue());
                        String process = "" + ds.child("process").getValue();
                        String mark = "" + ds.child("mark").getValue();
                        standbyList.add(new ModelStandbyProcess(code, process, startedDate, mark));
                }
                AdapterStandbyProcess adapterStandbyProcess = new AdapterStandbyProcess(StandbyListActivity.this, standbyList);
                standByRecyclerView.setAdapter(adapterStandbyProcess);
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public static boolean resumeProcess(String code) {
        DatabaseReference usersRef = fd.getReference("users").child(number);
        usersRef.child("code").setValue(code);
        usersRef.child("standby").child(code).removeValue();

        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}