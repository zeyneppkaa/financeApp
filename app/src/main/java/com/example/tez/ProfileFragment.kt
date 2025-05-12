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

class ProfileFragment : Fragment() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
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





        logoutText.setOnClickListener(logoutClickListener)
        logoutIcon.setOnClickListener(logoutClickListener)


        return view
    }
}
