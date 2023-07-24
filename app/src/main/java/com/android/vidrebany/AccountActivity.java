package com.android.vidrebany;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.widget.Toolbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class AccountActivity extends AppCompatActivity {

    private FirebaseDatabase fd;
    private DatabaseReference dr;
    TextView codeTv, processTv, startedTv, nameTv, endedTv, numberTv, errorTv, infoTv;
    Button scanBtn, cancelBtn;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        setContentView(R.layout.activity_account);

        toolbar = findViewById(R.id.toolbar_main);

        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Compte");

        codeTv = findViewById(R.id.codeTv);
        processTv = findViewById(R.id.processTv);
        startedTv = findViewById(R.id.startedTv);
        scanBtn = findViewById(R.id.scanBtn);
        nameTv = findViewById(R.id.nameTv);
        endedTv = findViewById(R.id.endedTv);
        numberTv = findViewById(R.id.numberTv);
        errorTv = findViewById(R.id.errorTv);
        infoTv = findViewById(R.id.infoTv);
        cancelBtn = findViewById(R.id.cancelBtn);
        scanBtn.setOnClickListener(view -> scan());
        errorTv.setVisibility(View.GONE);
        cancelBtn.setVisibility(View.GONE);

        String name, process, number;
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                Intent intent = new Intent(AccountActivity.this, UsersActivity.class);
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




    }



    private void setContent(String name, String process, String number){
        errorTv.setVisibility(View.GONE);
        String processState = "PROCÉS: "+process;
        String currentProcess = process.toLowerCase();
        boolean fastProcesses =
                currentProcess.equals("corte") ||
                currentProcess.equals("admin") ||
                currentProcess.equals("canteado") ||
                currentProcess.equals("mecanizado") ||
                currentProcess.equals("laca") ||
                currentProcess.equals("embalaje") ||
                currentProcess.equals("cajones") ||
                currentProcess.equals("espejos") ||
                currentProcess.equals("unero");
        dr = FirebaseDatabase.getInstance().getReference("users").child(number);

        dr.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String barcode = snapshot.child("code").getValue(String.class);

                if (barcode != null) {
                    String[] codeparts = barcode.split("X");

                    String puntuacio;
                    if (isValidIndex(codeparts, 1)) {
                        puntuacio = codeparts[1];
                    } else {
                        puntuacio = "s/p";
                    }

                    String codeNumber = "CODI: "+codeparts[0]+" PUNTUACIÓ: "+puntuacio;



                    codeTv.setText(codeNumber);

                    if (!barcode.equals("sense codi")) {
                        dr = FirebaseDatabase.getInstance().getReference("codes").child(barcode);

                        dr.addValueEventListener(new ValueEventListener() {
                            @SuppressLint("SetTextI18n")
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.child(process.toLowerCase()+"Started").getValue(Long.class) != null) {
                                    long started = snapshot.child(process.toLowerCase()+"Started").getValue(Long.class);
                                    Date startedDate = new Date(started);
                                    try {
                                        long ended = snapshot.child(process.toLowerCase()+"Ended").getValue(Long.class);
                                        Date endedDate = null;
                                        if (ended != 0) {
                                            ended = snapshot.child(process.toLowerCase()+"Ended").getValue(Long.class);
                                            endedDate = new Date(ended);
                                        }
                                        String endedOn;
                                        if (fastProcesses && ended != 0) {
                                            endedOn = "";
                                        } else {
                                            endedOn = "ACABAT: "+endedDate;

                                        }
                                        endedTv.setText(endedOn);

                                    } catch (NullPointerException e) {
                                        String endedOn;

                                        endedOn = "";
                                        endedTv.setText(endedOn);

                                    }


                                    @SuppressLint("SimpleDateFormat") DateFormat df = new SimpleDateFormat("dd/MM/yy-HH:mm");
                                    @SuppressLint("SimpleDateFormat") DateFormat df2 = new SimpleDateFormat("dd MM yyyy");
                                    String startedOn;
                                    if (fastProcesses) {
                                        startedOn = "PROCESSAT: \n"+df.format(startedDate);
                                    } else {
                                        startedOn = "COMENÇAT: "+df.format(startedDate);
                                    }
                                    String startedDateFormatted = df2.format(startedDate);
                                    startedTv.setText(startedOn);
                                    if (currentProcess.equals("montaje")
                                            && !snapshot.child("cajonesEnded").exists()
                                            && !snapshot.child("montajeEnded").exists()) {
                                        infoTv.setText("El procés cajones NO és present.");
                                        infoTv.setVisibility(View.VISIBLE);}
                                    else if (currentProcess.equals("montaje")
                                            && snapshot.child("cajonesEnded").exists()
                                            && !snapshot.child("montajeEnded").exists()) {
                                        infoTv.setText("El procés cajones SÍ és present.");
                                        infoTv.setVisibility(View.VISIBLE);
                                    }



                                    cancelBtn.setVisibility(View.VISIBLE);
                                    cancelBtn.setOnClickListener(view -> cancelOrder(barcode, process, number, startedDateFormatted));
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });



                    } else {
                        String startedOn = "COMENÇAT: sense començar";
                        startedTv.setText(startedOn);
                        endedTv.setText("");
                        errorTv.setVisibility(View.GONE);
                        cancelBtn.setVisibility(View.GONE);
                    }


                    nameTv.setText(name);

                    startedTv.setSingleLine(false);
                    endedTv.setSingleLine(false);



                    processTv.setText(processState);
                    numberTv.setText(number);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



    }

    private void cancelOrder(String barcode, String process, String number, String startedDate) {
        cancelBtn.setVisibility(View.GONE);

        String codeNumber = "CODI: sense codi";
        String started = "COMENÇAT: sense començar";
        codeTv.setText(codeNumber);
        startedTv.setText(started);

       fd = FirebaseDatabase.getInstance();
       dr = fd.getReference("codes").child(barcode);
       dr.child(process.toLowerCase()).removeValue();
       dr.child(process.toLowerCase()+"Started").removeValue();
       dr.child(process.toLowerCase()+"Ended").removeValue();
       dr.child(process.toLowerCase()+"User").removeValue();

        fd = FirebaseDatabase.getInstance();
            DatabaseReference processesRef = fd.getReference("processes").child(process.toLowerCase()).child(""+startedDate).child(""+startedDate).child(barcode);

        processesRef.removeValue();


        fd = FirebaseDatabase.getInstance();
        DatabaseReference userRef = fd.getReference("users").child(number).child("orders").child(""+startedDate).child(""+startedDate).child(barcode);
        userRef.removeValue();

        cancelBtn.setVisibility(View.GONE);
    }

    private void scan() {
        errorTv.setVisibility(View.GONE);
        IntentIntegrator intentIntegrator = new IntentIntegrator(
                AccountActivity.this
        );
        intentIntegrator.setPrompt("Per utilitzar flash prémer pujar volum")
                .setBeepEnabled(true)
                .setOrientationLocked(true)
                .setCaptureActivity(Capture.class)
                .initiateScan();

    }

    private void endProcess(String process, String code, boolean fastProcess) {
        String currentProcess = process.toLowerCase();

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy-HH:mm", Locale.GERMANY);
        String currentDateAndTime = sdf.format(new Date());
        SimpleDateFormat sdf1 = new SimpleDateFormat("dd MM yyyy", Locale.GERMANY);
        String currentDate = sdf1.format(new Date());
        errorTv.setVisibility(View.GONE);
        String processState = "PROCÉS: "+process;

        String[] codeparts = code.split("X");


        String endedOn;
        if (fastProcess) {
            endedOn = "";
        } else {
            endedOn = "ACABAT: \n"+currentDateAndTime;
        }



        cancelBtn.setVisibility(View.GONE);


        String puntuacio;
        if (isValidIndex(codeparts, 1)) {
            puntuacio = codeparts[1];
        } else {
            puntuacio = "s/p";
        }
        String codeNumber = "CODI: "+codeparts[0]+" PUNTUACIÓ: "+puntuacio;
        String number = numberTv.getText().toString();


        cancelBtn.setVisibility(View.VISIBLE);
        startedTv.setSingleLine(false);

        processTv.setText(processState);
        codeTv.setText(codeNumber);

        // Write a message to the database
        fd = FirebaseDatabase.getInstance();
        dr = fd.getReference("codes").child(code);
        dr.child("code").setValue(code);
        dr.child(process.toLowerCase()).setValue(true);
        dr.child(process.toLowerCase()+"Ended").setValue(System.currentTimeMillis());



        endedTv.setSingleLine(false);
        endedTv.setText(endedOn);
        cancelBtn.setVisibility(View.GONE);
        processTv.setText(processState);

        errorTv.setVisibility(View.GONE);
        DatabaseReference ordersRef = fd.getReference("users").child(number);
        DatabaseReference processesRef = fd.getReference("processes").child(process.toLowerCase());



        ordersRef.child("code").setValue("sense codi");
        ordersRef.child("orders").child(currentDate).child(currentDate).child(code).child("ended").setValue(currentDateAndTime);

        processesRef.child(currentDate).child(currentDate).child(code).child("ended").setValue(currentDateAndTime);

    }
    public static boolean isValidIndex(String[] arr, int index) {
        return index >= 0 && index < arr.length;
    }
    private void currentProcess(String code,
                                String process, String user,
                                String number, boolean fastProcess) {
        String currentProcess = process.toLowerCase();
        boolean fastProcesses = currentProcess.equals("corte") ||
                currentProcess.equals("admin") ||
                currentProcess.equals("canteado") ||
                currentProcess.equals("mecanizado") ||
                currentProcess.equals("laca") ||
                currentProcess.equals("embalaje") ||
                currentProcess.equals("cajones") ||
                currentProcess.equals("espejos") ||
                currentProcess.equals("unero");


        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy-HH:mm", Locale.GERMANY);
        String currentDateAndTime = sdf.format(new Date());
        SimpleDateFormat sdf1 = new SimpleDateFormat("dd MM yyyy", Locale.GERMANY);
        String currentDate = sdf1.format(new Date());
        errorTv.setVisibility(View.GONE);
        String processState = "PROCÉS: "+process;

        String[] codeparts = code.split("X");

        String puntuacio;
        if (isValidIndex(codeparts, 1)) {
            puntuacio = codeparts[1];
        } else {
            puntuacio = "s/p";
        }
        String codeNumber = "CODI: "+codeparts[0]+"\nPUNTUACIÓ: "+puntuacio;
        String startedOn;
        if (fastProcess) {
            startedOn = "PROCESSAT: \n"+currentDateAndTime;
        } else {
            startedOn = "COMENÇAT: \n"+currentDateAndTime;
        }
        String endedOn;

        // Write a message to the database
        fd = FirebaseDatabase.getInstance();
        dr = fd.getReference("codes").child(code);
        dr.child("code").setValue(code);
        dr.child(process.toLowerCase()+"User").setValue(user);
        dr.child(process.toLowerCase(Locale.ROOT)).setValue(true);
        DatabaseReference ordersRef = fd.getReference("users").child(number);
        DatabaseReference processesRef = fd.getReference("processes").child(process.toLowerCase());
        if (fastProcesses) {
            endedOn = "";
            dr.child(process.toLowerCase()+"Started").setValue(System.currentTimeMillis());
            dr.child(process.toLowerCase()+"Ended").setValue(System.currentTimeMillis());
            ordersRef.child("code").setValue(code);
            ordersRef.child("orders").child(currentDate).child(currentDate).child(code).child("ended").setValue(currentDateAndTime);

            processesRef.child(currentDate).child(currentDate).child(code).child("ended").setValue(currentDateAndTime);
        } else {

            endedOn = "ACABAT: sense acabar";
            cancelBtn.setVisibility(View.VISIBLE);
            dr.child(process.toLowerCase()+"Started").setValue(System.currentTimeMillis());
            ordersRef.child("code").setValue(code);
        }

        startedTv.setSingleLine(false);
        startedTv.setText(startedOn);

        processTv.setText(processState);
        endedTv.setText(endedOn);
        codeTv.setText(codeNumber);
        cancelBtn.setOnClickListener(view -> cancelOrder(code, process, number, currentDate));

        ordersRef.child("orders").child(currentDate).child(currentDate).child(code).child("started").setValue(currentDateAndTime);
        ordersRef.child("orders").child(currentDate).child("date").setValue(currentDate);
        ordersRef.child("orders").child(currentDate).child(currentDate).child(code).child("code").setValue(code);
        ordersRef.child("orders").child(currentDate).child(currentDate).child(code).child("process").setValue(process);

        processesRef.child(currentDate).child(currentDate).child(code).child("started").setValue(currentDateAndTime);
        processesRef.child(currentDate).child("date").setValue(currentDate);
        processesRef.child(currentDate).child(currentDate).child(code).child("code").setValue(code);
        processesRef.child(currentDate).child(currentDate).child(code).child("user").setValue(user);
    }

    private String intentResult(String contents) {
        return contents;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult intentResult = IntentIntegrator.parseActivityResult(
                requestCode,resultCode,data
        );
        if (intentResult.getContents() != null){
            String barcode = intentResult(intentResult.getContents());
            String temp = processTv.getText().toString();
            String process = temp.substring(temp.lastIndexOf(" ")+1);
            String name = nameTv.getText().toString();
            fd = FirebaseDatabase.getInstance();
            //check that barcode doesn't have slashes or two dots
            if (barcode.contains("/") || barcode.contains(".")) {
                errorTv.setVisibility(View.VISIBLE);
                errorTv.setText("El codi de barres no pot contenir / o .");
            } else {
                dr = fd.getReference("codes").child(barcode);
            }


            dr.addListenerForSingleValueEvent(new ValueEventListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                   // boolean corte = snapshot.child("corte").getValue();
                    String currentProcess = process.toLowerCase();

                    //temporarily commented out processes order
                    /*
                    boolean fastProcesses = currentProcess.equals("corte") ||
                            currentProcess.equals("admin") ||
                            currentProcess.equals("canteado") ||
                            currentProcess.equals("mecanizado") ||
                            currentProcess.equals("laca") ||
                            currentProcess.equals("embalaje") ||
                            currentProcess.equals("cajones") ||
                            currentProcess.equals("espejos") ||
                            currentProcess.equals("unero");
                    if (
                            currentProcess.equals("montaje")
                            && snapshot.child("cajonesEnded").exists()
                            && !snapshot.child("montajeStarted").exists()
                            && snapshot.child("lacaEnded").exists()||
                            currentProcess.equals("montaje")
                            && !snapshot.child("montajeStarted").exists()
                            && snapshot.child("mecanizadoEnded").exists()
                            && snapshot.child("cajonesEnded").exists()
                    ) {
                            infoTv.setText("El procés cajones SÍ és present.");

                        infoTv.setVisibility(View.VISIBLE);
                            currentProcess(barcode,process,name, numberTv.getText().toString(), false);
                    }
                    else if (
                            currentProcess.equals("montaje")
                            && !snapshot.child("cajonesEnded").exists()
                            && !snapshot.child("montajeStarted").exists()
                            && snapshot.child("lacaEnded").exists()||
                            currentProcess.equals("montaje")
                            && !snapshot.child("montajeStarted").exists()
                            && snapshot.child("mecanizadoEnded").exists()
                            && !snapshot.child("cajonesEnded").exists()) {
                        infoTv.setText("El procés cajones NO és present.");
                        infoTv.setVisibility(View.VISIBLE);
                        currentProcess(barcode,process,name, numberTv.getText().toString(), false);
                    }
                    else if (currentProcess.equals("montaje")
                    && !snapshot.child("cajonesEnded").exists()
                    && snapshot.child("montajeStarted").exists()
                    && snapshot.child("lacaEnded").exists()||
                    currentProcess.equals("montaje")
                    && snapshot.child("montajeStarted").exists()
                    && snapshot.child("mecanizadoEnded").exists()
                    && !snapshot.child("cajonesEnded").exists()) {
                        infoTv.setText("El procés cajones NO és present.");
                        infoTv.setVisibility(View.GONE);
                        endProcess(process, barcode, false);

                    } else if (
                            currentProcess.equals("montaje")
                                    && snapshot.child("cajonesEnded").exists()
                                    && snapshot.child("montajeStarted").exists()
                                    && snapshot.child("lacaEnded").exists()||
                                    currentProcess.equals("montaje")
                                            && snapshot.child("montajeStarted").exists()
                                            && snapshot.child("mecanizadoEnded").exists()
                                            && snapshot.child("cajonesEnded").exists()
                    ) {
                        infoTv.setText("El procés cajones SÍ és present.");

                        infoTv.setVisibility(View.GONE);
                        endProcess(process, barcode, false);
                    }
                    else if (currentProcess.equals("corte") && snapshot.child("adminEnded").exists() && !snapshot.child("corteStarted").exists()||
                            currentProcess.equals("canteado") && snapshot.child("corteEnded").exists() && !snapshot.child("canteadoStarted").exists()||
                            currentProcess.equals("mecanizado") && snapshot.child("canteadoEnded").exists() && !snapshot.child("mecanizadoStarted").exists()||
                            currentProcess.equals("laca") && snapshot.child("mecanizadoEnded").exists() && !snapshot.child("lacaStarted").exists()||
                            currentProcess.equals("embalaje")
                                    && snapshot.child("montajeEnded").exists()&&snapshot.child("espejosEnded").exists() && snapshot.child("cajonesEnded").exists() && snapshot.child("uneroEnded").exists() && !snapshot.child("embalajeStarted").exists()
                           // ||
                           // currentProcess.equals("transporte") && !snapshot.child("transporteStarted").exists()
                           //         && snapshot.child("embalajeEnded").exists()
                    ) {
                        currentProcess(barcode, process, name, numberTv.getText().toString(), true);

                    } else if (!fastProcesses && snapshot.child(currentProcess+"Started").exists() && Objects.equals(Objects.requireNonNull(snapshot.child(currentProcess + "User").getValue()).toString(), nameTv.getText().toString())) {
                        endProcess(process, barcode, false);





                    } else if (currentProcess.equals("admin") && !snapshot.child("adminStarted").exists()||
                            currentProcess.equals("unero") && !snapshot.child("uneroStarted").exists()||
                            currentProcess.equals("espejos") && !snapshot.child("espejosStarted").exists()) {
                        currentProcess(barcode, process, name, numberTv.getText().toString(), true);
                    } else if (currentProcess.equals("cajones") && !snapshot.child("cajonesStarted").exists()||
                            currentProcess.equals("unero") && !snapshot.child("uneroStarted").exists() ||
                            currentProcess.equals("espejos") && !snapshot.child("espejosStarted").exists()) {
                        currentProcess(barcode, process, name, numberTv.getText().toString(), true);
                    }

                    else if (currentProcess.equals("admin") && snapshot.child("adminStarted").exists()||
                            currentProcess.equals("corte") && snapshot.child("corteStarted").exists()||
                            currentProcess.equals("canteado") && snapshot.child("canteadoStarted").exists()||
                            currentProcess.equals("mecanizado") && snapshot.child("mecanizadoStarted").exists()||
                            currentProcess.equals("laca") && snapshot.child("lacaStarted").exists()||
                            currentProcess.equals("embalaje") && snapshot.child("embalajeStarted").exists()||
                            currentProcess.equals("transporte") && snapshot.child("transporteStarted").exists()||
                            currentProcess.equals("cajones") && snapshot.child("cajonesStarted").exists()||
                            currentProcess.equals("unero") && snapshot.child("uneroStarted").exists()||
                            currentProcess.equals("espejos") && snapshot.child("espejosStarted").exists())
                    {
                        errorTv.setText("El procés ja ha començat.");
                        errorTv.setVisibility(View.VISIBLE);
                    } else {
                        errorTv.setText("El procés anterior no s'ha complert.");
                        errorTv.setVisibility(View.VISIBLE);
                    }
                    */

                    /*
                    if (snapshot.child(currentProcess+"Started").exists() && Objects.equals(Objects.requireNonNull(snapshot.child(currentProcess + "User").getValue()).toString(), nameTv.getText().toString())) {
                        endProcess(process, barcode, false);
                    } else {
                         currentProcess(barcode, process, name, numberTv.getText().toString(), true);
                    }*/
                    switch (currentProcess) {
                        case "montaje":
                            if (!snapshot.child("montajeStarted").exists()) {
                                currentProcess(barcode, process, name, numberTv.getText().toString(), false);
                                if (snapshot.child("cajonesEnded").exists()) {
                                    infoTv.setText("El procés cajones SÍ és present.");
                                } else {
                                    infoTv.setText("El procés cajones NO és present.");
                                }
                                infoTv.setVisibility(View.VISIBLE);
                            } else if (snapshot.child("montajeStarted").exists()) {
                                infoTv.setVisibility(View.GONE);
                                if (snapshot.child("montajeEnded").exists()) {
                                    errorTv.setText("Aquest procés montaje ja ha acabat.");
                                    errorTv.setVisibility(View.VISIBLE);
                                } else {
                                    infoTv.setText("Acabat el procés montaje: " + barcode);
                                    infoTv.setVisibility(View.VISIBLE);
                                    endProcess(process, barcode, false);
                                }
                            }
                            break;
                        case "cajones":
                            if (!snapshot.child("cajonesStarted").exists()) {
                                currentProcess(barcode, process, name, numberTv.getText().toString(), true);
                            } else {
                                errorTv.setText("El procés cajones ja ha començat.");
                                errorTv.setVisibility(View.VISIBLE);
                            }
                            break;
                        case "embalaje":
                            if (!snapshot.child("embalajeStarted").exists() && snapshot.child("montajeEnded").exists()) {
                                currentProcess(barcode, process, name, numberTv.getText().toString(), true);
                            } else if (snapshot.child("embalajeStarted").exists()) {
                                errorTv.setText("El procés embalaje ja ha començat.");
                                errorTv.setVisibility(View.VISIBLE);
                            } else {
                                errorTv.setText("El procés anterior montaje no s'ha complert.");
                                errorTv.setVisibility(View.VISIBLE);
                            }
                            break;
                        default:
                            errorTv.setText("Invalid process.");
                            errorTv.setVisibility(View.VISIBLE);
                            break;
                    }

                }




                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        } else {
            Toast.makeText(AccountActivity.this, "Ups! No has escanejat res", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}