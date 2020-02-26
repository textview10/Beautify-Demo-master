package com.megvii.beautify.model;

import java.util.List;

/**
 * Created by liyanshun on 2017/6/28.
 */

public class ModelData {
    public static ModelData sStickData;//避免每次更新preferece
    public static ModelData sFilterData;//避免每次更新preferece
    public List<Model> modelList;
    public int index;

    public ModelData(List<Model> modelList) {
        this.modelList = modelList;
    }
}
