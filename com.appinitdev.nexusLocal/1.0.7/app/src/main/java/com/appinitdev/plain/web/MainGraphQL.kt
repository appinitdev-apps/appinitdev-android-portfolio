class MainGraphQL(val schema: Schema) {
    class Configuration : SchemaConfigurationDSL() {
        fun init() {
            schemaBlock = {
                addChatMessageSchema()
                addChatChannelSchema()
                addSmsSchema()
                addImageSchema()
                addAudioSchema()
                addVideoSchema()
                addMediaSchema()
                addDocSchema()
                addContactSchema()
                addCallSchema()
                addPackageSchema()
                addFileQuerySchema()
                addFileUploadSchema()
                addFileMutationSchema()
                addFeedSchema()
                addNoteSchema()
                addTagSchema()
                addScreenMirrorSchema()
                addPomodoroSchema()
                addNotificationSchema()
                addAppSchema()
                addAppFileSchema()
                addAppLogsSchema()
                addDataStoreSchema()
                addDbSchema()
                addBookmarkSchema()
                addPairingSchema()
                addPeerSchema()
                addSchemaTypes()
            }
        }
        internal var schemaBlock: (SchemaBuilder.() -> Unit)? = null
    }
    companion object Feature : BaseApplicationPlugin<Application, Configuration, MainGraphQL> {
        override val key = AttributeKey<MainGraphQL>("MainGraphQL")
        private suspend fun executeGraphqlQL(
            schema: Schema,
            query: String,
            call: ApplicationCall,
        ): String {
            val request = Json.decodeFromString(GraphqlRequest.serializer(), query)
            return schema.execute(
                request.query,
                request.variables?.toString(),
                context { +call },
            )
        }
        override fun install(
            pipeline: Application,
            configure: Configuration.() -> Unit,
        ): MainGraphQL {
            val config = Configuration().apply(configure)
            val schema =
                KGraphQL.schema {
                    configuration = config
                    config.schemaBlock?.invoke(this)
                }
            pipeline.routing {
                route("/graphql") {
                    post {
                        if (!TempData.webEnabled.value) {
                            call.respond(HttpStatusCode.Forbidden)
                            return@post
                        }
                        val clientId = call.request.header("c-id") ?: ""
                        if (clientId.isNotEmpty()) {
                            val token = HttpServerManager.tokenCache[clientId]
                            if (token == null) {
                                call.respond(HttpStatusCode.Unauthorized)
                                return@post
                            }
                            val decryptedBytes = CryptoHelper.chaCha20Decrypt(token, call.receive())
                            val decryptedStr = decryptedBytes?.decodeToString() ?: ""
                            if (decryptedStr.isEmpty()) {
                                call.respond(HttpStatusCode.Unauthorized)
                                return@post
                            }
                            val parsed = ReplayGuard.parse(decryptedStr)
                            if (parsed == null) {
                                call.respond(HttpStatusCode.BadRequest)
                                return@post
                            }
                            val err = ReplayGuard.validate(clientId, parsed)
                            if (err != null) {
                                call.respond(HttpStatusCode.BadRequest)
                                return@post
                            }
                            HttpServerManager.clientRequestTs[clientId] = System.currentTimeMillis()
                            sendEvent(WebRequestReceivedEvent())
                            val r = executeGraphqlQL(schema, parsed.body, call)
                            call.respondBytes(CryptoHelper.chaCha20Encrypt(token, r))
                        } else {
                            if (clientId.isEmpty()) {
                                call.respondText(
                                    """{"errors":[{"message":"Unauthorized"}]}""",
                                    contentType = ContentType.Application.Json,
                                )
                                return@post
                            }
                            val authStr = call.request.header("authorization")?.split(" ")
                            val bearerToken = authStr?.getOrNull(1) ?: ""
                            val session = SessionList.getByClientIdAsync(clientId)
                            if (
                                bearerToken.isEmpty() ||
                                session == null ||
                                session.type != DSession.TYPE_CUSTOM ||
                                session.token != bearerToken
                            ) {
                                call.respondText(
                                    """{"errors":[{"message":"Unauthorized"}]}""",
                                    contentType = ContentType.Application.Json,
                                )
                                return@post
                            }
                            val requestStr = call.receiveText()
                            LogCat.d("[Request] $requestStr")
                            HttpServerManager.clientRequestTs[clientId] = System.currentTimeMillis() 
                            sendEvent(WebRequestReceivedEvent())
                            val r = executeGraphqlQL(schema, requestStr, call)
                            call.respondText(r, contentType = ContentType.Application.Json)
                        }
                    }
                }
            }
            pipeline.intercept(ApplicationCallPipeline.Monitoring) {
                try {
                    coroutineScope {
                        proceed()
                    }
                } catch (e: Throwable) {
                    if (e is GraphQLError) {
                        val clientId = call.request.header("c-id") ?: ""
                        val type = call.request.header("c-type") ?: "" 
                        val channelId = call.request.header("c-cid") ?: "" 
                        if (clientId.isNotEmpty()) {
                            val token = if (channelId.isNotEmpty()) {
                                ChatCacheManager.channelKeyCache[channelId]
                            } else if (type == "peer") {
                                ChatCacheManager.peerKeyCache[channelId]
                            } else {
                                HttpServerManager.tokenCache[clientId]
                            }
                            if (token != null) {
                                call.respondBytes(CryptoHelper.chaCha20Encrypt(token, e.serialize()))
                            } else {
                                call.respond(HttpStatusCode.Unauthorized)
                            }
                        } else {
                            context.respond(HttpStatusCode.OK, e.serialize())
                        }
                    } else {
                        throw e
                    }
                }
            }
            return MainGraphQL(schema)
        }
    }
}
