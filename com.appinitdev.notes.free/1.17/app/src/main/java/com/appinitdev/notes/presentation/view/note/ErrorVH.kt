class ErrorVH(private val binding: ErrorBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(error: FileError) {
        binding.Name.text = error.name
        binding.Description.text = error.description
    }
}
