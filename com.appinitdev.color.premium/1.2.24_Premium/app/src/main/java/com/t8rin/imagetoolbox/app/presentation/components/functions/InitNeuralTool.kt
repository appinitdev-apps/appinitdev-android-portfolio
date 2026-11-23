 

internal fun ImageToolboxApplication.initNeuralTool() = NeuralTool.init(
    context = this,
    httpClient = httpClient,
    baseUrl = HF_BASE_URL
)