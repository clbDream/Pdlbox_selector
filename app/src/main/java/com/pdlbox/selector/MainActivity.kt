package com.pdlbox.selector

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.pdlbox.selector.activity.ImageSelectActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun chooseImg(view: View) {
        ImageSelectActivity.start(this@MainActivity, object : ImageSelectActivity.OnPhotoSelectListener {

            override fun onSelected(data: MutableList<String>) {
                Toast.makeText(this@MainActivity, "选择了$data", Toast.LENGTH_SHORT).show()
            }

            override fun onCancel() {
                Toast.makeText(this@MainActivity, "取消了", Toast.LENGTH_SHORT).show()
            }
        })
    }
}