package com.example.talkspace.ui

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.example.talkspace.R
import com.example.talkspace.databinding.FragmentMainBinding

class MainFragment : Fragment() {

    private var binding: FragmentMainBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val fragmentBinding= FragmentMainBinding.inflate(inflater, container, false)
        binding = fragmentBinding
        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("MainFragment","onViewCreated")


        binding?.bottomNavigation?.setOnItemSelectedListener { item ->
            when(item.itemId){
                R.id.item_chat ->{ parentFragmentManager.beginTransaction()
                    .replace(R.id.inner_container, ChatListFragment(),"")
                    .commit()
                    true
                }
                R.id.item_call ->{ parentFragmentManager.beginTransaction()
                    .replace(R.id.inner_container, CallHistoryFragment(),"")
                    .commit()
                    true
                }
                R.id.item_add ->{ parentFragmentManager.beginTransaction()
                    .replace(R.id.inner_container, AddContactFragment(),"")
                    .commit()
                    true
                }
                R.id.item_contact ->{ parentFragmentManager.beginTransaction()
                    .replace(R.id.inner_container, ContactFragment(),"")
                    .commit()
                    true
                }
                R.id.item_setting ->{ parentFragmentManager.beginTransaction()
                    .replace(R.id.inner_container, SettingFragment(),"")
                    .commit()
                    true
                }else -> {
                    false
            }
            }
        }
        Log.d("MainFragment","savedInstanceState : $savedInstanceState and " +
                "\n selectedItemId : ${binding?.bottomNavigation?.autofillId}")
        if(savedInstanceState == null || savedInstanceState.isEmpty){
            binding?.bottomNavigation?.selectedItemId = R.id.item_chat
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d("MainFragment","onStart")
//        if(savedInstanceState == null){

//            binding?.bottomNavigation?.selectedItemId = R.id.item_chat

//        }
    }

    override fun onStop() {
        super.onStop()
        Log.d("MainFragment","stop main fragment")

    }

    override fun onDestroy() {
        super.onDestroy()

        Log.d("MainFragment.Kt","Destroy main fragment")
        binding = null
    }
}