package com.zd.imageextradata.model;

import com.zd.imageextradata.util.ByteUtil;

/**
 * Create by zhangdong 2019/9/19
 */
public class PLTEBlock extends DataBlock {

    /**
     * 调色板信息，每一个调色板信息由3个字节构成，分别为RGB
     * 总的信息数位1-256个
     */
    private byte[][] palettes;

    public byte[][] getPalettes() {
        return palettes;
    }

    public void setPalettes(byte[][] palettes) {
        this.palettes = palettes;
    }

    @Override
    public void setData(byte[] data) {
        int length = ByteUtil.highByteToInt(this.getLength());
        int col = 3;
        int row = length / col;
        palettes = new byte[row][col];
        for(int i = 0; i < col; i++) {
            for(int j = 0; j < row; j++) {
                palettes[j][i] = data[j * col + i];
            }
        }

        this.data = data;
    }

}
