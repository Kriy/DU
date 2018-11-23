package com.example.weiyue.ui.video

import `in`.srain.cube.views.ptr.PtrDefaultHandler
import `in`.srain.cube.views.ptr.PtrFrameLayout
import `in`.srain.cube.views.ptr.PtrHandler
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.chad.library.adapter.base.BaseQuickAdapter
import com.example.weiyue.R
import com.example.weiyue.bean.VideoChannelBean
import com.example.weiyue.bean.VideoDetailBean
import com.example.weiyue.component.ApplicationComponent
import com.example.weiyue.component.DaggerHttpComponent
import com.example.weiyue.ui.adapter.VideoDetailAdapter
import com.example.weiyue.ui.base.BaseFragment
import com.example.weiyue.ui.video.contract.VideoContract
import com.example.weiyue.ui.video.presenter.VideoPresenter
import com.example.weiyue.widget.CustomLoadMoreView
import com.example.weiyue.widget.SimpleMultiStateView
import kotlinx.android.synthetic.main.fragment_detail.*

class DetailFragment : BaseFragment<VideoPresenter>(), VideoContract.View {

    private var videoDetailBean: VideoDetailBean? = null
    private lateinit var detailAdapter: VideoDetailAdapter
    private var pageNum = 1
    private var typeId: String? = null

    companion object {
        fun newInstance(typeId: String): DetailFragment {
            val args = Bundle()
            args.putCharSequence("typeId", typeId)
            val fragment = DetailFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun getContentLayout(): Int = R.layout.fragment_detail

    override fun getSimpleMultiStateView(): SimpleMultiStateView? = mSimpleMultiStateView

    override fun initInjector(appComponent: ApplicationComponent) {
        DaggerHttpComponent.builder()
                .applicationComponent(appComponent)
                .build()
                .inject(this)
    }

    override fun bindView(view: View, savedInstanceState: Bundle?) {
        mPtrFrameLayout.disableWhenHorizontalMove(true)
        mPtrFrameLayout.setPtrHandler(object : PtrHandler {
            override fun checkCanDoRefresh(frame: PtrFrameLayout?, content: View?, header: View?): Boolean {
                return PtrDefaultHandler.checkContentCanBePulledDown(frame, mRecyclerView, header)
            }

            override fun onRefreshBegin(frame: PtrFrameLayout?) {
                pageNum = 1
                mPresenter?.getVideoDetails(pageNum, "list", typeId!!)
            }
        })
        videoDetailBean = VideoDetailBean()
        mRecyclerView.layoutManager = LinearLayoutManager(activity)
        detailAdapter = VideoDetailAdapter(activity, R.layout.item_detail_video, videoDetailBean?.item)
        mRecyclerView.adapter = detailAdapter
        detailAdapter.setEnableLoadMore(true)
        detailAdapter.setLoadMoreView(CustomLoadMoreView())
        detailAdapter.openLoadAnimation(BaseQuickAdapter.ALPHAIN)
        detailAdapter.setOnLoadMoreListener({ mPresenter?.getVideoDetails(pageNum, "list", typeId!!) }, mRecyclerView)
    }

    override fun initData() {
        when (arguments) {
            null -> return
            else -> {
                typeId = arguments.getString("typeId")
                mPresenter?.getVideoDetails(pageNum, "list", typeId!!)
            }
        }
    }

    override fun loadVideoChannel(channelBean: List<VideoChannelBean>?) {
    }

    override fun loadVideoDetails(detailBean: List<VideoDetailBean>?) {
        when (detailBean) {
            null -> {
                showError()
                return
            }
            else -> {
                pageNum++
                detailAdapter.setNewData(detailBean[0].item)
                mPtrFrameLayout.refreshComplete()
                showSuccess()
            }
        }
    }

    override fun loadMoreVideoDetails(detailBean: List<VideoDetailBean>?) {
        when (detailBean) {
            null -> {
                detailAdapter.loadMoreEnd()
                return
            }
            else -> {
                pageNum++
                detailBean[0].item?.let {
                    detailAdapter.addData(it)
                    detailAdapter.loadMoreComplete()
                }
            }
        }
    }

}