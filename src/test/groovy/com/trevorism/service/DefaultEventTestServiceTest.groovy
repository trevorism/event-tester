package com.trevorism.service

import com.google.gson.Gson
import com.trevorism.https.SecureHttpClient
import com.trevorism.model.TestSuite
import com.trevorism.model.WorkflowStatus
import org.junit.jupiter.api.Test

import java.time.Instant

class DefaultEventTestServiceTest {

    @Test
    void testSendSampleEvent() {
        DefaultEventTestService defaultEventTestService = new DefaultEventTestService([post: { x,y -> "yes" }] as SecureHttpClient)
        assert defaultEventTestService.sendSampleEvent([:])
    }

    @Test
    void testEnsureTestSuiteDataExists() {
        DefaultEventTestService defaultEventTestService = new DefaultEventTestService([get: { x -> "{}" }] as SecureHttpClient)
        assert defaultEventTestService.ensureTestSuiteDataExists(new TestSuite([id:"5071251278135296", source: "event-tester", kind: "web"]))
    }

    @Test
    void testEnsureMinimumEventTopicAndSubscriptionData() {
        DefaultEventTestService defaultEventTestService = new DefaultEventTestService([get: { x -> "[]" }] as SecureHttpClient)
        assert !defaultEventTestService.ensureMinimumEventTopicAndSubscriptionData()
    }

    @Test
    void testEnsureScheduleData() {
        DefaultEventTestService defaultEventTestService = new DefaultEventTestService([get: { x -> "{}" }] as SecureHttpClient)
        assert defaultEventTestService.ensureScheduleData()
    }

    @Test
    void testEnsureSampleEventReceipt() {
        DefaultEventTestService defaultEventTestService = new DefaultEventTestService([get: { x -> "{\"timestamp\":\"${Instant.now().toString()}\"}".toString() }] as SecureHttpClient)
        assert defaultEventTestService.ensureSampleEventReceipt()
    }

    @Test
    void testEnsureHeartbeat() {
        DefaultEventTestService defaultEventTestService = new DefaultEventTestService([get: { x -> "{\"timestamp\":\"${Instant.now().minusMillis(1000).toString()}\"}".toString() }] as SecureHttpClient)
        assert defaultEventTestService.ensureHeartbeat()
    }

    @Test
    void testStoreHeartbeat() {
        DefaultEventTestService defaultEventTestService = new DefaultEventTestService([post: { x,y -> "yes" }, delete: { x -> "{}"}] as SecureHttpClient)
        defaultEventTestService.storeHeartbeat(["beat": true])
    }

    @Test
    void testStoreEvent() {
        DefaultEventTestService defaultEventTestService = new DefaultEventTestService([post: { x,y -> "yes" }, delete: { x -> "{}"}] as SecureHttpClient)
        defaultEventTestService.storeEvent(["event": true])
    }

    @Test
    void testEnsureGithubInvocationSuccess(){
        Gson gson = new Gson()
        DefaultEventTestService defaultEventTestService = new DefaultEventTestService([get: { x -> gson.toJson(new WorkflowStatus(updatedAt: Instant.now())) }] as SecureHttpClient)
        assert defaultEventTestService.ensureGithubInvocationSuccess(new TestSuite([source: "event-tester", kind: "web"]))
    }

    @Test
    void testInvokeGithubWorkflow(){
        DefaultEventTestService defaultEventTestService = new DefaultEventTestService([post: { x,y -> "yes" }] as SecureHttpClient)
        assert defaultEventTestService.invokeGithubWorkflow(new TestSuite([source: "event-tester", kind: "web"]))
    }

}
