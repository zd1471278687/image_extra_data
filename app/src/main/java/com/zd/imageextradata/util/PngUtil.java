package com.zd.imageextradata.util;

import android.text.TextUtils;
import android.util.Log;
import com.zd.imageextradata.factory.BlockFactory;
import com.zd.imageextradata.model.CommonBlock;
import com.zd.imageextradata.model.DataBlock;
import com.zd.imageextradata.model.Png;
import com.zd.imageextradata.model.PngHeader;

import java.io.*;

/**
 * Create by zhangdong 2019/9/19
 */
public class PngUtil {
    private static final String TAG = PngUtil.class.getSimpleName();

    /**
     * 将指定的文件信息写入到png文件中，并输出到指定的文件中
     *
     * @param pngFileName   png文件名
     * @param inputFileName 要隐藏的文件名
     * @param outFileName   输出文件名
     */
    public static void writeFileToPng(String pngFileName, String inputFileName, String outFileName) {
        Png png = readPng(pngFileName);
        writeTextToPng(png, pngFileName, inputFileName, outFileName);
    }

    /**
     * 读取指定png文件的信息
     *
     * @param pngFileName
     * @return
     * @throws IOException
     */
    private static Png readPng(String pngFileName) {
        Png png = new Png();
        File pngFile = new File(pngFileName);
        InputStream pngIn = null;
        //记录输入流读取位置(字节为单位)
        long pos = 0;
        try {
            pngIn = new FileInputStream(pngFile);
            //读取头部信息
            PngHeader pngHeader = new PngHeader();
            pngIn.read(pngHeader.getFlag());
            png.setPngHeader(pngHeader);
            pos += pngHeader.getFlag().length;

            while (pos < pngFile.length()) {
                DataBlock realDataBlock = null;
                //读取数据块
                DataBlock dataBlock = new CommonBlock();
                //先读取长度，4个字节
                pngIn.read(dataBlock.getLength());
                pos += dataBlock.getLength().length;
                //再读取类型码，4个字节
                pngIn.read(dataBlock.getChunkTypeCode());
                pos += dataBlock.getChunkTypeCode().length;
                //如果有数据再读取数据
                //读取数据
                realDataBlock = BlockFactory.readBlock(pngIn, png, dataBlock);
                pos += ByteUtil.highByteToInt(dataBlock.getLength());
                //读取crc，4个字节
                pngIn.read(realDataBlock.getCrc());
                //添加读取到的数据块
                png.getDataBlocks().add(realDataBlock);
                pos += realDataBlock.getCrc().length;
                dataBlock = null;
                Log.i(TAG, "pos = " + pos + " length = " + pngFile.length());
            }
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        } finally {
            try {
                if (pngIn != null) {
                    pngIn.close();
                }
            } catch (IOException e) {
                Log.e(TAG, e.toString());
            }
        }
        return png;
    }

