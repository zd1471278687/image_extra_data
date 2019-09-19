package com.zd.imageextradata.model;

import java.util.LinkedList;
import java.util.List;

/**
 * Create by zhangdong 2019/9/19
 */
public class Png {
    private PngHeader pngHeader;

    /**
     * 数据块集合
     */
    private List<DataBlock> dataBlocks;

    public Png() {
        dataBlocks = new LinkedList<DataBlock>();
    }


    public PngHeader getPngHeader() {
        return pngHeader;
    }

    public void setPngHeader(PngHeader pngHeader) {
        this.pngHeader = pngHeader;
    }

    public List<DataBlock> getDataBlocks() {
        return dataBlocks;
    }

    public void setDataBlocks(List<DataBlock> dataBlocks) {
        this.dataBlocks = dataBlocks;
    }
}
