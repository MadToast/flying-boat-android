package com.madtoast.flyingboat.ui.fragments.home

import android.graphics.Typeface
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Bundle
import android.text.TextUtils
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePaddingRelative
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.madtoast.flyingboat.R
import com.madtoast.flyingboat.api.floatplane.model.content.Post
import com.madtoast.flyingboat.api.floatplane.model.creator.Creator
import com.madtoast.flyingboat.databinding.FragmentHomeBinding
import com.madtoast.flyingboat.ui.components.adapters.BaseItem
import com.madtoast.flyingboat.ui.components.adapters.BaseViewAdapter
import com.madtoast.flyingboat.ui.components.views.HeaderItemView
import com.madtoast.flyingboat.ui.components.views.NestedRecyclerView
import com.madtoast.flyingboat.ui.components.views.PostView
import com.madtoast.flyingboat.ui.components.views.TextItemView
import com.madtoast.flyingboat.ui.fragments.home.viewmodels.HomeViewModel
import com.madtoast.flyingboat.ui.fragments.home.viewmodels.HomeViewModelFactory
import com.madtoast.flyingboat.ui.utilities.selectImageQuality
import jp.wasabeef.recyclerview.animators.SlideInLeftAnimator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*


class HomeFragment : Fragment() {

    private lateinit var _homeViewModel: HomeViewModel
    private lateinit var _binding: FragmentHomeBinding
    private var _bottomPadding = 0
    private var _startPadding = 0
    private var _viewInsetsApplied = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _homeViewModel =
            ViewModelProvider(
                this,
                HomeViewModelFactory(requireContext().cacheDir, requireContext())
            )[HomeViewModel::class.java]

        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        _binding.homeList.setOnApplyWindowInsetsListener { v, insets ->
            _viewInsetsApplied = true
            val tv = TypedValue()
            if (requireActivity().theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
                val actionBarHeight =
                    TypedValue.complexToDimensionPixelSize(tv.data, resources.displayMetrics)
                val bottomNavigationHeight =
                    resources.getDimensionPixelSize(R.dimen.fragment_padding_bottom)
                val bottomAdditionalPadding =
                    resources.getDimensionPixelSize(R.dimen.fragment_padding_bottom_additional)
                val startNavigationHeight =
                    resources.getDimensionPixelSize(R.dimen.fragment_padding_start)
                val startAdditionalPadding =
                    resources.getDimensionPixelSize(R.dimen.fragment_padding_start_additional)
                //Calculate top and bottom padding to take status bar and navigation bar into account
                val windowInsets =
                    ViewCompat.getRootWindowInsets(requireActivity().window.decorView)
                val systemBarsInsets = windowInsets?.getInsets(WindowInsetsCompat.Type.systemBars())
                systemBarsInsets?.top?.let {
                    _binding.homeList.updatePaddingRelative(top = actionBarHeight + it)
                }
                // Apply the left padding to recycler views
                var paddingStart = 0
                if (TextUtils.getLayoutDirectionFromLocale(Locale.getDefault()) == View.LAYOUT_DIRECTION_LTR) {
                    systemBarsInsets?.left?.let {
                        paddingStart = it
                    }
                }
                if (TextUtils.getLayoutDirectionFromLocale(Locale.getDefault()) != View.LAYOUT_DIRECTION_LTR) {
                    systemBarsInsets?.right?.let {
                        paddingStart = it
                    }
                }
                _startPadding = startNavigationHeight + startAdditionalPadding + paddingStart
                updateStartPadding()

                systemBarsInsets?.bottom?.let {
                    _bottomPadding = bottomNavigationHeight + bottomAdditionalPadding + it
                    updateBottomPadding()
                }
            }
            v.onApplyWindowInsets(insets)
        }

        setupRecyclerView()
        setupObservers()
        loadRequiredData()

