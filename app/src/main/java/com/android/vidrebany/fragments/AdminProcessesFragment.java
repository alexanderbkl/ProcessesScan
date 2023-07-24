package com.android.vidrebany.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.android.vidrebany.AdminProcessActivity;
import com.android.vidrebany.R;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class AdminProcessesFragment extends Fragment {

    ListView processesListView;
    String[] processesList;
    public AdminProcessesFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_processes, container, false);

        processesListView = view.findViewById(R.id.processesListView);

        processesList = getResources().getStringArray(R.array.processes_list);
        final ArrayAdapter<String> adapterProcessesList = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_list_item_1, android.R.id.text1, processesList);

        processesListView.setAdapter(adapterProcessesList);

        processesListView.setOnItemClickListener((parent, view1, position, id) -> {
            Intent intent = new Intent(getContext(), AdminProcessActivity.class);
            intent.putExtra("process", processesList[position].toLowerCase());
            requireContext().startActivity(intent);
                });

        // Inflate the layout for this fragment
        return view;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull @NotNull Menu menu, @NonNull @NotNull MenuInflater inflater) {

        super.onCreateOptionsMenu(menu, inflater);
    }


}