data class GraphQLResponse(
    val data: JSONObject? = null,
    val errors: List<GraphQLError>? = null,
    val isSuccess: Boolean = false
)
data class GraphQLError(
    val message: String
)
object PeerGraphQLClient {
    suspend fun createChatItem(
        peer: DPeer,
        clientId: String,
        content: DMessageContent,
    ): GraphQLResponse? {
        val mutation = $$"""
                mutation CreateChatItem($content: String!) {
                    createChatItem(content: $content) {
                        id
                        fromId
                        toId
                        createdAt
                    }
                }
            """.trimIndent()
        return execute(
            peer = peer,
            clientId = clientId,
            query = mutation,
            variables = mapOf("content" to content.toJSONString())
        )
    }
    suspend fun sendChannelSystemMessage(
        peer: DPeer,
        clientId: String,
        type: String,
        payload: String,
        channelId: String = "",
        channelKey: String = "",
    ): GraphQLResponse? {
        val mutation = $$"""
                mutation ChannelSystemMessage($type: String!, $payload: String!) {
                    channelSystemMessage(type: $type, payload: $payload)
                }
            """.trimIndent()
        val variables = mapOf(
            "type" to type,
            "payload" to payload,
        )
        val useChannelKey = peer.key.isEmpty() && channelKey.isNotEmpty()
        return if (useChannelKey) {
            execute(
                peer = peer,
                encryptionKey = channelKey,
                clientId = clientId,
                query = mutation,
                variables = variables,
                channelId = channelId,
            )
        } else {
            execute(peer = peer, clientId = clientId, query = mutation, variables = variables)
        }
    }
    suspend fun createChannelChatItem(
        peer: DPeer,
        channelId: String,
        channelKey: String,
        clientId: String,
        content: DMessageContent,
    ): GraphQLResponse? {
        val mutation = $$"""
                mutation CreateChatItem($content: String!) {
                    createChatItem(content: $content) {
                        id
                        fromId
                        toId
                        createdAt
                    }
                }
            """.trimIndent()
        return execute(
            peer = peer,
            encryptionKey = channelKey,
            clientId = clientId,
            query = mutation,
            variables = mapOf("content" to content.toJSONString()),
            channelId = channelId,
        )
    }
    private suspend fun execute(
        peer: DPeer,
        encryptionKey: String = peer.key,
        clientId: String,
        query: String,
        variables: Map<String, String>,
        channelId: String = "",
    ): GraphQLResponse? {
        return try {
            val requestJson = JSONObject().apply {
                put("query", query)
                val variablesJson = JSONObject()
                variables.forEach { (key, value) ->
                    variablesJson.put(key, value)
                }
                put("variables", variablesJson)
            }.toString()
            val timestamp = System.currentTimeMillis().toString()
            val signature = SignatureHelper.signTextAsync("$timestamp$requestJson")
            val requestBody = "$signature|$timestamp|$requestJson".toRequestBody("application/json".toMediaType())
            val requestBuilder = Request.Builder()
                .url(peer.getApiUrl())
                .post(requestBody)
                .addHeader("c-id", clientId)
            if (channelId.isNotEmpty()) {
                requestBuilder.addHeader("c-cid", channelId)
            }
            val httpClient = HttpClientManager.createCryptoHttpClient(encryptionKey, 10)
            val response = httpClient.newCall(requestBuilder.build()).execute()
            val responseBody = response.body.string()
            if (response.isSuccessful) {
                parseGraphQLResponse(responseBody)
            } else {
                LogCat.e("GraphQL request failed: ${response.code} - $responseBody")
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    private fun parseGraphQLResponse(responseBody: String): GraphQLResponse? {
        return try {
            val json = JSONObject(responseBody)
            val data = if (json.has("data") && !json.isNull("data")) {
                json.getJSONObject("data")
            } else {
                null
            }
            val errors = if (json.has("errors")) {
                val errorsArray = json.getJSONArray("errors")
                (0 until errorsArray.length()).map { i ->
                    val errorObj = errorsArray.getJSONObject(i)
                    GraphQLError(
                        message = errorObj.getString("message")
                    )
                }
            } else {
                null
            }
            GraphQLResponse(data = data, errors = errors, isSuccess = errors.isNullOrEmpty())
        } catch (e: Exception) {
            LogCat.e("Failed to parse GraphQL response: ${e.message}")
            null
        }
    }
} 