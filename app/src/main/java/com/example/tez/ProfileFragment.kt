package com.example.tez

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFragment : Fragment() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val profileNameTextView = view.findViewById<TextView>(R.id.ProfileName)
        val profileMailTextView = view.findViewById<TextView>(R.id.text_mail)

        val auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid

        if (userId != null) {
            val db = FirebaseFirestore.getInstance()
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val fullName = document.getString("name") ?: "İsimsiz Kullanıcı"
                        val email = document.getString("email") ?: "E-posta bulunamadı"

                        profileNameTextView.text = fullName
                        profileMailTextView.text = email
                    } else {
                        profileNameTextView.text = "İsim Bulunamadı"
                        profileMailTextView.text = "E-posta bulunamadı"
                    }
                }
                .addOnFailureListener {
                    profileNameTextView.text = "İsim yüklenemedi"
                    profileMailTextView.text = "E-posta yüklenemedi"
                }
        } else {
            profileNameTextView.text = "Kullanıcı giriş yapmamış"
            profileMailTextView.text = "E-posta yok"
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // Çıkış butonları
        val logoutText = view.findViewById<TextView>(R.id.textView9)
        val logoutIcon = view.findViewById<ImageView>(R.id.imageLogout)


        // Çıkış işlemi
        val logoutClickListener = View.OnClickListener {
            auth.signOut() // Firebase oturumunu kapat
            findNavController().navigate(R.id.logInFragment) // Login ekranına yönlendir
        }

        val securityButton = view.findViewById<ImageView>(R.id.bttn_edit2)
        securityButton.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_securityProfileFragment)
        }

        val settingsButton = view.findViewById<ImageView>(R.id.bttn_edit3)
        settingsButton.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_changePasswordFragment)
        }

        val helpButton =view.findViewById<ImageView>(R.id.bttn_edit4)
        helpButton.setOnClickListener{
            findNavController().navigate(R.id.action_profileFragment_to_HelpFragment)
        }

        logoutText.setOnClickListener(logoutClickListener)
        logoutIcon.setOnClickListener(logoutClickListener)

        return view
    }
}
