
open class KeyboardScopedFragment : ScopedFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.addHeightKeyboardCallbacks()
    }

     




    protected fun View.addHeightKeyboardCallbacks() {
         








        @Suppress("UNUSED_VARIABLE") val deferringInsetsListener = RootViewDeferringInsetsCallback(
                persistentInsetTypes = WindowInsetsCompat.Type.systemBars(),
                deferredInsetTypes = WindowInsetsCompat.Type.ime()
        )

         














        ViewCompat.setWindowInsetsAnimationCallback(
                this,
                HeightDeferringInsetsAnimationCallback(
                        view = this,
                        persistentInsetTypes = WindowInsetsCompat.Type.systemBars(),
                        deferredInsetTypes = WindowInsetsCompat.Type.ime(),
                        
                        
                        dispatchMode = WindowInsetsAnimationCompat.Callback.DISPATCH_MODE_CONTINUE_ON_SUBTREE
                )
        )
        ViewCompat.setWindowInsetsAnimationCallback(
                this,
                HeightDeferringInsetsAnimationCallback(
                        view = this,
                        persistentInsetTypes = WindowInsetsCompat.Type.systemBars(),
                        deferredInsetTypes = WindowInsetsCompat.Type.ime()
                )
        )
    }

     



    protected fun View.addTranslateKeyboardCallbacks() {
         








        @Suppress("UNUSED_VARIABLE") val deferringInsetsListener = RootViewDeferringInsetsCallback(
                persistentInsetTypes = WindowInsetsCompat.Type.systemBars(),
                deferredInsetTypes = WindowInsetsCompat.Type.ime()
        )

         














        ViewCompat.setWindowInsetsAnimationCallback(
                this,
                HeightDeferringInsetsAnimationCallback(
                        view = this,
                        persistentInsetTypes = WindowInsetsCompat.Type.systemBars(),
                        deferredInsetTypes = WindowInsetsCompat.Type.ime(),
                        
                        
                        dispatchMode = WindowInsetsAnimationCompat.Callback.DISPATCH_MODE_CONTINUE_ON_SUBTREE
                )
        )
        ViewCompat.setWindowInsetsAnimationCallback(
                this,
                HeightDeferringInsetsAnimationCallback(
                        view = this,
                        persistentInsetTypes = WindowInsetsCompat.Type.systemBars(),
                        deferredInsetTypes = WindowInsetsCompat.Type.ime()
                )
        )
    }

    protected fun EditText.setWindowInsetsAnimationCallback() {
         












        ViewCompat.setWindowInsetsAnimationCallback(this, ControlFocusInsetsAnimationCallback(this)
        )
    }
}