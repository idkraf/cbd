package org.tensorflow.lite.examples.imageclassification.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavOptions;
import androidx.navigation.NavOptionsBuilder;
import androidx.navigation.Navigation;

import org.tensorflow.lite.examples.imageclassification.GalleryActivity;
import org.tensorflow.lite.examples.imageclassification.ImageDisplay;
import org.tensorflow.lite.examples.imageclassification.R;
import org.tensorflow.lite.examples.imageclassification.databinding.FragmentMainBinding;

import java.util.ArrayList;

public class MainFragment extends Fragment {
    private FragmentMainBinding binding;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

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
        binding.keArtikel.setOnClickListener(v->navigateToArtikel());
    }

    private void navigateToCamera(){
        //first go to Permission if not permission
        Navigation.findNavController(requireActivity(), R.id.fragment_container)
                .navigate(MainFragmentDirections.actionMainFragmentToCameraFragment(),
                        new NavOptions.Builder().setPopUpTo(R.id.main_fragment,true).build()
                );

    }

    private void navigateToGaleri(){
        Intent move = new Intent(requireActivity(), GalleryActivity.class);
        startActivity(move);
    }

    private void navigateToArtikel(){
        //first go to Permission if not permission
        Navigation.findNavController(requireActivity(), R.id.fragment_container)
                .navigate(MainFragmentDirections.actionMainFragmentToArtikelFragment(),
                        new NavOptions.Builder().setPopUpTo(R.id.main_fragment,true).build()    );
    }

}
