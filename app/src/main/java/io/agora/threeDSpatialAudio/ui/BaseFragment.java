package io.agora.threeDSpatialAudio.ui;

import androidx.fragment.app.Fragment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseFragment extends Fragment {
    private final static Logger log = LoggerFactory.getLogger(BaseFragment.class);

    protected BaseActivity activity() {
       return  ((BaseActivity) getActivity());
    }
    protected long displayUID(int i){
        return i & 0xFFFFFFFFL;
    }
}
