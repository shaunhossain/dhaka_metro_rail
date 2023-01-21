package com.shaunhossain.dhakametrorail.ui.splash

import android.Manifest
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.navigation.fragment.findNavController
import com.shaunhossain.dhakametrorail.R
import com.shaunhossain.dhakametrorail.databinding.FragmentSplashBinding
import com.shaunhossain.dhakametrorail.utils.hasLocationPermission

class SplashFragment : Fragment() {
    private var _binding: FragmentSplashBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSplashBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
            ),
            0
        )

        if (!requireContext().hasLocationPermission()) {
            throw Exception("No permission")
        } else {
            findNavController().navigate(R.id.action_splashFragment_to_mapRouteFragment)
        }
    }
}