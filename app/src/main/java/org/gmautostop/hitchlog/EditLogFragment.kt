package org.gmautostop.hitchlog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import dagger.hilt.android.AndroidEntryPoint
import org.gmautostop.hitchlog.databinding.FragmentEditLogBinding

@AndroidEntryPoint
class EditLogFragment : Fragment(R.layout.fragment_edit_log) {
    private val viewModel: EditLogViewModel by navGraphViewModels(R.id.nav) {
        defaultViewModelProviderFactory
    }

    private val args: EditLogFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        viewModel.id = args.logId ?: ""
        viewModel.name = args.logName ?: ""

        val binding = FragmentEditLogBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.editLogModel = viewModel

        viewModel.navigationCommands.observe(viewLifecycleOwner, Observer {
            findNavController().navigate(it)
        })
        return binding.root
    }
}
