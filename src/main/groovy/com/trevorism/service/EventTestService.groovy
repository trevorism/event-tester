package com.trevorism.service

import com.trevorism.model.TestSuite

interface EventTestService {

    void sendSampleEvent(TestSuite testSuite)
    void invokeGithubWorkflow(TestSuite testSuite)

    boolean ensureTestSuiteDataExists(TestSuite testSuite)
    boolean ensureMinimumEventTopicAndSubscriptionData(TestSuite testSuite)
    boolean ensureScheduleData(TestSuite testSuite)
    boolean ensureSampleEventReceipt(TestSuite testSuite)
    boolean ensureGithubInvocationSuccess(TestSuite testSuite)

    boolean ensureHeartbeat()

    void storeHeartbeat(Map map)
    void storeEvent(Map map)
}