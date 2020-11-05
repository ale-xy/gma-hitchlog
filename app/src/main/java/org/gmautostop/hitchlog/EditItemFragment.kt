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
import org.gmautostop.hitchlog.databinding.FragmentEditItemBinding

@AndroidEntryPoint
class EditItemFragment : Fragment(R.layout.fragment_edit_item) {
    private val recordModel: RecordViewModel by navGraphViewModels(R.id.nav) {
        defaultViewModelProviderFactory
    }
    private val args: EditItemFragmentArgs by navArgs()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        args.record?.let {
            recordModel.initialize(it)
        } ?: recordModel.initialize(args.recordType)

        val binding = FragmentEditItemBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.recordModel = recordModel

        recordModel.navigationCommands.observe(viewLifecycleOwner, Observer {
            findNavController().navigate(it)
        })
        return binding.root
    }
}
