package com.zd.imageextradata.factory;

import android.util.Log;
import com.zd.imageextradata.model.*;
import com.zd.imageextradata.util.BlockUtil;
import com.zd.imageextradata.util.ByteUtil;

import java.io.IOException;
import java.io.InputStream;

/**
 * Create by zhangdong 2019/9/19
 */
public class BlockFactory {
    public static DataBlock readBlock(InputStream in, Png png, DataBlock dataBlock) throws IOException {
        String hexCode = ByteUtil.byteToHex(dataBlock.getChunkTypeCode(),
                0, dataBlock.getChunkTypeCode().length);
        hexCode = hexCode.toUpperCase();
        Log.e("BlockFactory", "hexCode = " + hexCode);
        DataBlock realDataBlock = null;
        if(BlockUtil.isIHDR(hexCode)) {
            //IHDR数据块
            realDataBlock = new IHDRBlock();
        } else if(BlockUtil.isPLTE(hexCode)) {
            //PLTE数据块
            realDataBlock = new PLTEBlock();
        } else if(BlockUtil.isIDAT(hexCode)) {
            //IDAT数据块
            realDataBlock = new IDATBlock();
        } else if(BlockUtil.isIEND(hexCode)) {
            //IEND数据块
            realDataBlock = new IENDBlock();
        } else if(BlockUtil.isSRGB(hexCode)) {
            //sRGB数据块
            realDataBlock = new SRGBBlock();
        } else if(BlockUtil.istEXt(hexCode)) {
            //tEXt数据块
            realDataBlock = new TEXTBlock();
        } else if(BlockUtil.isPHYS(hexCode)) {
            //pHYs数据块
            realDataBlock = new PHYSBlock();
        } else if(BlockUtil.istRNS(hexCode)) {
            //tRNS数据块
            realDataBlock = new TRNSBlock();
        } else {
            //其它数据块
            realDataBlock = dataBlock;
        }
        realDataBlock.setLength(dataBlock.getLength());
        realDataBlock.setChunkTypeCode(dataBlock.getChunkTypeCode());
        //读取数据,这里的测试版做法是： 把所有数据读取进内存来
        int len = -1;
        //大于图片大小
        byte[] data = new byte[204800];
        len = in.read(data, 0, ByteUtil.highByteToInt(dataBlock.getLength()));
        realDataBlock.setData(ByteUtil.cutByte(data, 0, len));
        return realDataBlock;
    }
}
