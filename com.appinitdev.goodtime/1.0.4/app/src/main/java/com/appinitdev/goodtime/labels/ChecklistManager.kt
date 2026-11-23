data class TaskItem(
    val id: String,
    val title: String,
    val isCompleted: Boolean = false,
    val label: String
)
class ChecklistManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("goodtime_checklists", Context.MODE_PRIVATE)
    private val _tasks = MutableStateFlow<List<TaskItem>>(emptyList())
    val tasks: StateFlow<List<TaskItem>> = _tasks.asStateFlow()
    init {
        loadTasks()
    }
    fun getTasksForLabel(label: String): List<TaskItem> {
        return _tasks.value.filter { it.label == label }
    }
    fun getPendingTasksCount(label: String): Int {
        return _tasks.value.count { it.label == label && !it.isCompleted }
    }
    fun getCompletedTasksCount(label: String): Int {
        return _tasks.value.count { it.label == label && it.isCompleted }
    }
    fun getCompletionPercentage(label: String): Int {
        val labelTasks = getTasksForLabel(label)
        if (labelTasks.isEmpty()) return 0
        val completed = labelTasks.count { it.isCompleted }
        return (completed * 100) / labelTasks.size
    }
    fun importSessionSubtasks(label: String, titles: List<String>) {
        val newItems = titles.filter { it.isNotBlank() }.map {
            TaskItem(
                id = System.currentTimeMillis().toString() + "_" + (0..999).random(),
                title = it.trim(),
                isCompleted = false,
                label = label
            )
        }
        val updated = _tasks.value + newItems
        _tasks.value = updated
        saveTasks(updated)
    }
    fun addTask(title: String, label: String) {
        if (title.isBlank()) return
        val newTask = TaskItem(
            id = System.currentTimeMillis().toString(),
            title = title.trim(),
            isCompleted = false,
            label = label
        )
        val updated = _tasks.value + newTask
        _tasks.value = updated
        saveTasks(updated)
    }
    fun toggleTask(taskId: String) {
        val updated = _tasks.value.map { task ->
            if (task.id == taskId) {
                task.copy(isCompleted = !task.isCompleted)
            } else {
                task
            }
        }
        _tasks.value = updated
        saveTasks(updated)
    }
    fun deleteTask(taskId: String) {
        val updated = _tasks.value.filterNot { it.id == taskId }
        _tasks.value = updated
        saveTasks(updated)
    }
    fun clearCompleted(label: String) {
        val updated = _tasks.value.filterNot { it.label == label && it.isCompleted }
        _tasks.value = updated
        saveTasks(updated)
    }
    private fun loadTasks() {
        val rawJson = prefs.getString(KEY_TASKS_JSON, null) ?: return
        try {
            val jsonArray = JSONArray(rawJson)
            val list = mutableListOf<TaskItem>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    TaskItem(
                        id = obj.getString("id"),
                        title = obj.getString("title"),
                        isCompleted = obj.getBoolean("isCompleted"),
                        label = obj.optString("label", "unlabeled")
                    )
                )
            }
            _tasks.value = list
        } catch (_: Exception) {
        }
    }
    private fun saveTasks(list: List<TaskItem>) {
        try {
            val jsonArray = JSONArray()
            for (item in list) {
                val obj = JSONObject().apply {
                    put("id", item.id)
                    put("title", item.title)
                    put("isCompleted", item.isCompleted)
                    put("label", item.label)
                }
                jsonArray.put(obj)
            }
            prefs.edit().putString(KEY_TASKS_JSON, jsonArray.toString()).apply()
        } catch (_: Exception) {
        }
    }
    companion object {
        private const val KEY_TASKS_JSON = "tasks_json_v1"
    }
}
