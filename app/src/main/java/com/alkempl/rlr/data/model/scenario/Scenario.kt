import com.alkempl.rlr.data.model.scenario.ScenarioChapter
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

data class Scenario(
    @Json(name = "chapters")
    val chapters: List<ScenarioChapter>?,
    @Json(name = "description")
    val description: String, // It's a really demo
    @Json(name = "id")
    val id: String, // 436207e3-0f06-4c5b-9b88-74ba456ede32
    @Json(name = "name")
    val name: String, // Demo scenario
)