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
import org.gmautostop.hitchlog.databinding.FragmentEditRecordBinding

@AndroidEntryPoint
class EditRecordFragment : Fragment(R.layout.fragment_edit_record) {
    private val recordModel: RecordOldViewModel by navGraphViewModels(R.id.nav) {
        defaultViewModelProviderFactory
    }

    private val args: EditRecordFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        args.record?.let {
            recordModel.initialize(it)
        } ?: recordModel.initialize(args.recordType)

        val binding = FragmentEditRecordBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.recordModel = recordModel

        recordModel.navigationCommands.observe(viewLifecycleOwner, Observer {
            findNavController().navigate(it)
        })
        return binding.root
    }
}
