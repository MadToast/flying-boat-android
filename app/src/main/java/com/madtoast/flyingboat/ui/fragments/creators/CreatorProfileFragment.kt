package com.madtoast.flyingboat.ui.fragments.creators

import android.content.res.Configuration
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.madtoast.flyingboat.R
import com.madtoast.flyingboat.api.floatplane.model.enums.SortType
import com.madtoast.flyingboat.databinding.FragmentCreatorProfileBinding
import com.madtoast.flyingboat.ui.components.adapters.BaseViewAdapter
import com.madtoast.flyingboat.ui.components.adapters.ViewPagerCustomViewsAdapter
import com.madtoast.flyingboat.ui.components.views.PostView
import com.madtoast.flyingboat.ui.components.views.TextItemView
import com.madtoast.flyingboat.ui.fragments.creators.viewmodels.CreatorProfileViewModel
import com.madtoast.flyingboat.ui.fragments.creators.viewmodels.CreatorProfileViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CreatorProfileFragment(itemView: View) : Fragment() {

    companion object {
        const val LIMIT: Int = 20
    }

    private lateinit var _creatorProfileViewModel: CreatorProfileViewModel
    private lateinit var _binding: FragmentCreatorProfileBinding
    val args: CreatorProfileFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreatorProfileBinding.inflate(inflater, container, false)

        _creatorProfileViewModel = ViewModelProvider(
            this,
            CreatorProfileViewModelFactory(requireContext().cacheDir, requireContext())
        )[CreatorProfileViewModel::class.java]

        //Apply insets to list
        _binding.root.setOnApplyWindowInsetsListener { v, insets ->
            val tv = TypedValue()
            if (requireActivity().theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
                val actionBarHeight =
                    TypedValue.complexToDimensionPixelSize(tv.data, resources.displayMetrics)
                //Calculate top padding to take status bar into account
                val windowInsets =
                    ViewCompat.getRootWindowInsets(requireActivity().window.decorView)
                val systemBarsInsets = windowInsets?.getInsets(WindowInsetsCompat.Type.systemBars())
                systemBarsInsets?.top?.let {
                    _binding.root.updatePadding(top = actionBarHeight + it)
                }
                systemBarsInsets?.bottom?.let {
                    _binding.root.updatePadding(bottom = it)
                }
            }
            v.onApplyWindowInsets(insets)
        }

        //Initialize the view model
        _creatorProfileViewModel.init()

        //Set up recycler view
        setupViewPager()

        return _binding.root
    }

    private fun loadCreatorContent(
        forceRefresh: Boolean = false,
        search: String? = null,
        tags: Array<String>? = null,
        hasVideo: Boolean? = null,
        hasAudio: Boolean? = null,
        hasPicture: Boolean? = null,
        hasText: Boolean? = null,
        sort: SortType? = null,
        fromDuration: Int? = null,
        toDuration: Int? = null,
        fromDate: String? = null,
        toDate: String? = null
    ) {
        //Fetch the required data
        CoroutineScope(Dispatchers.IO).launch {
            _creatorProfileViewModel.listCreatorContent(
                args.creatorId,
                forceRefresh,
                LIMIT,
                _creatorProfileViewModel.postResults.value?.success?.size, //Always fetch after current size
                search,
                tags,
                hasVideo,
                hasAudio,
                hasPicture,
                hasText,
                sort,
                fromDuration,
                toDuration,
                fromDate,
                toDate
            )
        }
    }

    private fun setupViewPager() {
        val listContentHandler = object : ViewPagerCustomViewsAdapter.ViewItemHandler {
            override fun getViewType(): Int {
                return 0
            }

            override fun initializeView(parent: ViewGroup): RecyclerView.ViewHolder {
                val recyclerView = RecyclerView(parent.context)
                setupRecyclerView(recyclerView)
                return ViewPagerCustomViewsAdapter.ViewPagerContent(recyclerView)
            }

            override fun applyContent(viewHolder: RecyclerView.ViewHolder) {
                if (viewHolder.itemView is RecyclerView && _creatorProfileViewModel.postResults.value?.success != null) {
                    val sourceData = _creatorProfileViewModel.postResults.value!!.success!!
                    val recyclerView = viewHolder.itemView as RecyclerView
                    val adapter = recyclerView.adapter as BaseViewAdapter
                    val adapterDataSet = adapter.getItemSet()
                    var startingIndex =
                        0 //Move forward in adapter at the same time we move forward with received content

                    if (adapterDataSet.size - 1 > sourceData.size) {
                        val itemsRemoved = adapterDataSet.size
                        adapterDataSet.clear() //Clear the adapter data set
                        adapter.notifyItemRangeRemoved(
                            0,
                            itemsRemoved
                        ) //Notify adapter that items were removed
                    }

                    for (item in sourceData) {
                        var itemWasUpdated = false
                        for (i in startingIndex..adapterDataSet.size) {
                            val currentItem = adapterDataSet[i]
                            if (currentItem is PostView.Companion.PostItem) {
                                startingIndex = i

                                itemWasUpdated = true
                                break
                            }
                        }

                        if (!itemWasUpdated) {
                            adapter.addItem(PostView.Companion.PostItem(item))
                            startingIndex++ //Move this forward to avoid entering loop above
                        }
                    }
                }
            }
        }
    }

    private fun setupRecyclerView(recyclerView: RecyclerView) {
        val useGridLayout =
            resources.getBoolean(R.bool.isBigScreen) || resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        val gridColumns = resources.getInteger(R.integer.grid_columns)
        recyclerView.adapter = BaseViewAdapter(ArrayList())
        recyclerView.layoutManager = if (useGridLayout) {
            val layoutManager = GridLayoutManager(requireContext(), gridColumns)
            layoutManager.spanSizeLookup = (object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    val item = (recyclerView.adapter as BaseViewAdapter).getItemAt(position)
                    return if (item is TextItemView.Companion.TextItem) {
                        gridColumns
                    } else {
                        1
                    }
                }
            })

            layoutManager
        } else {
            LinearLayoutManager(requireContext())
        }
    }
}