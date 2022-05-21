package io.agora.threeDSpatialAudio.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.agora.threeDSpatialAudio.R;
import io.agora.threeDSpatialAudio.databinding.FragmentHostBinding;
import io.agora.threeDSpatialAudio.model.AGEventHandler;
import io.agora.threeDSpatialAudio.model.pojos.Sound;

public class HostFragment extends BaseFragment implements AdapterView.OnItemSelectedListener, AGEventHandler {
    private final static Logger log = LoggerFactory.getLogger(HostFragment.class);

    private FragmentHostBinding binding;
    private String channelName;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentHostBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle arguments = this.getArguments();
        if(arguments!= null){
            channelName = arguments.getString("channelName");
        }
        binding.hostChannelName.setText(channelName);
        activity().event().addEventHandler(this);

        activity().worker().hostChannel(channelName, activity().config().mUid);
        binding.hostVideoSwitch.setOnCheckedChangeListener((compoundButton, isVideoChecked) -> {
            if(isVideoChecked){
                activity().worker().startLocalVideo(binding.hostVideoContainer.getId(), activity());
            }else{
              activity().worker().endLocalVideo(binding.hostVideoContainer.getId(), activity() );
            }
        });

        binding.hostAudioMixingSwitch.setOnCheckedChangeListener((compoundButton, isAudioChecked) -> {
            if(isAudioChecked){
                activity().worker().startAudioMixing(Sound.byIndex(binding.hostAudioMixSpinner.getSelectedItemPosition()));
            }else{
                activity().worker().stopAudioMixing();
            }
        });
        setupAudioMixerSpinner(binding.hostAudioMixSpinner);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        activity().event().removeEventHandler(this);
        activity().worker().leaveChannel(channelName);
        binding = null;
    }


    private void setupAudioMixerSpinner(Spinner spinner){
        ArrayAdapter<Sound> adapter = new ArrayAdapter<Sound>(this.getContext(), android.R.layout.simple_spinner_item, Sound.values());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        if(binding.hostAudioMixingSwitch.isChecked()){
            activity().worker().startAudioMixing(Sound.byIndex(i));
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        //TODO Not implemented? no change?
    }

    @Override
    public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
        log.debug("onJoinChannelSuccess {} {} {}", channel, displayUID(uid), elapsed);
        activity().runOnUiThread(() -> {
            if (activity().isFinishing()) {
                return;
            }
            BaseActivity activity = activity();
            String userString = getString(R.string.user_id) + displayUID(activity.config().mUid);
            binding.hostUserIdBubble.setText(userString);

        });


    }

    @Override
    public void onUserJoined(int uid, int elapsed) {
        log.debug("onUserJoin {} {}",uid, elapsed);
    }

    @Override
    public void onUserOffline(int uid, int reason) {
        log.debug("onUserOffline {} {}",uid, reason);
        //TODO
    }

    @Override
    public void onExtraCallback(int type, Object... data) {
        log.debug("onExtraCallback {}", type);
        //TODO
    }
}