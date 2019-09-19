package com.zd.imageextradata.model;

import com.zd.imageextradata.util.ByteUtil;

/**
 * Create by zhangdong 2019/9/19
 */
public class SRGBBlock extends DataBlock{

    /**
     * Rendering intent,1个字节
     */
    private byte[] renderingIntent;

    public SRGBBlock() {
        super();
        renderingIntent = new byte[1];
    }

    public byte[] getRenderingIntent() {
        return renderingIntent;
    }
    public void setRenderingIntent(byte[] renderingIntent) {
        this.renderingIntent = renderingIntent;
    }

    @Override
    public void setData(byte[] data) {
        int pos = 0;
        this.renderingIntent = ByteUtil.cutByte(data, pos, this.renderingIntent.length);
        pos += this.renderingIntent.length;

        this.data = data;
    }

}
