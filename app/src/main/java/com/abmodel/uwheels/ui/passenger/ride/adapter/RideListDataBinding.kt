package com.abmodel.uwheels.ui.passenger.ride.adapter

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.abmodel.uwheels.data.model.Ride

@BindingAdapter("rideList")
fun bindRecyclerView(
	recyclerView: RecyclerView,
	data: List<Ride>?
) {
	val adapter = recyclerView.adapter as RideItemAdapter
	adapter.submitList(data)
}
