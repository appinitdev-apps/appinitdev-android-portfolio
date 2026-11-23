open class NotNullLiveData<T>(value: T) : MutableLiveData<T>(value) {
    override fun getValue(): T {
        return requireNotNull(super.getValue())
    }
}
