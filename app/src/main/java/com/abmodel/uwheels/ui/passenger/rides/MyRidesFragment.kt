package com.abmodel.uwheels.ui.passenger.rides

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.abmodel.uwheels.databinding.FragmentMyRidesBinding

class MyRidesFragment : Fragment() {

	// Binding objects to access the view elements
	private var _binding: FragmentMyRidesBinding? = null
	private val binding get() = _binding!!

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		// Inflate the layout and binding for this fragment
		_binding = FragmentMyRidesBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}
}
