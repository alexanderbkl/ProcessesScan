package com.android.vidrebany;

import android.view.Menu;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.android.vidrebany.adapters.AdapterDatesDetails;
import com.android.vidrebany.models.ModelDatesDetails;
import com.google.firebase.database.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static android.text.TextUtils.isEmpty;

public class DataDetailsActivity extends AppCompatActivity {

    private RecyclerView detailsRecyclerView;
    private AdapterDatesDetails adapterDatesDetails;
    private TextView nameTv, processTv, numberTv, puntuacioTv;
    private List<ModelDatesDetails> datesDetailsList;
    private String number, date = null, puntuacio, name, process;

    public DataDetailsActivity() {
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_details);

        nameTv = findViewById(R.id.nameTv);
        processTv = findViewById(R.id.processTv);
        numberTv = findViewById(R.id.numberTv);
        puntuacioTv = findViewById(R.id.puntuacioTv);

        Toolbar toolbar = findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                Intent intent = new Intent(DataDetailsActivity.this, LoginActivity.class);
                startActivity(intent);
            } else {
                name = extras.getString("name");
                process = extras.getString("process");
                number = extras.getString("number");
                date = extras.getString("date");
                puntuacio = extras.getString("puntuacio");
                Objects.requireNonNull(getSupportActionBar()).setTitle(date);
                toolbar.setTitle(date); //change actionbar title
                setContent();
            }
        } else {
            number = (String) savedInstanceState.getSerializable("number");
            date = (String) savedInstanceState.getSerializable("date");
        }



        number = numberTv.getText().toString();


        detailsRecyclerView = findViewById(R.id.detailsRecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        detailsRecyclerView.setLayoutManager(layoutManager);

        datesDetailsList = new ArrayList<>();

        loadDatesDetails();

    }
    private void setContent() {
        nameTv.setText(name);
        processTv.setText(process);
        numberTv.setText(number);
        puntuacioTv.setText(puntuacio);
    }

    private void loadDatesDetails() {
        DatabaseReference datesDetailsRef = FirebaseDatabase.getInstance().getReference().child("users").child(number).child("orders").child(date).child(date);
        datesDetailsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                datesDetailsList.clear();

                for (DataSnapshot ds: snapshot.getChildren()) {
                    ModelDatesDetails modelDatesDetails = ds.getValue(ModelDatesDetails.class);
                    assert modelDatesDetails != null;
                    datesDetailsList.add(modelDatesDetails);
                    adapterDatesDetails = new AdapterDatesDetails(DataDetailsActivity.this, datesDetailsList, date, number);
                    adapterDatesDetails.setHasStableIds(true);
                    detailsRecyclerView.setAdapter(adapterDatesDetails);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) item.getActionView();

        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                if (!isEmpty(s)) {
                    searchDatesDetails(s);
                } else {
                    loadDatesDetails();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (!isEmpty(s)) {
                    searchDatesDetails(s);
                } else {
                    loadDatesDetails();
                }
                return false;            }
        });
        return true;
    }


    private void searchDatesDetails(String s) {
        DatabaseReference datesDetailsRef = FirebaseDatabase.getInstance().getReference().child("users").child(number).child("orders").child(date).child(date);        datesDetailsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                datesDetailsList.clear();

                for (DataSnapshot ds: snapshot.getChildren()) {
                    ModelDatesDetails modelDatesDetails = ds.getValue(ModelDatesDetails.class);
                    assert modelDatesDetails != null;
                    String code = modelDatesDetails.getCode();
                    String started = modelDatesDetails.getStarted();
                    String ended = modelDatesDetails.getEnded();
                    String process = modelDatesDetails.getProcess();
                    if ((code != null && code.contains(s))
                            || (started != null && started.contains(s))
                            || (ended != null && ended.contains(s))
                            || (process != null && process.contains(s))) {
                        datesDetailsList.add(modelDatesDetails);
                    }


                    adapterDatesDetails = new AdapterDatesDetails(DataDetailsActivity.this, datesDetailsList, date, number);
                    adapterDatesDetails.setHasStableIds(true);
                    detailsRecyclerView.setAdapter(adapterDatesDetails);
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                loadDatesDetails();
            }
        });
    }




    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}