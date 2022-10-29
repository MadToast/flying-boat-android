package com.madtoast.flyingboat.ui.fragments.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.madtoast.flyingboat.databinding.FragmentHomeBinding
import com.madtoast.flyingboat.ui.components.adapters.BaseViewAdapter
import com.madtoast.flyingboat.ui.components.views.LoadingView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private var isLoading = false

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(
                this,
                HomeViewModelFactory(requireContext().cacheDir, requireContext())
            )[HomeViewModel::class.java]

        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        setupRecyclerView(binding, homeViewModel)
        setupObservers(binding, homeViewModel)
        loadRequiredData(homeViewModel)

        return binding.root
    }

    private fun loadRequiredData(viewModel: HomeViewModel) {
        if (viewModel.subscriptionsArrayList.isEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                viewModel.subscriptions()
            }
        }
    }

    private fun setupObservers(binding: FragmentHomeBinding, homeViewModel: HomeViewModel) {

    }

    private fun setupRecyclerView(binding: FragmentHomeBinding, homeViewModel: HomeViewModel) {
        binding.homeRecyclerView.adapter = BaseViewAdapter(homeViewModel.postItemsArrayList)

        binding.homeRecyclerView.onScrolledListener {
            val linearLayoutManager = it.layoutManager as LinearLayoutManager

            if (!isLoading) {
                if (linearLayoutManager.findLastCompletelyVisibleItemPosition() == it.adapter!!.itemCount - 1) {
                    //bottom of list!
                    setLoading(it.adapter as BaseViewAdapter)
                    CoroutineScope(Dispatchers.IO).launch {
                        //homeViewModel.creatorList()
                    }
                }
            }
        }
    }

    private fun setLoading(adapter: BaseViewAdapter) {
        adapter.addItem(LoadingView.Companion.LoadingItem(null, true))
        isLoading = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

/**
 * Extension function to simplify setting an afterTextChanged action to EditText components.
 */
fun RecyclerView.onScrolledListener(onScrolled: (recyclerView: RecyclerView) -> Unit) {
    this.addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            onScrolled.invoke(recyclerView)
        }
    })
}
