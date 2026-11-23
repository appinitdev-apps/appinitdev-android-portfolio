
fun SchemaBuilder.addPeerSchema() {
    query("peers") {
        resolver { ->
            AppDatabase.instance.peerDao().getAll().map { it.toModel() }
        }
    }
    mutation("deletePeer") {
        resolver { id: ID ->
            PeerManager.deletePeer(MainApp.instance, id.value)
            true
        }
    }
    type<Peer> {}
}
