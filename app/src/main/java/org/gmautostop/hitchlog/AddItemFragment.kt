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
import org.gmautostop.hitchlog.databinding.FragmentAddItemBinding

class AddItemFragment : Fragment(R.layout.fragment_add_item) {
    private val viewModel: HitchLogViewModel by navGraphViewModels(R.id.nav)
    private val recordModel: RecordViewModel by navGraphViewModels(R.id.nav)
    private val args: AddItemFragmentArgs by navArgs()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        recordModel.reset()
        recordModel.type = args.recordType
        val binding = FragmentAddItemBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        binding.recordModel = recordModel

        viewModel.navigationCommands.observe(viewLifecycleOwner, Observer {
            findNavController().navigate(it)
        })
        return binding.root
    }
}
