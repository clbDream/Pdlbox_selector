package com.pdlbox.selector.activity

import android.content.ContentResolver
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.text.TextUtils
import android.view.*
import android.view.animation.*
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.HandlerCompat.postDelayed
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.pdlbox.selector.R
import com.pdlbox.selector.adapter.BaseAdapter
import com.pdlbox.selector.adapter.ImageSelectAdapter
import com.pdlbox.selector.dialog.AlbumDialog
import com.pdlbox.selector.dialog.BaseDialog
import com.pdlbox.selector.widget.GridSpaceDecoration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.*

/**
 *    author : Android 轮子哥
 *    github : https://github.com/getActivity/AndroidProject-Kotlin
 *    time   : 2019/07/24
 *    desc   : 选择图片
 */
class ImageSelectActivity : AppCompatActivity(), Runnable,
    BaseAdapter.OnItemClickListener, BaseAdapter.OnItemLongClickListener,
    BaseAdapter.OnChildClickListener {

    companion object {

        private const val INTENT_KEY_IN_MAX_SELECT: String = "maxSelect"
        private const val INTENT_KEY_OUT_IMAGE_LIST: String = "imageList"

        fun start(activity: AppCompatActivity, listener: OnPhotoSelectListener?) {
            start(activity, 1, listener)
        }

        fun start(activity: AppCompatActivity, maxSelect: Int, listener: OnPhotoSelectListener?) {
            if (maxSelect < 1) {
                // 最少要选择一个图片
                throw IllegalArgumentException("are you ok?")
            }
            val intent = Intent(activity, ImageSelectActivity::class.java)
            intent.putExtra(INTENT_KEY_IN_MAX_SELECT, maxSelect)
            activity.startActivity(intent)

//            activity.startActivityForResult(intent, object : OnActivityCallback {
//                override fun onActivityResult(resultCode: Int, data: Intent?) {
//                    if (listener == null) {
//                        return
//                    }
//                    if (data == null) {
//                        listener.onCancel()
//                        return
//                    }
//                    val list: ArrayList<String>? = data.getStringArrayListExtra(
//                        INTENT_KEY_OUT_IMAGE_LIST
//                    )
//                    if (list == null || list.isEmpty()) {
//                        listener.onCancel()
//                        return
//                    }
//                    val iterator: MutableIterator<String> = list.iterator()
//                    while (iterator.hasNext()) {
//                        if (!File(iterator.next()).isFile) {
//                            iterator.remove()
//                        }
//                    }
//                    if (resultCode == RESULT_OK && list.isNotEmpty()) {
//                        listener.onSelected(list)
//                        return
//                    }
//                    listener.onCancel()
//                }
//            })
        }
    }

    private val recyclerView: RecyclerView? by lazy { findViewById(R.id.rv_image_select_list) }
    private val floatingView: FloatingActionButton? by lazy { findViewById(R.id.fab_image_select_floating) }
    private val left_back: ImageView? by lazy { findViewById(R.id.left_back) }
    private val title_view: TextView? by lazy { findViewById(R.id.title_view) }
    private val right_view: TextView? by lazy { findViewById(R.id.right_view) }

    /** 最大选中 */
    private var maxSelect: Int = 1

    /** 选中列表 */
    private val selectImage = ArrayList<String>()

    /** 全部图片 */
    private val allImage = ArrayList<String>()

    /** 图片专辑 */
    private val allAlbum = HashMap<String, MutableList<String>>()

    /** 列表适配器 */
    private val adapter: ImageSelectAdapter = ImageSelectAdapter(this, selectImage)

    /** 专辑选择对话框 */
    private var albumDialog: AlbumDialog.Builder? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.image_select_activity)
        initView()
        initData()
    }


     private fun initView() {
         left_back?.setOnClickListener { finish() }
         right_view?.setOnClickListener {
             onRightClick()
         }

        floatingView?.setOnClickListener {
            if (selectImage.isEmpty()) {
                // TODO 点击拍照
//                    CameraActivity.start(this, object : OnCameraListener {
//
//                        override fun onSelected(file: File) {
//                            // 当前选中图片的数量必须小于最大选中数
//                            if (selectImage.size < maxSelect) {
//                                selectImage.add(file.path)
//                            }
//
//                            // 这里需要延迟刷新，否则可能会找不到拍照的图片
//                            postDelayed({
//                                // 重新加载图片列表
//                                lifecycleScope.launch { run() }
//                            }, 1000)
//                        }
//
//                        override fun onError(details: String) {
//                            toast(details)
//                        }
//                    })
                return@setOnClickListener
            }

            // 完成选择
            setResult(
                RESULT_OK,
                Intent().putStringArrayListExtra(INTENT_KEY_OUT_IMAGE_LIST, selectImage)
            )
            finish()
        }

        adapter.setOnChildClickListener(R.id.fl_image_select_check, this@ImageSelectActivity)
        adapter.setOnItemClickListener(this@ImageSelectActivity)
        adapter.setOnItemLongClickListener(this@ImageSelectActivity)

        recyclerView?.let {
            it.adapter = adapter
            // 禁用动画效果
            it.itemAnimator = null
            // 添加分割线
            it.addItemDecoration(GridSpaceDecoration(resources.getDimension(R.dimen.dp_3).toInt()))
            // 设置滚动监听
            it.addOnScrollListener(object : RecyclerView.OnScrollListener() {

                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    when (newState) {
                        RecyclerView.SCROLL_STATE_DRAGGING -> floatingView?.hide()
                        RecyclerView.SCROLL_STATE_IDLE -> floatingView?.show()
                    }
                }
            })
        }
    }

    private fun initData() {
        XXPermissions.with(this).permission(Permission.WRITE_EXTERNAL_STORAGE,Permission.READ_EXTERNAL_STORAGE)
            .request { permissions, all ->
                if (all) {
                    // 加载图片列表
                    lifecycleScope.launch(Dispatchers.IO) { run() }
                } else {
                    finish()
                }
            }

        //TODO 获取最大的选择数
//        maxSelect = getInt(INTENT_KEY_IN_MAX_SELECT, maxSelect)

    }

    private fun onRightClick() {
        if (allImage.isEmpty()) {
            return
        }
        val data: ArrayList<AlbumDialog.AlbumInfo> = ArrayList(allAlbum.size + 1)
        var count = 0
        val keys: MutableSet<String> = allAlbum.keys
        for (key: String in keys) {
            val list: MutableList<String>? = allAlbum[key]
            if (list == null || list.isEmpty()) {
                continue
            }
            count += list.size
            data.add(
                AlbumDialog.AlbumInfo(
                    list[0],
                    key,
                    String.format(getString(R.string.image_select_total), list.size),
                    adapter.getData() === list
                )
            )
        }
        data.add(0, AlbumDialog.AlbumInfo(
            allImage[0],
            getString(R.string.image_select_all),
            String.format(getString(R.string.image_select_total), count),
            adapter.getData() === allImage
        )
        )
        if (albumDialog == null) {
            albumDialog = AlbumDialog.Builder(this)
                .setListener(object : AlbumDialog.OnListener {
                    override fun onSelected(dialog: BaseDialog?, position: Int, bean: AlbumDialog.AlbumInfo) {
                        title_view?.setText(bean.getName())
                        // 滚动回第一个位置
                        recyclerView?.scrollToPosition(0)
                        if (position == 0) {
                            adapter.setData(allImage)
                        } else {
                            adapter.setData(allAlbum[bean.getName()])
                        }
                        // 执行列表动画
                        recyclerView?.layoutAnimation = AnimationUtils.loadLayoutAnimation(
                            this@ImageSelectActivity, R.anim.layout_from_right)
                        recyclerView?.scheduleLayoutAnimation()
                    }
                })
        }
        albumDialog!!.setData(data)
            .show()
    }

    override fun onRestart() {
        super.onRestart()
        val iterator: MutableIterator<String> = selectImage.iterator()
        // 遍历判断选择了的图片是否被删除了
        while (iterator.hasNext()) {
            val path: String = iterator.next()
            val file = File(path)
            if (file.isFile) {
                continue
            }
            iterator.remove()
            allImage.remove(path)
            val parentFile: File = file.parentFile ?: continue
            allAlbum[parentFile.name]?.remove(path)
            adapter.notifyDataSetChanged()
            if (selectImage.isEmpty()) {
                floatingView?.setImageResource(R.drawable.camera_ic)
            } else {
                floatingView?.setImageResource(R.drawable.succeed_ic)
            }
        }
    }

    /**
     * [BaseAdapter.OnItemClickListener]
     * @param recyclerView      RecyclerView对象
     * @param itemView          被点击的条目对象
     * @param position          被点击的条目位置
     */
    override fun onItemClick(recyclerView: RecyclerView?, itemView: View?, position: Int) {
        //TODO 预览图片待处理
//        ImagePreviewActivity.start(this@ImageSelectActivity, adapter.getData().toMutableList(), position)
    }

    /**
     * [BaseAdapter.OnItemLongClickListener]
     * @param recyclerView      RecyclerView对象
     * @param itemView          被点击的条目对象
     * @param position          被点击的条目位置
     */
    override fun onItemLongClick(recyclerView: RecyclerView?, itemView: View?, position: Int): Boolean {
        if (selectImage.size < maxSelect) {
            // 长按的时候模拟选中
            itemView?.findViewById<View?>(R.id.fl_image_select_check)?.let {
                return it.performClick()
            }
        }
        return false
    }

    /**
     * [BaseAdapter.OnChildClickListener]
     * @param recyclerView      RecyclerView对象
     * @param childView         被点击的条目子 View Id
     * @param position          被点击的条目位置
     */
    override fun onChildClick(recyclerView: RecyclerView?, childView: View?, position: Int) {
        if (childView?.id == R.id.fl_image_select_check) {
            val path = adapter.getItem(position)
            val file = File(path)
            if (!file.isFile) {
                adapter.removeItem(position)
                Toast.makeText(this, R.string.image_select_error, Toast.LENGTH_SHORT).show()
                return
            }
            if (selectImage.contains(path)) {
                selectImage.remove(path)
                if (selectImage.isEmpty()) {
                    floatingView?.setImageResource(R.drawable.camera_ic)
                }
                adapter.notifyItemChanged(position)
                return
            }
            if (maxSelect == 1 && selectImage.size == 1) {
                val data: MutableList<String> = adapter.getData()
                val index: Int = data.indexOf(selectImage.removeAt(0))
                if (index != -1) {
                    adapter.notifyItemChanged(index)
                }
                selectImage.add(path)
            } else if (selectImage.size < maxSelect) {
                selectImage.add(path)
                if (selectImage.size == 1) {
                    floatingView?.setImageResource(R.drawable.succeed_ic)
                }
            } else {
                Toast.makeText(this, String.format(getString(R.string.image_select_max_hint), maxSelect), Toast.LENGTH_SHORT).show()
            }
            adapter.notifyItemChanged(position)
        }
    }

    override fun run() {
        allAlbum.clear()
        allImage.clear()
        val contentUri: Uri = MediaStore.Files.getContentUri("external")
        val sortOrder: String = MediaStore.Files.FileColumns.DATE_MODIFIED + " DESC"
        val selection: String = "(" + MediaStore.Files.FileColumns.MEDIA_TYPE + "=?)" + " AND " + MediaStore.MediaColumns.SIZE + ">0"
        val contentResolver: ContentResolver = contentResolver
        val projections: Array<String?> = arrayOf(MediaStore.Files.FileColumns._ID, MediaStore.MediaColumns.DATA,
            MediaStore.MediaColumns.DISPLAY_NAME, MediaStore.MediaColumns.DATE_MODIFIED, MediaStore.MediaColumns.MIME_TYPE,
            MediaStore.MediaColumns.WIDTH, MediaStore.MediaColumns.HEIGHT, MediaStore.MediaColumns.SIZE)
        var cursor: Cursor? = null
        if (XXPermissions.isGranted(this, Permission.READ_EXTERNAL_STORAGE, Permission.WRITE_EXTERNAL_STORAGE)) {
            cursor = contentResolver.query(contentUri, projections, selection,
                arrayOf<String?>(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString()), sortOrder)
        }
        if (cursor != null && cursor.moveToFirst()) {
            val pathIndex: Int = cursor.getColumnIndex(MediaStore.MediaColumns.DATA)
            val mimeTypeIndex: Int = cursor.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE)
            val sizeIndex: Int = cursor.getColumnIndex(MediaStore.MediaColumns.SIZE)
            do {
                val size: Long = cursor.getLong(sizeIndex)
                // 图片大小不得小于 1 KB
                if (size < 1024) {
                    continue
                }
                val type: String = cursor.getString(mimeTypeIndex)
                val path: String = cursor.getString(pathIndex)
                if (TextUtils.isEmpty(path) || TextUtils.isEmpty(type)) {
                    continue
                }
                val file = File(path)
                if (!file.exists() || !file.isFile) {
                    continue
                }
                val parentFile: File = file.parentFile ?: continue

                // 获取目录名作为专辑名称
                val albumName: String = parentFile.name
                var data: MutableList<String>? = allAlbum[albumName]
                if (data == null) {
                    data = ArrayList()
                    allAlbum[albumName] = data
                }
                data.add(path)
                allImage.add(path)
            } while (cursor.moveToNext())
            cursor.close()
        }
        postDelayed( Handler(Looper.getMainLooper()),{
            // 滚动回第一个位置
            recyclerView?.scrollToPosition(0)
            // 设置新的列表数据
            adapter.setData(allImage)
            if (selectImage.isEmpty()) {
                floatingView?.setImageResource(R.drawable.camera_ic)
            } else {
                floatingView?.setImageResource(R.drawable.succeed_ic)
            }

            // 执行列表动画
            recyclerView?.layoutAnimation = AnimationUtils.loadLayoutAnimation(this, R.anim.layout_fall_down)
            recyclerView?.scheduleLayoutAnimation()
            if (allImage.isEmpty()) {
                // 显示空布局
//                showEmpty()
                // 设置右标题
                title_view?.setText(null)
            } else {
                // 显示加载完成
//                showComplete()
                // 设置右标题
                title_view?.setText(R.string.image_select_all)
            }
        }, this,500)
    }

    /**
     * 图片选择监听
     */
    interface OnPhotoSelectListener {

        /**
         * 选择回调
         *
         * @param data          图片列表
         */
        fun onSelected(data: MutableList<String>)

        /**
         * 取消回调
         */
        fun onCancel() {}
    }
}