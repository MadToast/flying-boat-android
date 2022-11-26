package com.madtoast.flyingboat.ui.fragments.home

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutParams
import com.madtoast.flyingboat.R
import com.madtoast.flyingboat.api.floatplane.model.content.Post
import com.madtoast.flyingboat.api.floatplane.model.creator.Creator
import com.madtoast.flyingboat.databinding.FragmentHomeBinding
import com.madtoast.flyingboat.ui.components.adapters.BaseItem
import com.madtoast.flyingboat.ui.components.adapters.BaseViewAdapter
import com.madtoast.flyingboat.ui.components.views.*
import com.madtoast.flyingboat.ui.fragments.home.viewmodels.HomeViewModel
import com.madtoast.flyingboat.ui.fragments.home.viewmodels.HomeViewModelFactory
import com.madtoast.flyingboat.ui.utilities.selectImageQuality
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class HomeFragment : Fragment() {

    private lateinit var _homeViewModel: HomeViewModel
    private lateinit var _binding: FragmentHomeBinding
    private var _bottomPadding = 0
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
                //Calculate top and bottom padding to take status bar and navigation bar into account
                val windowInsets =
                    ViewCompat.getRootWindowInsets(requireActivity().window.decorView)
                val systemBarsInsets = windowInsets?.getInsets(WindowInsetsCompat.Type.systemBars())
                systemBarsInsets?.top?.let {
                    _binding.homeList.updatePadding(top = actionBarHeight + it)
                }
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
        homeItemsList.add(TextItemView.Companion.TextItem(getString(R.string.welcome)))
        for (creator in creators) {
            //Generate Header
            homeItemsList.add(generateHeaderForCreator(creator))

            //Generate Nested List
            homeItemsList.add(generateNestedRecyclerForCreator(creator))
        }
        homeItemsList.add(BlankView.Companion.BlankViewItem(_bottomPadding))
        homeAdapter.updateDataSet(homeItemsList)
        updateBottomPadding()
    }

    private fun generateNestedRecyclerForCreator(creator: Creator): NestedRecyclerView.Companion.NestedRecyclerItem {
        //Create the supporting objects for the recycler view
        val creatorAdapterItems = ArrayList<BaseItem>()
        val creatorLayoutManagerOrientation = LinearLayoutManager.HORIZONTAL

        //Set the nested recycler items to not match the parent's width
        val childLayoutParams = LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        //Create the nested list item
        val creatorNestedList = NestedRecyclerView.Companion.NestedRecyclerItem(
            Id = creator.id!!,
            AdapterItems = creatorAdapterItems,
            LayoutManagerOrientation = creatorLayoutManagerOrientation
        )

        //Set Adapter attached listener so we load data only when it's showing on screen
        creatorNestedList.setOnAdapterAttachedListener { itAdapter ->
            if (itAdapter.itemCount == 0) {
                //Setup Creator Observer
                setupCreatorObserver(creator)

                //Load content for creator
                requestContentForCreator(creator)
            }
        }

        //Make the nested list infinite
        creatorNestedList.setOnScrollListener { layoutManager, adapter ->
            val linearLayoutManager = layoutManager as LinearLayoutManager
            val baseAdapter = adapter as BaseViewAdapter

            if (linearLayoutManager.findLastCompletelyVisibleItemPosition() == baseAdapter.itemCount - 1) {
                requestContentForCreator(creator)
            }
        }

        //Return the generated list
        return creatorNestedList
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
            creator.id?.apply {
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
            var startingIndex =
                0 //Move forward in adapter at the same time we move forward with received content

            for (item in content) {
                adapterDataSet.add(PostView.Companion.PostItem(item, minifyPosts))
                startingIndex++ //Move this forward to avoid entering loop above
            }

            adapterItem.AdapterItems = adapterDataSet
            adapter.updateItem(adapterPosition, adapterItem)
        }
    }

    private fun setupRecyclerView() {
        _binding.homeList.adapter = BaseViewAdapter(ArrayList())
        _binding.homeList.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun updateBottomPadding() {
        val homeList: RecyclerView = _binding.homeList
        val homeAdapter = homeList.adapter as BaseViewAdapter?
        if (homeAdapter != null && homeAdapter.itemCount > 0) {
            if (homeAdapter.getItemAt(homeAdapter.itemCount - 1) is BlankView.Companion.BlankViewItem) {
                (homeAdapter.getItemAt(homeAdapter.itemCount - 1) as BlankView.Companion.BlankViewItem).height =
                    _bottomPadding
                homeAdapter.notifyItemChanged(homeAdapter.itemCount - 1)
            } else {
                homeAdapter.addItem(BlankView.Companion.BlankViewItem(_bottomPadding))
            }
        }
    }
}
