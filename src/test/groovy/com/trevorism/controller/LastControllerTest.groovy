package com.trevorism.controller

import com.trevorism.https.SecureHttpClient
import org.junit.jupiter.api.Test

class LastControllerTest {

    @Test
    void testHeartbeat() {
        LastController lastController = new LastController()
        lastController.appClientSecureHttpClient = [get : { x -> "{\"a\":\"response\"}" }] as SecureHttpClient
        assert lastController.heartbeat()
    }

    @Test
    void testEvent() {
        LastController lastController = new LastController()
        lastController.appClientSecureHttpClient = [get : { x -> "{\"a\":\"response\"}" }] as SecureHttpClient
        assert lastController.event()
    }

    @Test
    void testTestRun() {
        LastController lastController = new LastController()
        lastController.appClientSecureHttpClient = [get : { x -> "{\"a\":\"response\"}" }] as SecureHttpClient
        assert lastController.testRun()
    }
}
