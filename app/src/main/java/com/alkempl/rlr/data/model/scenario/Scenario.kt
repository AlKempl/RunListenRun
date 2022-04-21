import com.alkempl.rlr.data.model.scenario.ScenarioChapter
import com.google.gson.annotations.SerializedName

data class Scenario(
    @SerializedName("chapters")
    val chapters: List<ScenarioChapter>,
    @SerializedName("description")
    val description: String, // It's a really demo
    @SerializedName("id")
    val id: String, // 436207e3-0f06-4c5b-9b88-74ba456ede32
    @SerializedName("name")
    val name: String, // Demo scenario
    @SerializedName("next_id")
    val nextId: Any?, // null
    @SerializedName("prev_id")
    val prevId: Any? // null
)