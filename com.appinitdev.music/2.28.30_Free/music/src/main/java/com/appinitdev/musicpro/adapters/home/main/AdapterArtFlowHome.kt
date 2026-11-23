
 








class AdapterArtFlowHome(private var data: List<ArtFlowData<Any>>) : RecyclerView.Adapter<VerticalListViewHolder>() {

    private var adapterArtFlowHomeCallbacks: AdapterArtFlowHomeCallbacks? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerticalListViewHolder {
        return Holder(AdapterHomeArtflowBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: VerticalListViewHolder, @SuppressLint("RecyclerView") position: Int) {
        if (holder is Holder) {
            val item = data[position]
            val sliderAdapter = SliderAdapter(item)

            holder.binding.title.text = holder.binding.title.context.getString(item.title)

            if (item.title == R.string.recommended) {
                holder.binding.title.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0)
            } else {
                holder.binding.title.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_for_arrow_12dp, 0)
            }

            holder.binding.felicitySlider.setAdapter(sliderAdapter)
            holder.binding.felicitySlider.start()

            sliderAdapter.setOnItemClickListener { itemPosition, imageView ->
                item.position = itemPosition
                adapterArtFlowHomeCallbacks?.onItemClicked(imageView, position, itemPosition)
            }

            sliderAdapter.setOnItemLongClickListener { itemPosition, imageView ->
                item.position = itemPosition
                adapterArtFlowHomeCallbacks?.onItemLongClicked(imageView, position, itemPosition)
            }

            if (item.position >= 0) {
                holder.binding.felicitySlider.setCurrentItem(item.position, smoothScroll = false)
                holder.binding.container.transitionName = item.items[item.position].toString()
            }

            holder.binding.title.setOnClickListener {
                adapterArtFlowHomeCallbacks?.onPanelItemClicked(item.title, it)
            }

            holder.binding.container.setOnClickListener {
                item.position = holder.binding.felicitySlider.getCurrentItem()
                adapterArtFlowHomeCallbacks?.onClicked(holder.binding.container, position)
            }
        }
    }

    override fun onViewRecycled(holder: VerticalListViewHolder) {
        super.onViewRecycled(holder)
        if (holder is Holder) {
            holder.binding.felicitySlider.stop()
        }
    }

    inner class Holder(val binding: AdapterHomeArtflowBinding) : VerticalListViewHolder(binding.root)

     








    private inner class SliderAdapter(private val data: ArtFlowData<Any>) : FelicityPager.PageAdapter {

        private var onItemClick: ((position: Int, imageView: ImageView) -> Unit)? = null
        private var onItemLongClick: ((position: Int, imageView: ImageView) -> Unit)? = null

        override fun getCount(): Int = data.items.size.coerceAtMost(12)

        override fun getItemId(position: Int): Long = position.toLong()

        override fun onCreateView(position: Int, parent: ViewGroup): View {
            return ImageView(parent.context).apply {
                scaleType = ImageView.ScaleType.CENTER_CROP
                layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
        }

        override fun onBindView(position: Int, view: View) {
            val iv = view as ImageView
            if (data.items.isNotEmpty()) {
                iv.loadArtCover(
                        item = data.items[position],
                        roundedCorners = false,
                        blur = false,
                        crop = true
                )
            }
            iv.setOnClickListener { onItemClick?.invoke(position, iv) }
            iv.setOnLongClickListener { onItemLongClick?.invoke(position, iv); true }
        }

        override fun onRecycleView(position: Int, view: View) {
            try {
                val iv = view as ImageView
                Glide.with(iv.context).clear(iv)
            } catch (e: IllegalArgumentException) {
                
                e.printStackTrace()
            }
        }

        fun setOnItemClickListener(listener: (position: Int, imageView: ImageView) -> Unit) {
            onItemClick = listener
        }

        fun setOnItemLongClickListener(listener: (position: Int, imageView: ImageView) -> Unit) {
            onItemLongClick = listener
        }
    }

     








    fun updateData(newData: List<ArtFlowData<Any>>) {
        val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = data.size
            override fun getNewListSize() = newData.size
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                data[oldItemPosition].title == newData[newItemPosition].title

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                data[oldItemPosition].items == newData[newItemPosition].items
        })
        data = newData
        diff.dispatchUpdatesTo(this)
    }

     







    fun getSection(rowPosition: Int): ArtFlowData<Any>? = data.getOrNull(rowPosition)

     




    fun setAdapterArtFlowHomeCallbacks(adapterArtFlowHomeCallbacks: AdapterArtFlowHomeCallbacks) {
        this.adapterArtFlowHomeCallbacks = adapterArtFlowHomeCallbacks
    }

    companion object {
        interface AdapterArtFlowHomeCallbacks : GeneralAdapterCallbacks {
             






            fun onItemClicked(imageView: ImageView, rowPosition: Int, itemPosition: Int)

             







            fun onItemLongClicked(imageView: ImageView, rowPosition: Int, itemPosition: Int)

            fun onClicked(view: View, position: Int, itemPosition: Int)
            fun onClicked(view: View, position: Int)
        }
    }
}
