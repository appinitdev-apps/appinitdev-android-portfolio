class MdnsIfaceSelectorTest {
    @Test fun `DatagramSocket delivers bytes to loopback`() {
        val loopback = ip4("127.0.0.1")
        val payload = byteArrayOf(0xDE.toByte(), 0xAD.toByte(), 0xBE.toByte(), 0xEF.toByte())
        val receiver = DatagramSocket(InetSocketAddress(loopback, 0))
        receiver.soTimeout = 2000
        val port = receiver.localPort
        val received = DatagramPacket(ByteArray(256), 256)
        try {
            DatagramSocket(InetSocketAddress(loopback, 0)).use { ds ->
                ds.send(DatagramPacket(payload, payload.size, loopback, port))
            }
            receiver.receive(received)
        } finally {
            receiver.close()
        }
        assertEquals(payload.size, received.length)
        for (i in payload.indices) assertEquals(payload[i], received.data[i])
    }
    @Test fun `subnet arithmetic selects correct slash-24 match`() {
        val wlanIp = ip4("192.168.1.10")
        val senderOnWlan = ip4("192.168.1.200")
        val senderOnAp = ip4("192.168.43.50")
        val mask24 = 0xFFFFFF00.toInt()
        assertEquals(
            "sender on wlan subnet should match local IP",
            ipToInt(wlanIp) and mask24,
            ipToInt(senderOnWlan) and mask24,
        )
        assertTrue(
            "sender on hotspot subnet must NOT match wlan IP",
            (ipToInt(wlanIp) and mask24) != (ipToInt(senderOnAp) and mask24),
        )
    }
    @Test fun `dummy0 is not mobile data`() = assertTrue(!isMobileDataInterface("dummy0"))
    @Test fun `v4-wlan0 is not mobile data`() = assertTrue(!isMobileDataInterface("v4-wlan0"))
    private fun ip4(addr: String) = InetAddress.getByName(addr) as Inet4Address
    private fun loopbackIface(): NetworkInterface =
        NetworkInterface.getNetworkInterfaces().asSequence().first { it.isLoopback }
}
