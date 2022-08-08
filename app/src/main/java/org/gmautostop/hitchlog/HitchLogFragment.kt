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
import org.gmautostop.hitchlog.databinding.FragmentHitchlogBinding

@AndroidEntryPoint
class HitchLogFragment : Fragment() {

    private val viewModel: HitchLogOldViewModel by navGraphViewModels(R.id.nav){
        defaultViewModelProviderFactory
    }

    private val args: HitchLogFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentHitchlogBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        viewModel.navigationCommands.observe(viewLifecycleOwner, Observer {
            findNavController().navigate(it)
        })

        viewModel.logId = args.logId

        return binding.root
    }
}
