package com.shaunhossain.dhakametrorail.ui.home

import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.navigation.fragment.findNavController
import com.shaunhossain.dhakametrorail.R
import com.shaunhossain.dhakametrorail.databinding.FragmentHomeBinding
import com.shaunhossain.dhakametrorail.databinding.FragmentSplashBinding


class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val feelings = resources.getStringArray(R.array.feelings)
        val arrayAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, feelings)
        binding.autoCompleteTextView.setAdapter(arrayAdapter)

//        Handler().postDelayed({
//            findNavController().navigate(R.id.action_homeFragment_to_mapRouteFragment)
//        }, 3000)
    }
}