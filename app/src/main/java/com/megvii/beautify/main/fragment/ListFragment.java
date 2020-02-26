package com.megvii.beautify.main.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.megvii.beautify.R;
import com.megvii.beautify.component.DownLoaderManager;
import com.megvii.beautify.main.fragment.viewholder.ListItemViewHolder;
import com.megvii.beautify.model.Model;
import com.megvii.beautify.model.ModelData;
import com.megvii.beautify.model.BeautyDownEvent;
import com.megvii.beautify.util.Util;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import uk.co.ribot.easyadapter.EasyRecyclerAdapter;

/**
 * Created by liyanshun on 2017/7/10.
 */

public class ListFragment extends BaseFragment implements ListPresenter.IView {
    public static String ARGUMENTS = "ARGUMENTS";

    private int mCurrentType;
    @BindView(R.id.recyclerview)
    RecyclerView mRecyclerView;

    ListPresenter mPresenter;
    EasyRecyclerAdapter<Model> mAdapter;

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
        mCurrentType = args.getInt(ARGUMENTS, Util.TYPE_STICKER);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.list_fragment, null);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        EventBus.getDefault().register(this);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRecyclerView.setLayoutManager(linearLayoutManager);

        initAdapter();

        int resTitle;
        switch (mCurrentType) {
            case Util.TYPE_STICKER:
                resTitle = R.string.sticker;
                break;
            case Util.TYPE_FILTER:
                resTitle = R.string.filter;
                break;
            default:
                resTitle = R.string.cut_image;
        }

        title.setText(resTitle);
        mPresenter = new ListPresenter(this, mCurrentType);
        mPresenter.requestData();
    }

    private void initAdapter() {
        ListItemViewHolder.ItemListener listener = new ListItemViewHolder.ItemListener() {

            @Override
            public void onItemClicked(int position) {
                switch (mCurrentType) {
                    case Util.TYPE_STICKER:
                        if (position==0||(DownLoaderManager.STATE_NONE != mAdapter.getItems().get(position).status&&DownLoaderManager.STATE_DOWNLOADING != mAdapter.getItems().get(position).status)){
                            Model.stickerPosition = position;
                        }

                        mPresenter.handleStickerClick(position);
                        break;
                    case Util.TYPE_FILTER:
                        if (position==0||(DownLoaderManager.STATE_NONE != mAdapter.getItems().get(position).status&&DownLoaderManager.STATE_DOWNLOADING != mAdapter.getItems().get(position).status)){
                            Model.filterPosition = position;
                        }

                        mPresenter.handleFilterClick(position);
                        if(callback != null) {
                            callback.onListFragmentChanged();
                        }
                        break;
                }
                mAdapter.notifyDataSetChanged();
            }
        };
        mAdapter = new EasyRecyclerAdapter<>(getActivity(), ListItemViewHolder.class, listener);
        mRecyclerView.setAdapter(mAdapter);

    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onBeautyDownEvent(BeautyDownEvent event) {
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }


    @Override
    public void showLoading() {

    }

    @Override
    public void hideLoading() {

    }

    @Override
    public void onUpdateList(ModelData data) {
        switch (mCurrentType) {
            case Util.TYPE_STICKER:
                mAdapter.addItems(data.modelList);
                break;
            case Util.TYPE_FILTER:
                mAdapter.addItems(data.modelList);
                break;
            default:
                mAdapter.addItems(data.modelList);
        }
    }

    @Override
    public void onDestroy() {
        mPresenter.onDestory();
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }
}
