    package com.example.taskconnectapp

    import android.content.Context
    import android.os.Bundle
    import android.util.Log
    import android.view.LayoutInflater
    import android.view.View
    import android.view.ViewGroup
    import android.widget.Button
    import android.widget.ImageView
    import android.widget.TextView
    import androidx.fragment.app.Fragment
    import com.bumptech.glide.Glide
    import okhttp3.*
    import org.json.JSONObject
    import java.io.IOException
    import android.content.Intent

    class ProfileFragment : Fragment() {
        private lateinit var ivUserProfile: ImageView
        private lateinit var tvUserName: TextView
        private lateinit var tvJoinedDate: TextView
        private lateinit var tvUserLocation: TextView
        private lateinit var tvUserPhoneNumber: TextView
        private lateinit var logoutButton: Button
        private lateinit var registrationButtonn: Button

        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            val view = inflater.inflate(R.layout.fragment_citizen_profile_page, container, false)

            // Find UI elements
            ivUserProfile = view.findViewById(R.id.ivUserProfile)
            tvUserName = view.findViewById(R.id.TvUserNameValue)
            tvJoinedDate = view.findViewById(R.id.joinedDate)
            tvUserLocation = view.findViewById(R.id.UserLocation)
            tvUserPhoneNumber = view.findViewById(R.id.UserPhoneNumber)
            registrationButtonn = view.findViewById(R.id.Register_Button)
            logoutButton = view.findViewById(R.id.logoutbtn)

            // Fetch user ID from SharedPreferences
            val sharedPref = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            val userId = sharedPref.getInt("users_id", 0) // Match the login key

            if (userId > 0) {
                fetchUserProfile(userId)
            } else {
                Log.e("ProfileFragment", "Invalid user ID")
            }

            // Logout button functionality
            logoutButton.setOnClickListener {
                sharedPref.edit().clear().apply()

                val intent = Intent(requireActivity(), LoginPage::class.java)
                startActivity(intent)
                requireActivity().finish() // Finish current activity
            }

            registrationButtonn.setOnClickListener {
                val intent = Intent(requireActivity(), RegistrationFormActivity::class.java)
                startActivity(intent)
            }


            return view
        }

        private fun fetchUserProfile(userId: Int) {
            val url = "http://10.0.2.2/taskconnect/get_user_profile.php?users_id=$userId"

            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("ProfileFragment", "Failed to fetch profile: ${e.message}")
                }

                override fun onResponse(call: Call, response: Response) {
                    val jsonString = response.body?.string()
                    Log.d("ProfileFragment", "Response: $jsonString") // LOG RESPONSE

                    try {
                        val jsonObject = JSONObject(jsonString)

                        requireActivity().runOnUiThread {
                            if (jsonObject.getBoolean("success")) {
                                val user = jsonObject.getJSONObject("user")

                                val firstName = user.optString("first_name", "Unknown")
                                val lastName = user.optString("last_name", "")
                                val profileImage = user.optString("profile_image", "")
                                val joinedDate = user.optString("created_at", "Unknown")
                                val rawLocation = user.optString("location", "N/A")
                                val location = if (rawLocation == "null") "N/A" else rawLocation
                                val phoneNumber = user.optString("mobile_no", "Not Available")

                                // **UPDATE UI WITH DATA**
                                tvUserName.text = "$firstName $lastName"
                                tvJoinedDate.text = "Joined $joinedDate"
                                tvUserLocation.text = location
                                tvUserPhoneNumber.text = phoneNumber

                                if (profileImage.isNotEmpty()) {
                                    Glide.with(requireContext()).load(profileImage).into(ivUserProfile)
                                } else {
                                    ivUserProfile.setImageResource(R.drawable.user_profile) // Default image
                                }
                            } else {
                                val errorMessage = jsonObject.optString("message", "Unknown error")
                                Log.e("ProfileFragment", "Error: $errorMessage")
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("ProfileFragment", "JSON Parsing error: ${e.message}")
                    }
                }
            })
        }
    }
