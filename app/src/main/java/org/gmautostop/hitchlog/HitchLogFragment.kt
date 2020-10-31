package org.gmautostop.hitchlog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import org.gmautostop.hitchlog.databinding.FragmentHitchlogBinding

class HitchLogFragment : Fragment() {

    private val viewModel: HitchLogViewModel by navGraphViewModels(R.id.nav)

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

        //todo select log
        viewModel.logId = "log"

        return binding.root
    }
}

//class HichLogAdapter(private val data: List<HitchLogRecord>) : RecyclerBaseAdapter() {
//    override fun getLayoutIdForPosition(position: Int) = R.layout.log_item
//
//    override fun getViewModel(position: Int) = ListItemViewModel(data[position])
//
//    override fun getItemCount() = data.size
//}