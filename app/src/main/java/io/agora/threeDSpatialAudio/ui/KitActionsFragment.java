package io.agora.threeDSpatialAudio.ui;

import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.navigation.fragment.NavHostFragment;

import io.agora.threeDSpatialAudio.R;
import io.agora.threeDSpatialAudio.databinding.FragmentKitActionsBinding;

public class KitActionsFragment extends BaseFragment {

    private FragmentKitActionsBinding binding;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentKitActionsBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.kitJoinAsHostButton.setOnClickListener(view1 -> {
            Editable channelName = binding.kitActionsChannelEditText.getText();
            if(channelName != null && channelName.length() > 0){
                Bundle bundle = new Bundle();
                bundle.putString("channelName", channelName.toString());
                NavHostFragment.findNavController(KitActionsFragment.this)
                        .navigate(R.id.action_kitActionsFragment_to_hostFragment, bundle);
            }else{
                activity().showLongToast(getString(R.string.no_channel_name_warning));
            }
        });
        binding.kitJoin9SeatButton.setOnClickListener((View view1)->{
            Editable channelName = binding.kitActionsChannelEditText.getText();
            if(channelName != null && channelName.length() > 0){
                Bundle bundle = new Bundle();
                bundle.putString("channelName", channelName.toString());
                NavHostFragment.findNavController(KitActionsFragment.this)
                        .navigate(R.id.action_kitActionsFragment_to_nineSeatsFragment, bundle);
            }else{
                activity().showLongToast(getString(R.string.no_channel_name_warning));
            }
        });
        binding.kitJoinImageButton.setOnClickListener((View view1)->{
            Editable channelName = binding.kitActionsChannelEditText.getText();
            if(channelName != null && channelName.length() > 0){
                Bundle bundle = new Bundle();
                bundle.putString("channelName", channelName.toString());
                NavHostFragment.findNavController(KitActionsFragment.this)
                        .navigate(R.id.action_kitActionsFragment_to_imageFragment, bundle);
            }else{
                activity().showLongToast(getString(R.string.no_channel_name_warning));
            }
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}