class RenderScriptProvider(context: Context) {
    val renderScript: RenderScript by lazy(LazyThreadSafetyMode.NONE) {
        RenderScript.create(context)
    }
    val blurScript: ScriptIntrinsicBlur by lazy(LazyThreadSafetyMode.NONE) {
        ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript))
    }
}