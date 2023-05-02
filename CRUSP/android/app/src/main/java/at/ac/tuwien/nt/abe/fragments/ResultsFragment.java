package at.ac.tuwien.nt.abe.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.SavedStateViewModelFactory;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import at.ac.tuwien.nt.abe.R;
import at.ac.tuwien.nt.abe.adapters.ResultsAdapter;
import at.ac.tuwien.nt.abe.viewmodels.MeasurementResultsViewModel;

public class ResultsFragment extends Fragment {
    private MeasurementResultsViewModel resultsVM;

    public ResultsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SavedStateViewModelFactory vmFactory = new SavedStateViewModelFactory(this.requireActivity().getApplication(), this);

        // get Viewmodels for results and telephony-info
        resultsVM = new ViewModelProvider(this, vmFactory).get(MeasurementResultsViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_results, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.results);
        ResultsAdapter adapter = new ResultsAdapter();
        recyclerView.setAdapter(adapter);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);

        // Get a new or existing ViewModel from the ViewModelProvider.
        resultsVM.getAllResults().observe(getViewLifecycleOwner(), adapter::setResultList);

        return view;
    }
}
