package com.example.pokemon.fight

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.example.pokemon.MenuActivity
import com.example.pokemon.R
import com.example.pokemon.databinding.FragmentFightMenuBinding
import com.example.pokemon.menu.MenuActivity


class FightMenuFragment : Fragment()  {
    lateinit var fightActivity: FightActivity
    lateinit var menuActivity: MenuActivity
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fightActivity = context as FightActivity
        val binding = FragmentFightMenuBinding.inflate(layoutInflater)
        binding.selectFight.setOnClickListener { view : View ->
            view.findNavController().navigate(R.id.action_fightMenuFragment_to_fightFragment)
        }
        binding.selectBag.setOnClickListener { view: View ->
            view.findNavController().navigate(R.id.action_fightMenuFragment_to_bagFragment)
        }
        binding.selectTeam.setOnClickListener { view: View ->
            view.findNavController().navigate(R.id.action_fightMenuFragment_to_fightPokemonTeamFragment)
        }
        binding.selectRun.setOnClickListener{
            fightActivity.getBattle().run()
        }
        return binding.root
    }
}