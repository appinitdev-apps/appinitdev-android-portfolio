enum class Accelerator { CPU, GPU, NPU }
class TensorBuffer {
    fun writeFloat(data: FloatArray) {}
    fun writeLong(data: LongArray) {}
    fun readFloat(): FloatArray = FloatArray(0)
}
class CompiledModel private constructor() {
    class Options(accelerator: Accelerator)
    companion object {
        fun create(modelPath: String, options: Options): CompiledModel = CompiledModel()
    }
    fun createInputBuffers(): List<TensorBuffer> = emptyList()
    fun createOutputBuffers(): List<TensorBuffer> = emptyList()
    fun run(inputs: List<TensorBuffer>, outputs: List<TensorBuffer>) {}
    fun close() {}
}
