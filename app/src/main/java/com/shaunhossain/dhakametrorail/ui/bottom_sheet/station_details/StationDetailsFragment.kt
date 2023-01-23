package com.shaunhossain.dhakametrorail.ui.bottom_sheet.station_details

import android.os.Bundle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.shaunhossain.dhakametrorail.databinding.FragmentStationDetailsBinding


class StationDetailsFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentStationDetailsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentStationDetailsBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

    }

    companion object {

        // TODO: Customize parameters
        fun newInstance(itemCount: Int): StationDetailsFragment =
            StationDetailsFragment().apply {
                arguments = Bundle().apply {
                    putInt("1", itemCount)
                }
            }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}