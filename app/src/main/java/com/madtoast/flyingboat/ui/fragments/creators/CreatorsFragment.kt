package com.madtoast.flyingboat.ui.fragments.creators

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.madtoast.flyingboat.api.floatplane.model.creator.Creator
import com.madtoast.flyingboat.databinding.FragmentCreatorsBinding
import com.madtoast.flyingboat.ui.components.adapters.BaseItem
import com.madtoast.flyingboat.ui.components.adapters.BaseViewAdapter
import com.madtoast.flyingboat.ui.components.views.CreatorItemView
import com.madtoast.flyingboat.ui.fragments.creators.viewmodels.CreatorsViewModel
import com.madtoast.flyingboat.ui.fragments.creators.viewmodels.CreatorsViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CreatorsFragment : Fragment() {

    lateinit var _creatorsViewModel: CreatorsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentCreatorsBinding.inflate(inflater, container, false)

        _creatorsViewModel = ViewModelProvider(
            this,
            CreatorsViewModelFactory(requireContext().cacheDir, requireContext())
        )[CreatorsViewModel::class.java]

        //Initialize the loginViewModel
        _creatorsViewModel.init()

        //Initialize the login observers
        setupObservers(binding, _creatorsViewModel)

        //Set up recycler view
        setupRecyclerView(binding)

        //Fetch the required data
        CoroutineScope(Dispatchers.IO).launch {
            _creatorsViewModel.listPlatformCreators("", false)
        }

        return binding.root
    }

    fun setupRecyclerView(binding: FragmentCreatorsBinding) {
        binding.creatorList.setHasFixedSize(true)
        binding.creatorList.adapter = BaseViewAdapter(ArrayList())
        binding.creatorList.layoutManager = LinearLayoutManager(requireContext())
    }

    fun setupObservers(binding: FragmentCreatorsBinding, viewModel: CreatorsViewModel) {
        viewModel.creatorsResult.observe(viewLifecycleOwner, Observer {
            val creatorResult = it ?: return@Observer
            if (creatorResult.success != null) {
                showCreatorsOnScreen(binding, creatorResult.success)
            }
        })
    }

    fun showCreatorsOnScreen(binding: FragmentCreatorsBinding, creators: Array<Creator>) {
        val creatorList: RecyclerView = binding.creatorList
        var creatorItemsList = ArrayList<BaseItem>()
        var creatorsAdapter = creatorList.adapter as BaseViewAdapter

        for (creator in creators) {
            creatorItemsList.add(CreatorItemView.Companion.CreatorItem(creator))
        }

        creatorsAdapter.updateDataSet(creatorItemsList)
    }
}