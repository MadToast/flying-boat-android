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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.madtoast.flyingboat.R
import com.madtoast.flyingboat.api.floatplane.model.creator.Creator
import com.madtoast.flyingboat.databinding.FragmentCreatorsBinding
import com.madtoast.flyingboat.ui.components.adapters.BaseItem
import com.madtoast.flyingboat.ui.components.adapters.BaseViewAdapter
import com.madtoast.flyingboat.ui.components.views.BlankView
import com.madtoast.flyingboat.ui.components.views.CreatorItemView
import com.madtoast.flyingboat.ui.components.views.TextItemView
import com.madtoast.flyingboat.ui.fragments.creators.viewmodels.CreatorsViewModel
import com.madtoast.flyingboat.ui.fragments.creators.viewmodels.CreatorsViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CreatorsFragment : Fragment() {

    private lateinit var _creatorsViewModel: CreatorsViewModel
    private lateinit var _binding: FragmentCreatorsBinding
    private var _bottomPadding = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreatorsBinding.inflate(inflater, container, false)

        _creatorsViewModel = ViewModelProvider(
            this,
            CreatorsViewModelFactory(requireContext().cacheDir, requireContext())
        )[CreatorsViewModel::class.java]

        //Apply insets to list
        _binding.creatorList.setOnApplyWindowInsetsListener { v, insets ->
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
                    _binding.creatorList.updatePadding(top = actionBarHeight + it)
                }
                systemBarsInsets?.bottom?.let {
                    _bottomPadding = bottomNavigationHeight + bottomAdditionalPadding + it
                    updateBottomPadding()
                }
            }
            v.onApplyWindowInsets(insets)
        }

        //Initialize the view model
        _creatorsViewModel.init()

        //Initialize the observers
        setupObservers()

        //Set up recycler view
        setupRecyclerView()

        //Fetch the required data
        CoroutineScope(Dispatchers.IO).launch {
            _creatorsViewModel.listPlatformCreators(
                "",
                false,
                Creator.SubscribedComparator().reversed()
            )
        }

        return _binding.root
    }

    private fun setupRecyclerView() {
        val useGridLayout =
            resources.getBoolean(R.bool.isBigScreen) || resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        val gridColumns = resources.getInteger(R.integer.grid_columns)
        _binding.creatorList.setHasFixedSize(true)
        _binding.creatorList.adapter = BaseViewAdapter(ArrayList())

        _binding.creatorList.layoutManager = if (useGridLayout) {
            val layoutManager = GridLayoutManager(requireContext(), gridColumns)

            layoutManager.spanSizeLookup = (object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    val item = (_binding.creatorList.adapter as BaseViewAdapter).getItemAt(position)
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

    private fun setupObservers() {
        _creatorsViewModel.creatorsResult.observe(viewLifecycleOwner, Observer {
            val creatorResult = it ?: return@Observer
            if (creatorResult.success != null) {
                showCreatorsOnScreen(creatorResult.success)
            }
        })
    }

    private fun updateBottomPadding() {
        val creatorList: RecyclerView = _binding.creatorList
        val creatorsAdapter = creatorList.adapter as BaseViewAdapter?
        if (creatorsAdapter != null && creatorsAdapter.itemCount > 0) {
            if (creatorsAdapter.getItemAt(creatorsAdapter.itemCount - 1) is BlankView.Companion.BlankViewItem) {
                (creatorsAdapter.getItemAt(creatorsAdapter.itemCount - 1) as BlankView.Companion.BlankViewItem).height =
                    _bottomPadding
                creatorsAdapter.notifyItemChanged(creatorsAdapter.itemCount - 1)
            } else {
                creatorsAdapter.addItem(BlankView.Companion.BlankViewItem(_bottomPadding))
            }
        }
    }

    private fun showCreatorsOnScreen(creators: Array<Creator>) {
        val creatorList: RecyclerView = _binding.creatorList
        var creatorItemsList = ArrayList<BaseItem>()
        var creatorsAdapter = creatorList.adapter as BaseViewAdapter
        var latestSubscribedStatus = false
        for (creator in creators) {
            if (latestSubscribedStatus != creator.userSubscribed) {
                latestSubscribedStatus = creator.userSubscribed
                val label = if (creator.userSubscribed) {
                    getString(R.string.subscribed)
                } else {
                    getString(R.string.unsubscribed)
                }

                creatorItemsList.add(TextItemView.Companion.TextItem(label))
            }

            creatorItemsList.add(CreatorItemView.Companion.CreatorItem(creator))
        }

        creatorsAdapter.updateDataSet(creatorItemsList)
        updateBottomPadding()
    }
}