package org.gmautostop.hitchlog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.firebase.ui.auth.AuthUI
import dagger.hilt.android.AndroidEntryPoint
import org.gmautostop.hitchlog.databinding.FragmentAuthBinding

@AndroidEntryPoint
class AuthFragment: Fragment() {
    private val viewModel: AuthViewModel by navGraphViewModels(R.id.nav){
        defaultViewModelProviderFactory
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentAuthBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        viewModel.navigationCommands.observe(viewLifecycleOwner, Observer {
            findNavController().navigate(it)
        })

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        if (viewModel.signedIn) {
            findNavController().navigate(
                R.id.action_authFragment_to_logListFragment,
                null,
                NavOptions.Builder().setPopUpTo(
                    R.id.authFragment,
                    true
                ).build()
            )
        } else {
            startActivityForResult(
                AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(listOf(
                        AuthUI.IdpConfig.EmailBuilder().build(),
                        AuthUI.IdpConfig.GoogleBuilder().build())
                    ).build(),
                1111)
        }
    }
}