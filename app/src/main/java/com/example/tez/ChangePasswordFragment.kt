package com.example.tez

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.tez.databinding.FragmentChangePasswordBinding
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

class ChangePasswordFragment : Fragment() {

    private var _binding: FragmentChangePasswordBinding? = null
    private val binding get() = _binding!!

    private lateinit var etCurrentPassword: EditText
    private lateinit var etNewPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnChangePassword: Button
    private val auth = FirebaseAuth.getInstance()



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentChangePasswordBinding.inflate(inflater, container, false)
        val view = binding.root

        etCurrentPassword = view.findViewById(R.id.currentPass)
        etNewPassword = view.findViewById(R.id.newPass)
        etConfirmPassword = view.findViewById(R.id.confirmNewPass)
        btnChangePassword = view.findViewById(R.id.btn_pass_save)

        btnChangePassword.setOnClickListener {
            changePassword()
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var isCurrentPassVisible = false
        var isNewPassVisible = false
        var isConfirmPassVisible = false

// eyeCurrent için
        binding.eyeCurrent.setOnClickListener {
            isCurrentPassVisible = !isCurrentPassVisible

            if (isCurrentPassVisible) {
                binding.currentPass.inputType = android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                binding.eyeCurrent.setImageResource(R.drawable.eye_open)
            } else {
                binding.currentPass.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
                binding.eyeCurrent.setImageResource(R.drawable.eye)
            }
            binding.currentPass.setSelection(binding.currentPass.text.length)
        }

// eyeNew için
        binding.eyeNew.setOnClickListener {
            isNewPassVisible = !isNewPassVisible

            if (isNewPassVisible) {
                binding.newPass.inputType = android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                binding.eyeNew.setImageResource(R.drawable.eye_open)
            } else {
                binding.newPass.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
                binding.eyeNew.setImageResource(R.drawable.eye)
            }
            binding.newPass.setSelection(binding.newPass.text.length)
        }

// eyeConfirm için
        binding.eyeConfirm.setOnClickListener {
            isConfirmPassVisible = !isConfirmPassVisible

            if (isConfirmPassVisible) {
                binding.confirmNewPass.inputType = android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                binding.eyeConfirm.setImageResource(R.drawable.eye_open)
            } else {
                binding.confirmNewPass.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
                binding.eyeConfirm.setImageResource(R.drawable.eye)
            }
            binding.confirmNewPass.setSelection(binding.confirmNewPass.text.length)
        }




        binding.leftSettings.setOnClickListener() {
            findNavController().navigate(R.id.action_changePasswordFragment_to_profileFragment)
        }
    }

    private fun changePassword() {
        val currentPassword = etCurrentPassword.text.toString()
        val newPassword = etNewPassword.text.toString()
        val confirmPassword = etConfirmPassword.text.toString()

        val user = auth.currentUser

        if (user == null) {
            Toast.makeText(context, "Kullanıcı oturumu açık değil.", Toast.LENGTH_SHORT).show()
            return
        }

        if (newPassword != confirmPassword) {
            Toast.makeText(context, "Yeni şifreler uyuşmuyor.", Toast.LENGTH_SHORT).show()
            return
        }

        val email = user.email
        if (email.isNullOrEmpty()) {
            Toast.makeText(context, "Email bulunamadı.", Toast.LENGTH_SHORT).show()
            return
        }

        // Re-authenticate the user
        val credential = EmailAuthProvider.getCredential(email, currentPassword)

        user.reauthenticate(credential)
            .addOnCompleteListener { authTask ->
                if (authTask.isSuccessful) {
                    user.updatePassword(newPassword)
                        .addOnCompleteListener { updateTask ->
                            if (updateTask.isSuccessful) {
                                Toast.makeText(context, "Şifre başarıyla güncellendi.", Toast.LENGTH_SHORT).show()
                                // Optionally clear fields
                                etCurrentPassword.text.clear()
                                etNewPassword.text.clear()
                                etConfirmPassword.text.clear()
                            } else {
                                Toast.makeText(context, "Şifre güncellenemedi: ${updateTask.exception?.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                } else {
                    Toast.makeText(context, "Mevcut şifre yanlış.", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
