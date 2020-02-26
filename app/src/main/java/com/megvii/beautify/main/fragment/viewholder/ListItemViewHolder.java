package com.megvii.beautify.main.fragment.viewholder;


import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.megvii.beautify.R;
import com.megvii.beautify.component.DownLoaderManager;
import com.megvii.beautify.model.Model;
import com.megvii.beautify.util.Util;

import java.util.Locale;

import uk.co.ribot.easyadapter.ItemViewHolder;
import uk.co.ribot.easyadapter.PositionInfo;
import uk.co.ribot.easyadapter.annotations.LayoutId;
import uk.co.ribot.easyadapter.annotations.ViewId;

/**
 * Created by liyanshun on 2017/7/11.
 */
@LayoutId(R.layout.list_item)
public class ListItemViewHolder extends ItemViewHolder<Model> {
    @ViewId(R.id.item_image)
    ImageView imageView;

    @ViewId(R.id.item_text)
    TextView textView;

    @ViewId(R.id.item_status)
    TextView tvItemStatus;

    private int position;


    public ListItemViewHolder(View view) {
        super(view);
    }

    @Override
    public void onSetValues(Model item, PositionInfo positionInfo) {
        position = positionInfo.getPosition();
        if (positionInfo.isFirst()) {
            imageView.setBackgroundColor(getContext().getResources().getColor(android.R.color.transparent));
            tvItemStatus.setVisibility(View.GONE);
        } else {
            int position=0;
            switch (item.type) {
                case Util.TYPE_STICKER:
                    position = Model.stickerPosition;
                    setDownStatus(item);
                    break;
                case Util.TYPE_FILTER:
                    position = Model.filterPosition;
                    setDownStatus(item);
                    break;

            }
            if (positionInfo.getPosition() == position) {
                if (DownLoaderManager.STATE_NONE != item.status&&DownLoaderManager.STATE_DOWNLOADING != item.status){
                    imageView.setBackgroundResource(R.drawable.filter_select);
                }
            } else {
                imageView.setBackgroundResource(R.drawable.filter_normal);
            }
        }
        imageView.setImageResource(item.imageId);
        String locale = Locale.getDefault().getLanguage();
        switch(locale){
            case "zh":
                textView.setText(item.titleChinese);
                break;
            case "en":
                textView.setText(" "); //英文版本不需要标记，去除
                break;
            default:
                textView.setText(item.titleChinese);
                break;
        }


    }

    public void setDownStatus(Model item) {
        if (DownLoaderManager.STATE_NONE == item.status) {
            tvItemStatus.setVisibility(View.VISIBLE);
            tvItemStatus.setText(R.string.click_download);
        } else if (DownLoaderManager.STATE_DOWNLOADING == item.status) {
            tvItemStatus.setVisibility(View.VISIBLE);
            tvItemStatus.setText(R.string.downloading);
        } else if (DownLoaderManager.STATE_DOWNLOADED == item.status) {
            tvItemStatus.setVisibility(View.GONE);
        }
    }


    @Override
    public void onSetListeners() {
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ItemListener listener = getListener(ItemListener.class);
                if (listener != null) {
                    listener.onItemClicked(position);
                }
            }
        });
    }

    public interface ItemListener {
        void onItemClicked(int position);
    }
}
