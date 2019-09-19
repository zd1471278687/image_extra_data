package com.zd.imageextradata.model;

/**
 * Create by zhangdong 2019/9/19
 */
public class PngHeader {

    /**
     * png文件头部信息，固定,8个字节
     */
    private byte[] flag;

    public PngHeader() {
        flag = new byte[8];
    }

    public byte[] getFlag() {
        return flag;
    }
    public void setFlag(byte[] flag) {
        this.flag = flag;
    }
}
