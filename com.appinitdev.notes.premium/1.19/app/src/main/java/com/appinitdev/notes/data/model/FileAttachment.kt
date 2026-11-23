
@Parcelize
data class FileAttachment(var localName: String, var originalName: String, var mimeType: String) :
    Attachment
