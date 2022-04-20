package com.alkempl.rlr.services

import Scenario
import com.alkempl.rlr.R
import com.google.gson.Gson
import java.io.File


class ScenarioService {
    fun loadScenario(filename: String) : Scenario{
        val gson = Gson()
        val reader = File(filename).bufferedReader()
        return gson.fromJson(reader, Scenario::class.java)
    }
}