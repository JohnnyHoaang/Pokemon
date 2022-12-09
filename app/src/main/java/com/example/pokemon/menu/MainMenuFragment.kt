package com.example.pokemon.menu

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.example.pokemon.fight.FightActivity
import com.example.pokemon.R
import com.example.pokemon.data.PokemonCreation
import com.example.pokemon.database.*
import com.example.pokemon.databinding.FragmentMainMenuBinding
import com.example.pokemon.objects.MoveData
import com.example.pokemon.objects.Pokemon
import com.example.pokemon.objects.PokemonCollection
import com.example.pokemon.objects.PokemonTeam
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MainMenuFragment : Fragment() {
    lateinit var menuActivity: MenuActivity
    private val database by lazy { PokemonRoomDatabase.getDatabase(menuActivity)}
    private lateinit var pokemonTeam: PokemonTeam
    private lateinit var pokemonCollection: PokemonCollection
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        menuActivity = context as MenuActivity
        pokemonTeam = menuActivity.getTeam()
        pokemonCollection = menuActivity.getCollect()
        val binding = FragmentMainMenuBinding.inflate(layoutInflater)
        binding.goToTeam.setOnClickListener { view : View ->
            view.findNavController().navigate(R.id.action_mainMenuFragment_to_teamFragment)
        }
        binding.goToPokeCenter.setOnClickListener { view: View ->
            view.findNavController().navigate(R.id.action_mainMenuFragment_to_pokeCenterFragment)
        }
        binding.goToTrainerBattle.setOnClickListener { switchToBattle("trainer") }
        binding.goToWildBattle.setOnClickListener{ switchToBattle("wild") }
        binding.save.setOnClickListener {
            saveToDatabase()
            Toast.makeText(activity, "Game saved", Toast.LENGTH_SHORT).show()
        }
        return binding.root
    }
    private fun switchToBattle(battleType:String) {
        if(!menuActivity.getTeam().isTeamDead()){
            if(battleType == "wild"){
                Toast.makeText(activity, "Loading...", Toast.LENGTH_SHORT).show()
                lifecycleScope.launch(Dispatchers.IO) {
                    val wildPokemon = generatePokemon()
                    lifecycleScope.launch(Dispatchers.Main){
                        switchBattle(battleType, wildPokemon, null)
                    }
                }
            } else {
                Toast.makeText(activity, "Loading...", Toast.LENGTH_LONG).show()
                lifecycleScope.launch(Dispatchers.IO) {
                    val trainerTeam = generatePokemonTeam()
                    lifecycleScope.launch(Dispatchers.Main){
                        switchBattle(battleType, null, trainerTeam)
                    }
                }
            }
        } else {
            Toast.makeText(activity, "Your team is dead! Go to the Pokecenter.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun switchBattle(type:String, wildPokemon : Pokemon?, trainerTeam: PokemonTeam?){
        val intent = Intent(menuActivity, FightActivity::class.java)
        intent.putExtra("pokemonTeam", pokemonTeam)
        intent.putExtra("pokemonCollection", pokemonCollection)
        intent.putExtra("wildPokemon", wildPokemon)
        intent.putExtra("trainerTeam", trainerTeam)
        intent.putExtra("battleType", type)
        menuActivity.getResult.launch(intent)
    }

    private fun saveToDatabase() {
        lifecycleScope.launch(Dispatchers.IO){
            val pokemonTeam = pokemonTeam
            val pokemonCollection = pokemonCollection
            database.PokemonDAO().clearPlayerPokemons()
            database.PokemonDAO().clearPokemonWithMoves()
            database.PokemonDAO().clearPokemonWithCurrentMoves()
            var uniqueId = 0
            for(i in 0 until pokemonTeam.getSize()){
                val pokemon = pokemonTeam.getPokemon(i)
                val playerPokemon = createPlayerPokemon(pokemon, i, 1, uniqueId)
                database.PokemonDAO().insertPokemon(playerPokemon)
                addMovesToDB(pokemon.getMoves(), uniqueId)
                uniqueId++
            }
            for(i in 0 until pokemonCollection.getSize()){
                val pokemon = pokemonCollection.getPokemon(i)
                val playerPokemon = createPlayerPokemon(pokemon, i, 0, uniqueId)
                database.PokemonDAO().insertPokemon(playerPokemon)
                addMovesToDB(pokemon.getMoves(), uniqueId)
                uniqueId++
            }
        }
    }

    private fun addMovesToDB(movesData: ArrayList<MoveData>, id: Int) {
        for(i in 0 until movesData.size){
            val move = movesData[i].move
            val moveDB = Move(movesData[i].moveName, move.getAccuracy(), move.getPower(), move.getDamageClass(), move.getHeal(),move.getTarget(), move.getTypes())
            database.PokemonDAO().insertMove(moveDB)
            val pokemonWithMoves = PokemonWithMoves(id, movesData[i].moveName, movesData[i].level_learned_at)
            database.PokemonDAO().insertPokemonWithMoves(pokemonWithMoves)
        }
    }

    private fun createPlayerPokemon(pokemon: Pokemon, position: Int, isTeam: Int, uniqueId: Int) : PlayerPokemon {
        val images = pokemon.getImages()
        val imagesToJsonStr = Gson().toJson(images)
        val types = pokemon.getTypes()
        val movesToJsonStr = Gson().toJson(types)
        return PlayerPokemon(
            uniqueId,
            position,
            isTeam,
            pokemon.getId(),
            pokemon.getSpecies(),
            pokemon.getName(),
            imagesToJsonStr,
            pokemon.getExperience(),
            pokemon.getExperienceReward(),
            pokemon.getLevel(),
            movesToJsonStr,
            pokemon.getMaxHp(),
            pokemon.getCurrentHp(),
            pokemon.getAttack(),
            pokemon.getDefense(),
            pokemon.getSpecialAttack(),
            pokemon.getSpecialDefense(),
            pokemon.getSpeed()
        )
    }
    // Generate random pokemon based on team level
    private suspend fun generatePokemon() : Pokemon {
        val creator = PokemonCreation()
        val pokemonId = (1..151).random().toString()
        val level = if(pokemonTeam.getLowestLevel()>5){
            (pokemonTeam.getLowestLevel()-5..pokemonTeam.getHighestLevel()+5).random()
        } else {
            (pokemonTeam.getLowestLevel()..pokemonTeam.getHighestLevel()+5).random()
        }
        val pokemon = creator.createPokemon(pokemonId, "", level)
        creator.setURLToBitMapImages(pokemon)
        return pokemon
    }

    private suspend fun generatePokemonTeam(): PokemonTeam {
        val pokemonTeam = PokemonTeam()
        val pokemonCount = (0..5).random()
        for(i in 0 .. pokemonCount){
            pokemonTeam.addPokemonToTeam(generatePokemon())
        }
        return pokemonTeam
    }
}
