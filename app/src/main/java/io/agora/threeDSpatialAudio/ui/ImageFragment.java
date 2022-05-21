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
import io.agora.threeDSpatialAudio.databinding.FragmentImageBinding;
import io.agora.threeDSpatialAudio.model.AGEventHandler;
import io.agora.threeDSpatialAudio.model.pojos.HeadArticulation;

public class ImageFragment extends BaseFragment implements AGEventHandler, AdapterView.OnItemSelectedListener {
    private final static Logger log = LoggerFactory.getLogger(ImageFragment.class);

    private FragmentImageBinding binding;
    private String channelName;
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentImageBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle arguments = this.getArguments();
        if (arguments != null) {
            channelName = arguments.getString("channelName");
        }
        binding.imageChannelName.setText(channelName);
        activity().event().addEventHandler(this);
        activity().worker().joinChannel(channelName, activity().config().mUid);

        binding.imageHeadMotionSwitch.setOnCheckedChangeListener((compoundButton, isHeadMotionChecked) -> {
            if(isHeadMotionChecked){
                //TODO set postion/motion with HeadArticulation.byIndex(binding.imageHeadMotionSpinner.getSelectedItemPosition()
                //probably something here between using the gyro vs spinner
            }else{
               //TODO turn off
            }
        });
        setupHeadMotionSpinner(binding.imageHeadMotionSpinner);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        activity().event().removeEventHandler(this);
        activity().worker().leaveChannel(channelName);
        binding = null;
    }

    @Override
    public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
        log.debug("onJoinChannelSuccess {} {} {}", channel, displayUID(uid), elapsed);
        activity().runOnUiThread(() -> {
            if (activity().isFinishing()) {
                return;
            }
            //TODO add the user's id if I skim down Head Motion
            //TODO set a position for our audience member
        });
    }

    @Override
    public void onUserJoined(int uid, int elapsed) {
        log.debug("onUserJoin {} {}",uid, elapsed);
        //TODO set an initial position for our host
    }

    @Override
    public void onUserOffline(int uid, int reason) {
        log.debug("onUserOffline {} {}",uid, reason);
        String stringifyID = "" + displayUID(uid);
        //TODO remove them from their seat if assaigned

    }

    @Override
    public void onExtraCallback(int type, Object... data) {
        //TODO
    }
    private void setupHeadMotionSpinner(Spinner spinner){
        ArrayAdapter<HeadArticulation> adapter = new ArrayAdapter<>(this.getContext(), android.R.layout.simple_spinner_item, HeadArticulation.values());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        if(binding.imageHeadMotionSwitch.isChecked()){
            //TODO change head position w/ HeadArticulation.byIndex(i);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        //TODO Not implemented? no change?
    }
}
