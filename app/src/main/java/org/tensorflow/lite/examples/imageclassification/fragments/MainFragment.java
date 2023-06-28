package org.tensorflow.lite.examples.imageclassification.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import org.tensorflow.lite.examples.imageclassification.R;
import org.tensorflow.lite.examples.imageclassification.databinding.FragmentMainBinding;

public class MainFragment extends Fragment {
    private FragmentMainBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMainBinding
                .inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.keKamera.setOnClickListener(v->navigateToCamera());
        binding.keGaleri.setOnClickListener(v->navigateToGaleri());
    }

    private void navigateToCamera(){
        //first go to Permission if not permission
        Navigation.findNavController(requireActivity(), R.id.fragment_container)
                .navigate(MainFragmentDirections.actionMainFragmentToPermissionsFragment());
    }

    private void navigateToGaleri(){
        //first go to Permission if not permission
        Navigation.findNavController(requireActivity(), R.id.fragment_container)
                .navigate(MainFragmentDirections.actionMainFragmentToPermissionsGaleriFragment());
    }
}
