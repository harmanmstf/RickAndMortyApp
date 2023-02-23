package com.example.rickandmorty

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import coil.load
import com.example.rickandmorty.databinding.FragmentHomeBinding
import com.example.rickandmorty.network.Api
import com.google.gson.GsonBuilder
import kotlinx.coroutines.launch


enum class ApiStatus { LOADING, ERROR, DONE }

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val _character = MutableLiveData<CharacterModel?>(null)
    val character: MutableLiveData<CharacterModel?> = _character
    private val _status = MutableLiveData<ApiStatus>(ApiStatus.DONE)
    val status: LiveData<ApiStatus> = _status


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvData.text = "binding done"
        character.observe(viewLifecycleOwner) {

            it?.let { ch ->
                binding.tvData.text = ch.name
                val imgUri = it.image.toUri().buildUpon().scheme("https").build()
                binding.imgData.load(imgUri)
            }

        }

        status.observe(viewLifecycleOwner){
            when(it){
                ApiStatus.LOADING -> binding.pbData.isVisible= true
                ApiStatus.ERROR -> {
                    Toast.makeText(requireContext(), "hata", Toast.LENGTH_SHORT).show()
                    binding.pbData.isVisible= false
                }
                ApiStatus.DONE -> binding.pbData.isVisible= false
                else -> {}
            }
        }

        binding.btnSubmit.setOnClickListener {
            val userId = try {
                binding.etUserId.text.toString().toInt()
            } catch (ex: Exception) {
                Toast.makeText(requireContext(), "Varsayılan değer kullanıldı.", Toast.LENGTH_SHORT)
                    .show()
                1
            }

            getCharacterById(userId)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getCharacterById(id: Int) {

        lifecycleScope.launch {
            _status.value = ApiStatus.LOADING
            try {
                val jsonResult = Api.retrofitService.getCharacterById(id)
                _character.value =
                    GsonBuilder().create().fromJson(jsonResult, CharacterModel::class.java)
                _status.value = ApiStatus.DONE
            } catch (e: Exception) {
                _status.value = ApiStatus.ERROR
                _character.value = null

            }
        }
    }

}

data class CharacterModel(
    val name: String,
    val image: String,
    )