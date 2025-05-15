package com.example.tez

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.tez.databinding.FragmentSignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class SignUpFragment : Fragment() {

    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val suToLogin = view.findViewById<ImageView>(R.id.su_to_login)

        suToLogin.setOnClickListener {
            findNavController().navigateUp()
        }

        // Firebase Authentication başlat
        auth = FirebaseAuth.getInstance()

        // Kayıt ol butonuna tıklama işlemi
        binding.bttnSuSignup.setOnClickListener {
            val name = binding.txtSuName.text.toString().trim()
            val email = binding.txtSuMail.text.toString().trim()
            val phone = binding.txtSuNo.text.toString().trim()
            val birthDate = binding.txtSuBirth.text.toString().trim()
            val password = binding.txtSuPassword.text.toString().trim()
            val confirmPassword = binding.txtSuPassword1.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || birthDate.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(requireContext(), "Lütfen tüm alanları doldurun!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(requireContext(), "Şifreler uyuşmuyor!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            registerUser(email, password, name, phone, birthDate)
        }

    }

    private fun registerUser(email: String, password: String, name: String, phone: String, birthDate: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        saveUserToFirestore(it.uid, name, email, phone, birthDate, password)
                        sendEmailVerification(it)
                    }
                } else {
                    Toast.makeText(requireContext(), "Kayıt başarısız: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun saveUserToFirestore(userId: String, name: String, email: String, phone: String, birthDate: String, password: String) {
        val db = FirebaseFirestore.getInstance()
        val userMap = hashMapOf(
            "name" to name,
            "email" to email,
            "phone" to phone,
            "birthDate" to birthDate,
            "created_at" to System.currentTimeMillis(),
            "password" to password
        )

        db.collection("users").document(userId)
            .set(userMap)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Kullanıcı bilgileri kaydedildi!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Firestore hata: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }



    private fun sendEmailVerification(user: FirebaseUser?) {
        user?.sendEmailVerification()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(requireContext(), "Kayıt başarılı! Lütfen e-posta adresinizi doğrulayın.", Toast.LENGTH_LONG).show()
                findNavController().navigate(R.id.action_signUpFragment_to_logInFragment)
            } else {
                Toast.makeText(requireContext(), "E-posta doğrulama gönderilemedi: ${task.exception?.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
