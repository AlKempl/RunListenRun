package com.alkempl.rlr.services

import com.alkempl.rlr.data.model.scenario.Scenario
import com.google.gson.Gson
import java.io.File


class ScenarioService {
    fun loadScenario(filename: String) : Scenario{
        val gson = Gson()
        val reader = File(filename).bufferedReader()
        return gson.fromJson(reader, Scenario::class.java)
    }
}