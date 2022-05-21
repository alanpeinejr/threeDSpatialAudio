package io.agora.threeDSpatialAudio.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import io.agora.threeDSpatialAudio.R;
import io.agora.threeDSpatialAudio.databinding.FragmentLocalKitBinding;

public class LocalKitFragment extends BaseFragment {

    private FragmentLocalKitBinding binding;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentLocalKitBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.buttonSecond.setOnClickListener(viewListener -> NavHostFragment.findNavController(LocalKitFragment.this)
                .navigate(R.id.HomeFragment_to_KitActionsFragment));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}