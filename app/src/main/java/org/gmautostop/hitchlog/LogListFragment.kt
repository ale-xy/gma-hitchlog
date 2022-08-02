package org.gmautostop.hitchlog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import dagger.hilt.android.AndroidEntryPoint
import org.gmautostop.hitchlog.databinding.FragmentLogListBinding

@AndroidEntryPoint
class LogListFragment : Fragment() {

    private val viewModel: LogListViewModel by navGraphViewModels(R.id.nav){
        defaultViewModelProviderFactory
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentLogListBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        viewModel.navigationCommands.observe(viewLifecycleOwner, Observer {
            findNavController().navigate(it)
        })

        return binding.root
    }
}