        return _binding.root
    }

    private fun loadRequiredData() {
        showLoading()
        CoroutineScope(Dispatchers.IO).launch {
            //Get the creators
            _homeViewModel.listPlatformCreators(
                "",
                sortedBy = Creator.SubscribedComparator().reversed()
            )
        }
    }

    private fun setupObservers() {
        _homeViewModel.creatorsResult.observe(viewLifecycleOwner, Observer {
            hideLoading()
            val creatorResult = it ?: return@Observer
            if (creatorResult.success != null) {
                prepareHomeScreen(creatorResult.success)
            }
        })
    }

    private fun prepareHomeScreen(creators: Array<Creator>) {
        val homeList: RecyclerView = _binding.homeList
        val homeItemsList = ArrayList<BaseItem>()
        val homeAdapter = homeList.adapter as BaseViewAdapter

        val headerItem = TextItemView.Companion.TextItem(
            getString(R.string.welcome),
            resources.getDimension(R.dimen.welcome_text),
            Typeface.BOLD
        )
        headerItem.setItemPadding(_startPadding, 0, 0, 0)
        homeItemsList.add(
            headerItem
        )

        var hasAddedSubHeaderForSubscribed = false
        var hasAddedSubHeaderForUnsubscribed = false
        for (creator in creators) {
            if (creator.userSubscribed && !hasAddedSubHeaderForSubscribed) {
                val header = TextItemView.Companion.TextItem(
                    getString(R.string.content_for_today),
                    resources.getDimension(R.dimen.subheader_text)
                )
                header.setItemPadding(_startPadding, 0, 0, 0)
                homeItemsList.add(header)
                hasAddedSubHeaderForSubscribed = true
            }
            if (!creator.userSubscribed && !hasAddedSubHeaderForUnsubscribed) {
                val header = TextItemView.Companion.TextItem(
                    getString(R.string.discover_content_today),
                    resources.getDimension(R.dimen.subheader_text)
                )
                header.setItemPadding(_startPadding, 0, 0, 0)
                homeItemsList.add(header)
                hasAddedSubHeaderForUnsubscribed = true
            }
            //Generate Header
            val header = generateHeaderForCreator(creator)
            header.setItemPadding(_startPadding, 0, 0, 0)
            homeItemsList.add(header)

            //Generate Nested List
            val list = generateNestedRecyclerForCreator(creator)
            list.setItemPadding(_startPadding, 0, 0, 0)
            homeItemsList.add(list)
        }
        homeAdapter.updateDataSet(homeItemsList)
        updateBottomPadding()

        //Tell the recycler that it has a fixed size
        homeList.setHasFixedSize(true)

        //Tell the recycler to keep more views in cache
        homeList.setItemViewCacheSize(20)
    }

    private fun generateNestedRecyclerForCreator(creator: Creator): NestedRecyclerView.Companion.NestedRecyclerItem {
        //Create the supporting objects for the recycler view
        val creatorAdapterItems = ArrayList<BaseItem>()
        val creatorLayoutManagerOrientation = LinearLayoutManager.HORIZONTAL

        //Create the nested list item
        val creatorNestedList = NestedRecyclerView.Companion.NestedRecyclerItem(
            Id = creator.id,
            AdapterItems = creatorAdapterItems,
            LayoutManagerOrientation = creatorLayoutManagerOrientation,
            InfiniteScrollable = true
        )

        //Set Adapter attached listener so we load data only when it's showing on screen
        creatorNestedList.setOnAdapterAttachedListener { itAdapter ->
            if (itAdapter.isEmpty() && !itAdapter.isLoading()) {
                //Set the adapter to loading to prevent double requests for content
                itAdapter.setLoading(true)

                //Setup Creator Observer
                setupCreatorObserver(creator)

                //Request creator data
                requestContentForCreator(creator)
            }
        }

        //Make the nested list infinite
        creatorNestedList.setOnScrollListener { layoutManager, adapter ->
            val linearLayoutManager = layoutManager as LinearLayoutManager
            val baseAdapter = adapter as BaseViewAdapter

            if (linearLayoutManager.findLastVisibleItemPosition() == baseAdapter.itemCount - 1 && !baseAdapter.isLoading()) {
                baseAdapter.setLoading(true)
                requestContentForCreator(creator)
            }
        }

        //Return the generated list
        return creatorNestedList
    }

    private fun showLoading() {
        _binding.loadingView.visibility = ImageView.VISIBLE
        (_binding.loadingView.drawable as AnimatedVectorDrawable).start()
    }

    private fun hideLoading() {
        _binding.loadingView.visibility = ImageView.GONE
    }

    private fun generateHeaderForCreator(creator: Creator): HeaderItemView.Companion.HeaderItem {
        val description: String = if (creator.userSubscribed) {
            getString(R.string.latest_content_header)
        } else {
            getString(R.string.discover_content_header)
        }
        val title = creator.title
        val creatorLogoToLoad = selectImageQuality(requireContext(), creator.icon)
        return HeaderItemView.Companion.HeaderItem(description, title, null, creatorLogoToLoad)
    }

    private fun requestContentForCreator(creator: Creator) {
        CoroutineScope(Dispatchers.IO).launch {
            creator.id.apply {
                _homeViewModel.listCreatorContent(this)
            }
        }
    }

    private fun setupCreatorObserver(creator: Creator) {
        _homeViewModel.setupLiveDataForCreator(creator).observe(viewLifecycleOwner, Observer {
            val contentResult = it ?: return@Observer
            if (contentResult.success != null) {
                mergeContent(contentResult.success, creator)
            }
        })
    }

    private fun getNestedListForCreator(creator: Creator, adapter: BaseViewAdapter): Int {
        for ((i, item) in adapter.getItemSet().withIndex()) {
            if (item is NestedRecyclerView.Companion.NestedRecyclerItem && item.Id == creator.id) {
                return i
            }
        }

        return -1
    }

    private fun mergeContent(content: ArrayList<Post>, creator: Creator) {
        val minifyPosts = true
        val adapter = (_binding.homeList.adapter as BaseViewAdapter)
        val adapterPosition = getNestedListForCreator(creator, adapter)

        if (adapterPosition > 0) {
            val adapterItem =
                adapter.getItemAt(adapterPosition) as NestedRecyclerView.Companion.NestedRecyclerItem
            val adapterDataSet = ArrayList<BaseItem>()

            for (item in content) {
                adapterDataSet.add(PostView.Companion.PostItem(item, minifyPosts))
            }

            adapterItem.AdapterItems = adapterDataSet

            _binding.homeList.post {
                adapter.updateItem(adapterPosition, adapterItem)
            }
        }
    }

    private fun setupRecyclerView() {
        _binding.homeList.adapter = BaseViewAdapter(ArrayList())
        _binding.homeList.layoutManager = LinearLayoutManager(requireContext())
        _binding.homeList.itemAnimator = SlideInLeftAnimator()
    }

    private fun updateBottomPadding() {
        // Set the margin of the last item as the padding on the recyclerview won't work correctly.
        val homeList: RecyclerView = _binding.homeList
        val homeAdapter = homeList.adapter as BaseViewAdapter?
        if (homeAdapter != null && homeAdapter.itemCount > 0) {
            homeAdapter.getItemAt(homeAdapter.itemCount - 1).bottomMargins = _bottomPadding
            homeAdapter.notifyItemChanged(homeAdapter.itemCount - 1)
        }
    }

    private fun updateStartPadding() {
        // Set the margin of the last item as the padding on the recyclerview won't work correctly.
        val homeList: RecyclerView = _binding.homeList
        val homeAdapter = homeList.adapter as BaseViewAdapter?
        if (homeAdapter != null && homeAdapter.itemCount > 0) {
            for ((index, item) in homeAdapter.getItemSet().withIndex()) {
                item.startPadding = _startPadding
                homeAdapter.notifyItemChanged(index)
            }
        }
    }
}
