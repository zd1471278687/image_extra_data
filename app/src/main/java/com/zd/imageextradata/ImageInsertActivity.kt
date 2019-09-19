package com.zd.imageextradata

import android.os.Bundle
import android.support.media.ExifInterface
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import com.zd.imageextradata.util.DialogUtil
import com.zd.imageextradata.util.FileUtil
import com.zd.imageextradata.util.PngUtil
import kotlinx.android.synthetic.main.activity_image_insert.*
import java.io.File
import java.io.IOException

/**
 * insert extra data
 * Create by zhangdong 2019/9/19
 */
class ImageInsertActivity : AppCompatActivity() {
    private var imageFormat = FORMAT_JPG
    private var savePath = FileUtil.getRootPath() + File.separator + "imageExtraData"
    private var fileName = JPG_FILE_NAME
    private var saveFileName = JPG_EXTRA_FILE_NAME

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_insert)
        initData()
    }

    private fun initData() {
        rg_format_choice?.setOnCheckedChangeListener { _, id ->
            when(id) {
                R.id.radio_jpg -> {
                    imageFormat = FORMAT_JPG
                    fileName = JPG_FILE_NAME
                    saveFileName = JPG_EXTRA_FILE_NAME
                }
                R.id.radio_png -> {
                    imageFormat = FORMAT_PNG
                    fileName = PNG_FILE_NAME
                    saveFileName = PNG_EXTRA_FILE_NAME
                }
            }
        }
        radio_png?.text = "PNG(不起作用-。-)"
        btn_write?.setOnClickListener {
            val info = et_extra_data?.text?.toString() ?: ""
            if (TextUtils.isEmpty(info)) {
                DialogUtil.showShortPromptToast(this, R.string.tip_extra_data_empty)
                return@setOnClickListener
            }
            if (FORMAT_JPG == imageFormat) {
                addExtraDataToJPGPicture(info)
            } else {
                addExtraDataToPNGPicture(info)
            }
        }
        btn_read?.setOnClickListener {
            if (FORMAT_JPG == imageFormat) {
                readExtraDataFromJPGPicture(savePath + File.separator + saveFileName)
            } else {
                readExtraDataFromPNGPicture(savePath + File.separator + saveFileName)
            }
        }
    }

    /**
     * 给JPG图片添加信息
     *
     * @param info 存入的信息
     */
    private fun addExtraDataToJPGPicture(info: String?) {
        //从assets目的读取图片并保存至sdCard
        val saveFile = FileUtil.getAssetsFile(this, fileName,
            savePath + File.separator, saveFileName) ?: return
        Log.i(TAG, saveFile.absolutePath)
        try {
            val extraInfo = EXTRA_PREFIX + info
            //保存信息（不能自定义属性）
            val mExifInterface = ExifInterface(saveFile.absolutePath)
            mExifInterface.setAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION, extraInfo)
            mExifInterface.setAttribute(ExifInterface.TAG_USER_COMMENT, extraInfo)
            mExifInterface.saveAttributes() // 这个地方一定要调用保存。否则保存不了.
            tv_save_data_result?.text = getString(R.string.save_extra_data, mExifInterface.getAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION))
        } catch (e: IOException) {
            Log.i(TAG, e.toString())
        }
    }

    /**
     * 获取JPG图片保存的信息
     *
     * @param imagePath 保存图片的路径
     */
    private fun readExtraDataFromJPGPicture(imagePath: String) {
        val file = File(imagePath)
        if (!file.exists()) {
            return
        }
        val fileBitmap = FileUtil.getFileBitmap(file) ?: return
        iv_image?.setImageBitmap(fileBitmap)
        try {
            Log.i(TAG, file.absolutePath)
            val mExifInterface = ExifInterface(file.absolutePath)
            val imageDescription = mExifInterface.getAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION)
            val userComment = mExifInterface.getAttribute(ExifInterface.TAG_USER_COMMENT)
            tv_read_data_result?.text = getString(R.string.read_extra_data, "$imageDescription-$userComment")
        } catch (e: IOException) {
            Log.i(TAG, e.toString())
        }
    }

    /**
     * 给PNG图片添加信息
     *
     * @param info 存入的信息
     */
    private fun addExtraDataToPNGPicture(info: String?) {
        //从assets目的读取图片并保存至sdCard
        val saveFile = FileUtil.getAssetsFile(this, fileName,
            savePath + File.separator, PNG_TEMP_FILE_NAME) ?: return
        Log.i(TAG, saveFile.absolutePath)
        //todo no use
        val extraInfo = EXTRA_PREFIX + info
        PngUtil.writeFileToPng(saveFile.absolutePath, extraInfo,
            savePath + File.separator + saveFileName)
        tv_save_data_result?.text = getString(R.string.save_extra_data, extraInfo)
    }

    /**
     * 获取PNG图片保存的信息
     *
     * @param imagePath 保存图片的路径
     */
    private fun readExtraDataFromPNGPicture(imagePath: String) {
        val file = File(imagePath)
        if (!file.exists()) {
            return
        }
        val fileBitmap = FileUtil.getFileBitmap(file) ?: return
        iv_image?.setImageBitmap(fileBitmap)
        //todo no use
        val extraString = PngUtil.readTextFromPng(savePath + File.separator + saveFileName)
        tv_read_data_result?.text = getString(R.string.read_extra_data, extraString)
    }

    companion object {
        private val TAG = ImageInsertActivity::class.java.simpleName ?: ""
        private const val FORMAT_JPG = "jpg"
        private const val FORMAT_PNG = "png"
        private const val JPG_FILE_NAME = "test_jpg.jpg"
        private const val PNG_FILE_NAME = "test_png.png"
        private const val PNG_TEMP_FILE_NAME = "test_insert_temp_png.png"
        private const val JPG_EXTRA_FILE_NAME = "test_insert_jpg_extra.jpg"
        private const val PNG_EXTRA_FILE_NAME = "test_insert_png_extra.png"
        private const val EXTRA_PREFIX = "zd_"
    }
}
