package com.example.tez

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.tez.databinding.FragmentLogInBinding
import com.google.firebase.auth.FirebaseAuth

class LogInFragment : Fragment() {

    private var _binding: FragmentLogInBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLogInBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var isLoginPasswordVisible = false

        binding.eyeLogin.setOnClickListener {
            isLoginPasswordVisible = !isLoginPasswordVisible

            if (isLoginPasswordVisible) {
                binding.etPassword.inputType = android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                binding.eyeLogin.setImageResource(R.drawable.eye_open)
            } else {
                binding.etPassword.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
                binding.eyeLogin.setImageResource(R.drawable.eye)
            }

            // İmleç konumunu koru
            binding.etPassword.setSelection(binding.etPassword.text.length)
        }


        // Firebase Auth başlat
        auth = FirebaseAuth.getInstance()

        // Login butonu tıklanma işlemi
        binding.btnLogin.setOnClickListener {
            val email = binding.etMail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginUser(email, password)
            } else {
                Toast.makeText(requireContext(), "Email ve şifreyi giriniz!", Toast.LENGTH_SHORT).show()
            }
        }

        // SignUp butonu tıklama işlemi
        binding.btnSignup.setOnClickListener {
            findNavController().navigate(R.id.action_logInFragment_to_SignUpFragment)
        }
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(requireContext(), "Giriş başarılı!", Toast.LENGTH_SHORT).show()

                    // Giriş başarılıysa HomeFragment'e geçiş yap
                    val navController = findNavController()
                    navController.navigate(R.id.action_SignUpFragment_to_homeFragment) // homeFragment'e yönlendirme
                } else {
                    Toast.makeText(requireContext(), "Giriş başarısız: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
