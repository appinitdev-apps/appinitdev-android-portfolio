class PeerGraphQL(val schema: Schema) {
    class Configuration : SchemaConfigurationDSL() {
        @SuppressLint("MissingPermission")
        fun init() {
            schemaBlock = {
                type<ChatItem> {
                    property("data") {
                        resolver { c: ChatItem ->
                            c.getContentData()
                        }
                    }
                }
                mutation("channelSystemMessage") {
                    resolver { type: String, payload: String, context: Context ->
                        val call = context.get<ApplicationCall>()!!
                        val fromId = call.request.header("c-id") ?: ""
                        ChannelSystemMessageHandler.handle(fromId, type, payload)
                        true
                    }
                }
                mutation("createChatItem") {
                    resolver { content: String, context: Context ->
                        val call = context.get<ApplicationCall>()!!
                        val fromPeerId = call.request.header("c-id") ?: ""
                        val fromChannelId = call.request.header("c-cid") ?: ""
                        val signature = call.attributes.getOrNull(SignatureAttrKey) ?: ""
                        val timestamp = call.attributes.getOrNull(TimestampAttrKey) ?: 0L
                        val item = try {
                            ChatMessageReceiver.receive(
                                fromPeerId = fromPeerId,
                                content = DChat.parseContent(content),
                                fromChannelId = fromChannelId,
                                signature = signature,
                                timestamp = timestamp,
                            )
                        } catch (e: ReplayedMessageException) {
                            LogCat.d("Dropped replayed message from $fromPeerId")
                            return@resolver emptyList<ChatItem>()
                        }
                        listOf(item.toModel())
                    }
                }
                stringScalar<Instant> {
                    deserialize = { value: String -> Instant.parse(value) }
                    serialize = Instant::toString
                }
                stringScalar<ID> {
                    deserialize = { it: String -> ID(it) }
                    serialize = { it: ID -> it.toString() }
                }
            }
        }
        internal var schemaBlock: (SchemaBuilder.() -> Unit)? = null
    }
    companion object Feature : BaseApplicationPlugin<Application, Configuration, PeerGraphQL> {
        override val key = AttributeKey<PeerGraphQL>("PeerGraphQL")
        private val SignatureAttrKey = AttributeKey<String>("PeerGraphQL.Signature")
        private val TimestampAttrKey = AttributeKey<Long>("PeerGraphQL.Timestamp")
        private suspend fun executeGraphqlQL(
            schema: Schema,
            query: String,
            call: ApplicationCall
        ): String {
            val request = Json.decodeFromString(GraphqlRequest.serializer(), query)
            return schema.execute(request.query, request.variables?.toString(), context {
                +call
            })
        }
        override fun install(
            pipeline: Application,
            configure: Configuration.() -> Unit,
        ): PeerGraphQL {
            val config = Configuration().apply(configure)
            val schema =
                KGraphQL.schema {
                    configuration = config
                    config.schemaBlock?.invoke(this)
                }
            pipeline.routing {
                route("/peer_graphql") {
                    post {
                        if (!TempData.webEnabled.value) {
                            call.respond(HttpStatusCode.Forbidden)
                            return@post
                        }
                        val clientId = call.request.header("c-id") ?: ""
                        val channelId = call.request.header("c-cid") ?: ""
                        val token = if (channelId.isNotEmpty()) {
                            ChatCacheManager.channelKeyCache[channelId]
                        } else {
                            ChatCacheManager.peerKeyCache[clientId]
                        }
                        val publicKey = ChatCacheManager.peerPublicKeyCache[clientId]
                        if (token == null || publicKey == null) {
                            call.respond(HttpStatusCode.Unauthorized)
                            return@post
                        }
                        val decryptResult = PeerChatParser.decrypt(token, clientId, publicKey, call.receive())
                        if (decryptResult.content == null) {
                            call.respond(decryptResult.code)
                            return@post
                        }
                        call.attributes.put(SignatureAttrKey, decryptResult.signature)
                        call.attributes.put(TimestampAttrKey, decryptResult.timestamp)
                        val r = executeGraphqlQL(schema, decryptResult.content, call)
                        call.respondBytes(CryptoHelper.chaCha20Encrypt(token, r))
                    }
                }
            }
            return PeerGraphQL(schema)
        }
    }
}
