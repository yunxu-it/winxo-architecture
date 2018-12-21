package cn.winxo.toolbox.module.view

import android.content.Intent
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import cn.winxo.toolbox.R
import cn.winxo.toolbox.data.Injection
import cn.winxo.toolbox.data.entity.Constant
import cn.winxo.toolbox.data.entity.local.Task
import cn.winxo.toolbox.module.adapter.TaskViewBinder
import cn.winxo.toolbox.module.contract.HomeContract
import cn.winxo.toolbox.module.presenter.HomePresenter
import cn.winxo.toolbox.util.DateUtils
import cn.winxo.toolbox.util.base.BaseMvpActivity
import cn.winxo.toolbox.util.interfaces.OnSwipeListener
import kotlinx.android.synthetic.main.activity_home.date_day
import kotlinx.android.synthetic.main.activity_home.date_month
import kotlinx.android.synthetic.main.activity_home.date_time
import kotlinx.android.synthetic.main.activity_home.date_year
import kotlinx.android.synthetic.main.activity_home.recycler_view
import kotlinx.android.synthetic.main.activity_home.swipe_refresh
import me.drakeet.multitype.MultiTypeAdapter
import java.util.Calendar

/**
 * @author lxlong
 * @date 6/29/2018
 * @desc
 */
class HomeActivity : BaseMvpActivity<HomeContract.Presenter>(), HomeContract.View, SwipeRefreshLayout.OnRefreshListener {
    private lateinit var mAdapter: MultiTypeAdapter

    override fun onLoadPresenter(): HomeContract.Presenter {
        return HomePresenter(this, Injection.provideTaskRepository(this))
    }

    override fun setLayoutResourceID(): Int {
        return R.layout.activity_home
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            val decorView = window.decorView
            decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        }
    }

    override fun initView() {
        swipe_refresh.setOnRefreshListener(this)
        recycler_view.layoutManager = LinearLayoutManager(this)
        mAdapter = MultiTypeAdapter()
        val binder = TaskViewBinder()
        binder.setOnSwipeListener(object : OnSwipeListener<Task> {
            override fun onDelete(position: Int, task: Task) {
                mPresenter.removeTask(position, task)
            }

            override fun onEdit(position: Int, task: Task) {

            }
        })
        mAdapter.register(Task::class.java, binder)
        recycler_view.adapter = mAdapter
    }

    override fun initData() {
        mPresenter.loadTask()
        val instance = Calendar.getInstance()
        date_day.text = instance.get(Calendar.DAY_OF_MONTH).toString()
        date_year.text = instance.get(Calendar.YEAR).toString()
        date_time.text = DateUtils.getTimeFormat(instance.get(Calendar.HOUR_OF_DAY), instance.get(Calendar.MINUTE))
        date_month.text = DateUtils.getChineseMonth(instance.get(Calendar.MONTH) + 1)

    }

    override fun onResume() {
        super.onResume()
        mPresenter.loadTask()
    }

    override fun addTask(task: Task) {
        val items = mAdapter.items as MutableList<Task>
        items.add(0, task)
        mAdapter.notifyItemInserted(0)
    }

    override fun removeTask(position: Int) {
        val items = mAdapter.items
        items.removeAt(position)
        mAdapter.notifyItemRangeRemoved(position, items.size)
    }

    override fun onRefresh() {
        swipe_refresh.isRefreshing = false
        val intent = Intent()
        intent.setClass(this, EditActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        intent.putExtra(Constant.ExtraKey.START_X, 0)
        intent.putExtra(Constant.ExtraKey.START_Y, 0)
        startActivity(intent)
    }

    override fun showData(tasks: List<Task>) {
        mAdapter.items = tasks
        mAdapter.notifyDataSetChanged()
    }
}
