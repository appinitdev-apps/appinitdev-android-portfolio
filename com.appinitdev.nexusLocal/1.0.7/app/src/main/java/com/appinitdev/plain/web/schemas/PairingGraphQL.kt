fun SchemaBuilder.addPairingSchema() {
    mutation("respondToPairing") {
        resolver { input: PairingRequestInput, accepted: Boolean ->
            NearbyPairManager.respondToPairing(input.toModel(), accepted)
            true
        }
    }
}