    /**
     * 将额外信息写入到指定png的文件中，并指定输出文件
     *
     * @param png         Png信息对象
     * @param pngFileName png文件名
     * @param extraString 额外信息
     * @param outFileName 输出文件名，内容包括png数据和要隐藏文件的信息
     */
    private static void writeTextToPng(Png png, String pngFileName, String extraString, String outFileName) {
        if (png == null || TextUtils.isEmpty(pngFileName) || TextUtils.isEmpty(extraString) || TextUtils.isEmpty(outFileName)) {
            return;
        }
        File pngFile = new File(pngFileName);
        File outFile = new File(outFileName);
        InputStream pngIn = null;
        OutputStream out = null;
        int len = -1;
        byte[] buf = new byte[1024];
        try {
            if (!outFile.exists()) {
                outFile.createNewFile();
            }
            pngIn = new FileInputStream(pngFile);
            out = new FileOutputStream(outFile);
            //获取最后一个数据块，即IEND数据块
            DataBlock iendBlock = png.getDataBlocks().get(png.getDataBlocks().size() - 1);
            //修改IEND数据块数据长度：原来的长度+要隐藏文件的长度
            long iendLength = ByteUtil.highByteToLong(iendBlock.getLength());
            iendLength += extraString.length();
            iendBlock.setLength(ByteUtil.longToHighByte(iendLength, iendBlock.getLength().length));
            //修改IEND crc信息：保存隐藏文件的大小（字节），方便后面读取png时找到文件内容的位置，并读取
            iendBlock.setCrc(ByteUtil.longToHighByte(extraString.length(), iendBlock.getCrc().length));
            //写入文件头部信息
            out.write(png.getPngHeader().getFlag());
            //写入数据块信息
            String hexCode = null;
            for (int i = 0; i < png.getDataBlocks().size(); i++) {
                DataBlock dataBlock = png.getDataBlocks().get(i);
                hexCode = ByteUtil.byteToHex(dataBlock.getChunkTypeCode(),
                        0, dataBlock.getChunkTypeCode().length);
                hexCode = hexCode.toUpperCase();
                out.write(dataBlock.getLength());
                out.write(dataBlock.getChunkTypeCode());
                //写数据块数据
                if (BlockUtil.isIEND(hexCode)) {
                    //写原来IEND数据块的数据
                    if (dataBlock.getData() != null) {
                        out.write(dataBlock.getData());
                    }
                    //如果是IEND数据块，那么将文件内容写入IEND数据块的数据中去
                    out.write(extraString.getBytes());
                } else {
                    out.write(dataBlock.getData());
                }
                out.write(dataBlock.getCrc());
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        } finally {
            try {
                if (pngIn != null) {
                    pngIn.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                Log.e(TAG, e.toString());
            }
        }
    }

    /**
     * 将读取到的文件信息写入到指定png的文件中，并指定输出文件
     *
     * @param png           Png信息对象
     * @param pngFileName   png文件名
     * @param inputFileName 要隐藏的文件名
     * @param outFileName   输出文件名，内容包括png数据和要隐藏文件的信息
     * @throws IOException
     */
    private static void wirteFileToPng(Png png, String pngFileName, String inputFileName, String outFileName) {
        File pngFile = new File(pngFileName);
        File inputFile = new File(inputFileName);
        File outFile = new File(outFileName);
        InputStream pngIn = null;
        InputStream inputIn = null;
        OutputStream out = null;
        int len = -1;
        byte[] buf = new byte[1024];
        try {
            if (!outFile.exists()) {
                outFile.createNewFile();
            }
            pngIn = new FileInputStream(pngFile);
            inputIn = new FileInputStream(inputFile);
            out = new FileOutputStream(outFile);
            //获取最后一个数据块，即IEND数据块
            DataBlock iendBlock = png.getDataBlocks().get(png.getDataBlocks().size() - 1);
            //修改IEND数据块数据长度：原来的长度+要隐藏文件的长度
            long iendLength = ByteUtil.highByteToLong(iendBlock.getLength());
            iendLength += inputFile.length();
            iendBlock.setLength(ByteUtil.longToHighByte(iendLength, iendBlock.getLength().length));
            //修改IEND crc信息：保存隐藏文件的大小（字节），方便后面读取png时找到文件内容的位置，并读取
            iendBlock.setCrc(ByteUtil.longToHighByte(inputFile.length(), iendBlock.getCrc().length));
            //写入文件头部信息
            out.write(png.getPngHeader().getFlag());
            //写入数据块信息
            String hexCode = null;
            for (int i = 0; i < png.getDataBlocks().size(); i++) {
                DataBlock dataBlock = png.getDataBlocks().get(i);
                hexCode = ByteUtil.byteToHex(dataBlock.getChunkTypeCode(),
                        0, dataBlock.getChunkTypeCode().length);
                hexCode = hexCode.toUpperCase();
                out.write(dataBlock.getLength());
                out.write(dataBlock.getChunkTypeCode());
                //写数据块数据
                if (BlockUtil.isIEND(hexCode)) {
                    //写原来IEND数据块的数据
                    if (dataBlock.getData() != null) {
                        out.write(dataBlock.getData());
                    }
                    //如果是IEND数据块，那么将文件内容写入IEND数据块的数据中去
                    len = -1;
                    while ((len = inputIn.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                } else {
                    out.write(dataBlock.getData());
                }
                out.write(dataBlock.getCrc());
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        } finally {
            try {
                if (pngIn != null) {
                    pngIn.close();
                }
                if (inputIn != null) {
                    inputIn.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                Log.e(TAG, e.toString());
            }
        }

    }

    /**
     * 读取png文件中存储的信息
     *
     * @param pngFileName png文件名
     * @return 存入的信息
     */
    public static String readTextFromPng(String pngFileName) {
        File pngFile = new File(pngFileName);
        InputStream pngIn = null;
        //记录输入流读取位置
        long pos = 0;
        int len = -1;
        byte[] buf = new byte[1024];
        try {
            pngIn = new BufferedInputStream(new FileInputStream(pngFile));
            DataBlock dataBlock = new CommonBlock();
            //获取crc的长度信息，因为不能写死，所以额外获取一下
            int crcLength = dataBlock.getCrc().length;
            byte[] fileLengthByte = new byte[crcLength];
            pngIn.mark(0);
            //定位到IEND数据块的crc信息位置，因为写入的时候我们往crc写入的是隐藏文件的大小信息
            pngIn.skip(pngFile.length() - crcLength);
            //读取crc信息
            pngIn.read(fileLengthByte);
            //获取到隐藏文件的大小（字节）
            int fileLength = ByteUtil.highByteToInt(fileLengthByte);
            //重新定位到开始部分　
            pngIn.reset();
            //定位到隐藏文件的第一个字节
            pngIn.skip(pngFile.length() - fileLength - crcLength);
            pos = pngFile.length() - fileLength - crcLength;
            Log.i(TAG, "pngFile = " + pngFile.length() + " fileLength = " + fileLength + " crcLength = " + crcLength);
            StringBuilder builder = new StringBuilder();
            //读取隐藏文件数据
            while ((len = pngIn.read(buf)) > 0) {
                if ((pos + len) > (pngFile.length() - crcLength)) {
                    builder.append(new String(buf).substring(0, (int) (pngFile.length() - crcLength - pos)));
                    break;
                } else {
                    builder.append(new String(buf).substring(0, len));
                }
                pos += len;
            }
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        } finally {
            try {
                if (pngIn != null) {
                    pngIn.close();
                }
            } catch (IOException e) {
                Log.e(TAG, e.toString());
            }
        }
        return "";
    }

    /**
     * 读取png文件中存储的信息，并写入到指定指定输出文件中
     *
     * @param pngFileName png文件名
     * @param outFileName 指定输出文件名
     * @throws IOException
     */
    public static void readFileFromPng(String pngFileName, String outFileName) {
        File pngFile = new File(pngFileName);
        File outFile = new File(outFileName);
        InputStream pngIn = null;
        OutputStream out = null;
        //记录输入流读取位置
        long pos = 0;
        int len = -1;
        byte[] buf = new byte[1024];
        try {
            if (!outFile.exists()) {
                outFile.createNewFile();
            }
            pngIn = new BufferedInputStream(new FileInputStream(pngFile));
            out = new FileOutputStream(outFile);
            DataBlock dataBlock = new CommonBlock();
            //获取crc的长度信息，因为不能写死，所以额外获取一下
            int crcLength = dataBlock.getCrc().length;
            byte[] fileLengthByte = new byte[crcLength];
            pngIn.mark(0);
            //定位到IEND数据块的crc信息位置，因为写入的时候我们往crc写入的是隐藏文件的大小信息
            pngIn.skip(pngFile.length() - crcLength);
            //读取crc信息
            pngIn.read(fileLengthByte);
            //获取到隐藏文件的大小（字节）
            int fileLength = ByteUtil.highByteToInt(fileLengthByte);
            //重新定位到开始部分　
            pngIn.reset();
            //定位到隐藏文件的第一个字节
            pngIn.skip(pngFile.length() - fileLength - crcLength);
            pos = pngFile.length() - fileLength - crcLength;
            //读取隐藏文件数据
            while ((len = pngIn.read(buf)) > 0) {
                if ((pos + len) > (pngFile.length() - crcLength)) {
                    out.write(buf, 0, (int) (pngFile.length() - crcLength - pos));
                    break;
                } else {
                    out.write(buf, 0, len);
                }
                pos += len;
            }
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        } finally {
            try {
                if (pngIn != null) {
                    pngIn.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                Log.e(TAG, e.toString());
            }
        }
    }
}
