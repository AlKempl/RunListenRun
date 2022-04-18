import com.google.gson.annotations.SerializedName

data class Scenario(
    @SerializedName("chapters")
    val chapters: List<Chapter>,
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
) {
    data class Chapter(
        @SerializedName("audio")
        val audio: Audio,
        @SerializedName("description")
        val description: String, // The One Where Monica Gets a Roommate
        @SerializedName("events")
        val events: List<Event>,
        @SerializedName("id")
        val id: String, // ca1b1ed4-ef6d-49e7-9e7f-bfafa29281d4
        @SerializedName("is_final")
        val isFinal: Boolean, // false
        @SerializedName("name")
        val name: String // Chapter 1
    ) {
        data class Audio(
            @SerializedName("music")
            val music: List<String>,
            @SerializedName("radio")
            val radio: List<String>
        )

        data class Event(
            @SerializedName("action")
            val action: List<ScenarioAction>,
            @SerializedName("actions")
            val actions: List<ScenarioAction>,
            @SerializedName("id")
            val id: String, // ca1b1ed4-ef6d-49e7-9e7f-bfafa29281d4
            @SerializedName("time")
            val time: Any?, // null
            @SerializedName("type")
            val type: String // Random
        ) {
            data class ScenarioAction(
                @SerializedName("action")
                val action: String, // give_item
                @SerializedName("count")
                val count: Int, // 1
                @SerializedName("delay")
                val delay: Int, // 0
                @SerializedName("item")
                val item: String, // res/diamond_pickaxe
                @SerializedName("soundtrack")
                val soundtrack: String // res/item_found.mp3
            )

            data class Action(
                @SerializedName("action")
                val action: String, // play_sound
                @SerializedName("delay")
                val delay: Int, // 0
                @SerializedName("obstacle")
                val obstacle: String, // dogs
                @SerializedName("severity")
                val severity: Int, // 3
                @SerializedName("soundtrack")
                val soundtrack: String // res/dat1.mp3
            )
        }
    }
}