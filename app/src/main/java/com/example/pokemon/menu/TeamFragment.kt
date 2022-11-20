package com.example.pokemon.menu

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pokemon.R
import com.example.pokemon.databinding.FragmentTeamBinding


class TeamFragment : Fragment() {
    lateinit var menuActivity: MenuActivity

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        menuActivity = context as MenuActivity
        val binding = FragmentTeamBinding.inflate(layoutInflater)
        val team = menuActivity.getTeam()

        if(team.getSize() >= 6){
            binding.addTeam.visibility = View.GONE;
        } else {
            binding.addTeam.visibility = View.VISIBLE;
        }
        binding.addTeam.setOnClickListener { view : View ->
            view.findNavController().navigate(R.id.action_teamFragment_to_teamAddPokemonFragment)
        }

        if(team.getSize() == 0){
            binding.removeTeam.visibility = View.GONE
        } else {
            binding.removeTeam.visibility = View.VISIBLE
        }
        binding.removeTeam.setOnClickListener { view : View ->
            view.findNavController().navigate(R.id.action_teamFragment_to_teamRemovePokemonFragment)
        }

        binding.viewCollection.setOnClickListener { view : View ->
            view.findNavController().navigate(R.id.action_teamFragment_to_viewCollectionFragment)
        }

        binding.changeOrder.setOnClickListener { view : View ->
            view.findNavController().navigate(R.id.action_teamFragment_to_changeTeamOrderFragment)
        }

        binding.TeamGoToMainMenu.setOnClickListener { view : View ->
            view.findNavController().navigate(R.id.action_teamFragment_to_mainMenuFragment)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val pokemonTeam = menuActivity.getTeam()
        val recyclerView = view.findViewById<RecyclerView>(R.id.pokemon_team_recycler_view)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(activity)
            recyclerView.adapter = PokemonTeamAdapter(pokemonTeam, view.context)
        }
    }
}