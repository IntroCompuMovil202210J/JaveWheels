package com.abmodel.uwheels.ui.shared.profile

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.abmodel.uwheels.BuildConfig
import com.abmodel.uwheels.R
import com.abmodel.uwheels.data.model.UploadedFile
import com.abmodel.uwheels.data.repository.auth.FirebaseAuthRepository
import com.abmodel.uwheels.databinding.FragmentEditProfileBinding
import com.abmodel.uwheels.ui.driver.apply.DriverApplicationFragment
import com.abmodel.uwheels.util.TEMP_IMG_FILE_EXT
import com.abmodel.uwheels.util.TEMP_IMG_FILE_NAME
import com.abmodel.uwheels.util.TakePictureWithUriReturnContract
import com.bumptech.glide.Glide
import java.io.File

class EditProfileFragment : Fragment() {

	// Binding objects to access the view elements
	private var _binding: FragmentEditProfileBinding? = null
	private val binding get() = _binding!!

	private var photoFile: UploadedFile? = null

	private val viewModel: EditProfileViewModel by viewModels()

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		// Inflate the layout and binding for this fragment
		_binding = FragmentEditProfileBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		binding.apply {
			FirebaseAuthRepository.getInstance().getLoggedInUser().let { user ->
				name.setText(user.name)
				lastName.setText(user.lastName)
				phoneNumber.setText(user.phone)

				// Set the profile photo
				if (user.photoUrl != null) {
					Glide.with(requireContext())
						.load(user.photoUrl)
						.placeholder(R.drawable.ic_account_circle)
						.error(R.drawable.ic_account_circle)
						.into(profilePhoto)
				}
			}

			save.setOnClickListener {
				onSavePressed()
			}
			cancel.setOnClickListener {
				onCancelPressed()
			}
			upload.setOnClickListener {
				onUploadPressed()
			}
			camera.setOnClickListener {
				onCameraPressed()
			}
		}

		viewModel.saveResult.observe(viewLifecycleOwner) { result ->
			result ?: return@observe
			result.error?.let {
				showMessage(it)
			}
			if (result.success) {
				findNavController().navigateUp()
			}
		}
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	private fun onSavePressed() {
		binding.apply {
			viewModel.editProfile(
				name.text.toString(),
				lastName.text.toString(),
				phoneNumber.text.toString(),
				photoFile
			)
		}
	}

	private fun onCancelPressed() {
		findNavController().navigateUp()
	}

	private fun showMessage(@StringRes message: Int) {
		Toast
			.makeText(requireContext(), message, Toast.LENGTH_SHORT)
			.show()
	}

	/**
	 * Calls [getFileFromDeviceResult] with the "image" MIME type
	 */
	private fun onUploadPressed() {
		getFileFromDeviceResult.launch("image/*")
	}

	/**
	 * Calls [takePictureResult] only if the scope is STARTED.
	 * Passes the temporal uri generated by [getTmpFileUri]
	 */
	private fun onCameraPressed() {
		lifecycleScope.launchWhenStarted {
			getTmpFileUri().let { uri ->
				Log.d(DriverApplicationFragment.TAG, "Generated temp uri: $uri")
				takePictureResult.launch(uri)
			}
		}
	}

	/**
	 * Launches an implicit intent to get a file from the device
	 */
	private val getFileFromDeviceResult =
		registerForActivityResult(
			ActivityResultContracts.GetContent()
		) { uri: Uri? ->
			if (uri != null) {
				onFileUploaded(uri)
			}
		}

	/**
	 * Launches an implicit intent to take a picture with the device's camera app
	 */
	private val takePictureResult =
		registerForActivityResult(
			TakePictureWithUriReturnContract()
		) { (isSuccess, imageUri) ->
			if (isSuccess) {
				onFileUploaded(imageUri)
			}
		}

	private fun onFileUploaded(uri: Uri) {
		// Get the file mimetype
		val fileMimeType = requireContext().contentResolver
			.getType(uri)
		// Get the file extension from the mimetype
		val fileExtension = fileMimeType?.substring(
			fileMimeType.lastIndexOf("/") + 1
		)
		// Get the file name
		val fileName =
			requireContext().getString(R.string.uploaded_profile_picture_name, fileExtension)

		photoFile =
			UploadedFile(
				fileName,
				fileMimeType ?: "",
				uri
			)

		binding.profilePhoto.setImageURI(uri)
	}

	/**
	 * Generates a temporal file uri to be used to store the picture taken
	 */
	private fun getTmpFileUri(): Uri {
		val tmpFile = File.createTempFile(
			TEMP_IMG_FILE_NAME, TEMP_IMG_FILE_EXT, activity?.cacheDir
		).apply {
			createNewFile()
			deleteOnExit()
		}

		return FileProvider.getUriForFile(
			requireContext(), "${BuildConfig.APPLICATION_ID}.provider", tmpFile
		)
	}
}
