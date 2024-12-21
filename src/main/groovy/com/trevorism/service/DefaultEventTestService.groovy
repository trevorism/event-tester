package com.trevorism.service

import com.trevorism.model.TestSuite

class DefaultEventTestService implements EventTestService{
    @Override
    void sendSampleEvent(TestSuite testSuite) {

    }

    @Override
    void invokeGithubWorkflow(TestSuite testSuite) {

    }

    @Override
    boolean ensureTestSuiteDataExists(TestSuite testSuite) {
        return false
    }

    @Override
    boolean ensureMinimumEventTopicAndSubscriptionData(TestSuite testSuite) {
        return false
    }

    @Override
    boolean ensureScheduleData(TestSuite testSuite) {
        return false
    }

    @Override
    boolean ensureSampleEventReceipt(TestSuite testSuite) {
        return false
    }

    @Override
    boolean ensureGithubInvocationSuccess(TestSuite testSuite) {
        return false
    }

    @Override
    boolean validateDailyRun() {
        return false
    }
}
