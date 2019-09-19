package com.zd.imageextradata

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import com.zd.imageextradata.util.DialogUtil
import com.zd.imageextradata.util.FileUtil
import kotlinx.android.synthetic.main.activity_image_append.*
import java.io.File

/**
 * append extra data
 * Create by zhangdong 2019/9/19
 */
class ImageAppendActivity : AppCompatActivity() {
    private var imageFormat = FORMAT_JPG
    private var savePath = FileUtil.getRootPath() + File.separator + "imageExtraData"
    private var fileName = JPG_FILE_NAME
    private var tempName = JPG_TEMP_FILE_NAME
    private var saveFileName = JPG_EXTRA_FILE_NAME

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_append)
        initData()
    }

    private fun initData() {
        rg_format_choice?.setOnCheckedChangeListener { _, id ->
            when (id) {
                R.id.radio_jpg -> {
                    imageFormat = FORMAT_JPG
                    fileName = JPG_FILE_NAME
                    tempName = JPG_TEMP_FILE_NAME
                    saveFileName = JPG_EXTRA_FILE_NAME
                }
                R.id.radio_png -> {
                    imageFormat = FORMAT_PNG
                    fileName = PNG_FILE_NAME
                    tempName = PNG_TEMP_FILE_NAME
                    saveFileName = PNG_EXTRA_FILE_NAME
                }
            }
        }
        btn_write?.setOnClickListener {
            val info = et_extra_data?.text?.toString() ?: ""
            if (TextUtils.isEmpty(info)) {
                DialogUtil.showShortPromptToast(this, R.string.tip_extra_data_empty)
                return@setOnClickListener
            }
            addExtraDataToPicture(info)
        }
        btn_read?.setOnClickListener {
            readExtraDataFromPicture(savePath + File.separator + saveFileName)
        }
    }

    /**
     * 给JPG图片添加信息
     *
     * @param info 存入的信息
     */
    private fun addExtraDataToPicture(info: String?) {
        //从assets目的读取图片并保存至sdCard
        val saveFile = FileUtil.getAssetsFile(
            this, fileName,
            savePath + File.separator, tempName
        ) ?: return
        val extraInfo = EXTRA_PREFIX + info
        //插入额外信息到末尾并保存为文件
        FileUtil.insertExtraDataToFile(saveFile, savePath + File.separator + saveFileName, extraInfo)
        tv_save_data_result?.text = getString(R.string.save_extra_data, extraInfo)
    }

    /**
     * 获取JPG图片保存的信息
     *
     * @param imagePath 保存图片的路径
     */
    private fun readExtraDataFromPicture(imagePath: String) {
        val file = File(imagePath)
        if (!file.exists()) {
            return
        }
        //展示有附加信息的图片
        val fileBitmap = FileUtil.getFileBitmap(file) ?: return
        iv_image?.setImageBitmap(fileBitmap)
        val byteFile = FileUtil.fileToByteArray(file)
        if (byteFile != null) {
            //转为字符串
            val fileString = String(byteFile)
            //截取文件末尾插入的数据
            val startIndex = fileString.lastIndexOf(EXTRA_PREFIX)
            if (startIndex > 0) {
                val hideMessage = fileString.subSequence(fileString.lastIndexOf(EXTRA_PREFIX), fileString.length)
                tv_read_data_result?.text = getString(R.string.read_extra_data, hideMessage)
            }
        }
    }

    companion object {
        private val TAG = ImageAppendActivity::class.java.simpleName ?: ""
        private const val FORMAT_JPG = "jpg"
        private const val FORMAT_PNG = "png"
        private const val JPG_FILE_NAME = "test_jpg.jpg"
        private const val PNG_FILE_NAME = "test_png.png"
        private const val JPG_TEMP_FILE_NAME = "test_temp_jpg.jpg"
        private const val PNG_TEMP_FILE_NAME = "test_temp_png.png"
        private const val JPG_EXTRA_FILE_NAME = "test_append_jpg_extra.jpg"
        private const val PNG_EXTRA_FILE_NAME = "test_append_png_extra.png"
        private const val EXTRA_PREFIX = "zd_"
    }
}
