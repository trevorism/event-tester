package com.trevorism.service

import com.trevorism.model.TestSuite

interface EventTestService {

    String sendSampleEvent(Map map)
    String invokeGithubWorkflow(TestSuite testSuite)

    boolean ensureTestSuiteDataExists(TestSuite testSuite)
    boolean ensureMinimumEventTopicAndSubscriptionData()
    boolean ensureScheduleData()
    boolean ensureSampleEventReceipt()
    boolean ensureGithubInvocationSuccess(TestSuite testSuite)

    boolean ensureHeartbeat()
    boolean ensureDailyTestRunAndAlert()

    void storeHeartbeat(Map map)
    void storeEvent(Map map)
}