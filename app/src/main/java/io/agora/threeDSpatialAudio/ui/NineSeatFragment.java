package io.agora.threeDSpatialAudio.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import androidx.annotation.NonNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import io.agora.threeDSpatialAudio.R;
import io.agora.threeDSpatialAudio.databinding.FragmentNineSeatBinding;
import io.agora.threeDSpatialAudio.model.AGEventHandler;

public class NineSeatFragment extends BaseFragment  implements AGEventHandler {
    private final static Logger log = LoggerFactory.getLogger(NineSeatFragment.class);
    private static final int[] SEATS = {
            R.id.nine_seats_top_left_seat, R.id.nine_seats_top_middle_seat, R.id.nine_seats_top_right_seat,
            R.id.nine_seats_middle_left_seat, R.id.nine_seats_middle_middle_seat, R.id.nine_seats_middle_right_seat,
            R.id.nine_seats_bottom_left_seat, R.id.nine_seats_bottom_middle_seat, R.id.nine_seats_bottom_right_seat,
    };
    private static final int[] LABELS = {
            R.id.nine_seats_top_left_label, R.id.nine_seats_top_middle_label, R.id.nine_seats_top_right_label,
            R.id.nine_seats_middle_left_label, R.id.nine_seats_middle_middle_label, R.id.nine_seats_middle_right_label,
            R.id.nine_seats_bottom_left_label, R.id.nine_seats_bottom_middle_label, R.id.nine_seats_bottom_right_label,
    };
    private FragmentNineSeatBinding binding;
    private String channelName;
    private HashMap<String, Integer> seated;
    private ArrayList<Integer> hostList1;
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        seated = new HashMap<>();
        hostList1 = new ArrayList<>();
        binding = FragmentNineSeatBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle arguments = this.getArguments();
        if (arguments != null) {
            channelName = arguments.getString("channelName");
        }
        binding.nineSeatChannelName.setText(channelName);
        activity().event().addEventHandler(this);
        activity().worker().joinChannel(channelName, activity().config().mUid);

        for(int id : SEATS){
            View view1 = activity().findViewById(id);
            view1.setOnClickListener(getOnClickListener(view1));
        }
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
            BaseActivity activity = activity();
            String userString = getString(R.string.user_id) + displayUID(activity.config().mUid);
            binding.nineSeatUserIdBubble.setText(userString);

        });
    }

    @Override
    public void onUserJoined(int uid, int elapsed) {
        log.debug("onUserJoin {} {}",uid, elapsed);
        if(!hostList1.contains(uid)){
            hostList1.add(uid);
        }
        //TODO set an initial position for our host
    }

    @Override
    public void onUserOffline(int uid, int reason) {
        log.debug("onUserOffline {} {}",uid, reason);
        String stringifyID = "" + displayUID(uid);
        setLabelFromLabel(stringifyID, "-");
        //TODO remove them from their seat if assaigned
        removeVideoFromSeat(seated.get(stringifyID));
        seated.remove(stringifyID);
        if(hostList1.contains(uid)){
            hostList1.remove(hostList1.indexOf(uid));
        }

    }

    @Override
    public void onExtraCallback(int type, Object... data) {
        //TODO
    }

    private View.OnClickListener getOnClickListener(View view){
        return (v) -> getAlertDialog(v).show();

    }

    private AlertDialog getAlertDialog(View view){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setTitle(R.string.seat_select_alert_title);
        if(hostList1.isEmpty()){
            dialogBuilder.setMessage(R.string.seat_empty);
        }else{
            String[] options = new String[hostList1.size()];
            for(int i = 0; i < options.length; i++){
                options[i] = String.valueOf(displayUID(hostList1.get(i)));
            }
            dialogBuilder.setItems(options, (dialogInterface, i) -> {
                if(seated.containsKey(options[i])){
                    activity().showLongToast("Please Evict from prior position before reseating");
                }else {
                    seated.put(options[i], view.getId());
                    setLabelFromSeat(view, options[i]);
                    view.setClipToOutline(true);
                    activity().worker().startRemoteVideo(hostList1.get(i), view.getId(), getActivity());
                }
            });
        }
        dialogBuilder.setCancelable(true);
        if(seated.containsValue(view.getId())) {
            dialogBuilder.setPositiveButton(R.string.seat_remove_from_seat, (dialogInterface, i) -> {
                setLabelFromSeat(view, "-");
                removeKeyByValue(view.getId());
                activity().worker().endRemoteVideo(view.getId(), getActivity());
                //TODO REMOVE FROM POSITION
            });
        }
        dialogBuilder.setNegativeButton(R.string.seat_cancel_click, (dialogInterface, i) -> {
            //do nothing
        });
        return dialogBuilder.create();
    }

    private void setLabelFromSeat(View seat, String label){
        int seatId = seat.getId();
        for (int i =0; i<SEATS.length; i++) {
            if (seatId == SEATS[i]) {
                TextView labelView = (TextView) activity().findViewById(LABELS[i]);
                labelView.setText(label);
            }
        }
    }
    private void setLabelFromLabel(String currentLabel, String newLabel){
        for(int j : LABELS){
            TextView labelView = ((TextView) activity().findViewById(j));
            if(currentLabel.contentEquals(labelView.getText())){
                labelView.setText(newLabel);
            }
        }
    }

    private void removeKeyByValue(Integer value){
        Set<Map.Entry<String, Integer>> entries = seated.entrySet();
        for(Map.Entry<String, Integer> entry : entries){
            if(value.equals(entry.getValue())){
                seated.remove(entry.getKey());
            }
        }
    }
    private void removeVideoFromSeat(Integer id){
        ViewGroup viewGroup = (ViewGroup) activity().findViewById(id);
        if(viewGroup.getChildCount() > 0){
            viewGroup.removeView(viewGroup.getChildAt(0));

        }
    }
}
