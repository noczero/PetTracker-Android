package com.zero.pettracker.ui.outdoor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.zero.pettracker.R;
import com.zero.pettracker.ui.indoor.IndoorViewModel;

public class OutdoorFragment extends Fragment {

    private OutdoorViewModel outdoorViewModel;

    // onCreateView
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        outdoorViewModel = ViewModelProviders.of(this).get(OutdoorViewModel.class);

        View root = inflater.inflate(R.layout.fragment_outdoor, container, false);

        final TextView textView = root.findViewById(R.id.text_dashboard);
        outdoorViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

        return root;
    }
}
