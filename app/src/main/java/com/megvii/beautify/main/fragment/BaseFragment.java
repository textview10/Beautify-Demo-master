package com.megvii.beautify.main.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import com.megvii.beautify.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by liyanshun on 2017/6/28.
 */

public class BaseFragment extends Fragment {

    @BindView(R.id.title_text)
    TextView title;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
    }

    protected FragmentCallBack callback = null;

    public void setCallBack(FragmentCallBack cb)
    {
        callback = cb;
    }

}
