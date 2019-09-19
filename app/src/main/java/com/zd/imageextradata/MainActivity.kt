package com.zd.imageextradata

import android.Manifest
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.zd.imageextradata.util.PermissionMediator
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestPermission()
        initData()
    }

    private fun initData() {
        btn_insert_data?.setOnClickListener {
            val intent = Intent(this@MainActivity, ImageInsertActivity::class.java)
            startActivity(intent)
        }
        btn_append_data?.setOnClickListener {
            val intent = Intent(this@MainActivity, ImageAppendActivity::class.java)
            startActivity(intent)
        }
    }

    private fun requestPermission() {
        //文件存取
        val permissions = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        PermissionMediator.checkPermission(this, permissions, object : PermissionMediator.DefaultPermissionRequest() {
            override fun onPermissionRequest(granted: Boolean, permission: String) {
                if (!granted) {
                    finish()
                }
            }

            override fun onPermissionRequest(
                isAllGranted: Boolean,
                permissions: Array<String>?,
                grantResults: IntArray?
            ) {
                if (!isAllGranted) {
                    finish()
                }
            }
        })
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        PermissionMediator.dispatchPermissionResult(this, requestCode, permissions, grantResults)
    }
}
