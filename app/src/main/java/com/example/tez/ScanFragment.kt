package com.example.tez

import android.Manifest
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.example.tez.databinding.FragmentScanBinding
import com.example.tez.model.Expense
import com.example.tez.utils.EditPriceDialogFragment
import com.google.firebase.Timestamp
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ScanFragment : Fragment() {

    private var _binding: FragmentScanBinding? = null
    private val binding get() = _binding!!

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private lateinit var cameraExecutor: ExecutorService

    private var detectedPrice: String? = null

    companion object {
        private const val CAMERA_PERMISSION_REQUEST = 1001
    }

    private var categoryName: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        categoryName = arguments?.getString("selectedCategoryName")

        binding.toolbarScan.setNavigationOnClickListener {
            findNavController().navigate(R.id.action_scanFragment_to_expensesFragment)
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
        setupScanButton()

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST
            )
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also { preview ->
                binding.previewView.surfaceProvider?.let {
                    preview.setSurfaceProvider(it)
                }
            }

            val imageAnalysis = ImageAnalysis.Builder()
                .setTargetResolution(Size(1280, 720))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                processImageProxy(imageProxy)
            }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                viewLifecycleOwner,
                cameraSelector,
                preview,
                imageAnalysis
            )
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    @OptIn(ExperimentalGetImage::class)
    private fun processImageProxy(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val rotationDegrees = imageProxy.imageInfo.rotationDegrees
            val image = InputImage.fromMediaImage(mediaImage, rotationDegrees)

            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val currentBinding = _binding ?: run {
                        imageProxy.close()
                        return@addOnSuccessListener
                    }
                    val previewView = currentBinding.previewView
                    val focusBox = currentBinding.focusBox

                    val focusLocation = IntArray(2)
                    val previewLocation = IntArray(2)
                    focusBox.getLocationOnScreen(focusLocation)
                    previewView.getLocationOnScreen(previewLocation)

                    val focusRect = Rect(
                        focusLocation[0] - previewLocation[0],
                        focusLocation[1] - previewLocation[1],
                        focusLocation[0] - previewLocation[0] + focusBox.width,
                        focusLocation[1] - previewLocation[1] + focusBox.height
                    )

                    val filteredText = visionText.textBlocks
                        .flatMap { it.lines }
                        .filter { line ->
                            line.boundingBox?.let { box ->
                                val centerX = box.centerX() * previewView.width / image.width
                                val centerY = box.centerY() * previewView.height / image.height
                                focusRect.contains(centerX, centerY)
                            } ?: false
                        }
                        .joinToString(" ") { it.text }

                    val price = extractPrice(filteredText)
                    detectedPrice = price

                    activity?.runOnUiThread {
                        _binding?.detectedPriceTextView?.text = price?.let {
                            "Detected Price: $it"
                        } ?: "Detected Price: "
                    }
                }
                .addOnFailureListener {
                    Log.e("ScanFragment", "Text recognition failed", it)
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }

    private fun extractPrice(text: String): String? {
        val regex = Regex("""\d+[.,]?\d{0,2}""")
        return regex.findAll(text)
            .map { it.value.replace(",", ".") }
            .filter { it.contains(".") }
            .maxByOrNull { it.length }
    }

    private fun setupScanButton() {
        binding.scanButton.setOnClickListener {
            val anim = ObjectAnimator.ofArgb(
                binding.focusBox.background,
                "tint",
                Color.GREEN,
                Color.TRANSPARENT
            )
            anim.duration = 500
            anim.repeatCount = 1
            anim.repeatMode = ValueAnimator.REVERSE
            anim.start()

            val priceToShow = detectedPrice ?: "0.00"
            val selectedCategory = categoryName ?: "Other"
            val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid

            if (userId == null) {
                Toast.makeText(requireContext(), "Kullanıcı oturumu yok.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val dialog = EditPriceDialogFragment(
                priceToShow,
                selectedCategory,
                userId
            ) {
                // Save başarılı olunca fragment geçişi yap
                findNavController().navigate(R.id.action_scanFragment_to_expensesFragment)
            }
            dialog.show(parentFragmentManager, "EditPriceDialog")
        }
    }




    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        cameraExecutor.shutdown()
    }
}
