package com.trevorism.service

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.trevorism.data.FastDatastoreRepository
import com.trevorism.data.Repository
import com.trevorism.event.ChannelClient
import com.trevorism.event.DefaultChannelClient
import com.trevorism.event.DefaultEventClient
import com.trevorism.event.EventClient
import com.trevorism.https.SecureHttpClient
import com.trevorism.model.TestSuite
import com.trevorism.model.WorkflowRequest
import io.micronaut.runtime.http.scope.RequestScope
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@RequestScope
class DefaultEventTestService implements EventTestService{

    private static final Logger log = LoggerFactory.getLogger(DefaultEventTestService.class.name)

    private Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").create()
    private SecureHttpClient secureHttpClient
    private EventClient<TestSuite> eventClient
    private ChannelClient channelClient
    private Repository<TestSuite> testSuiteRepository

    DefaultEventTestService(SecureHttpClient secureHttpClient){
        this.secureHttpClient = secureHttpClient
        this.eventClient = new DefaultEventClient(secureHttpClient)
        this.channelClient = new DefaultChannelClient(secureHttpClient)
        this.testSuiteRepository = new FastDatastoreRepository<>(TestSuite, secureHttpClient)
    }

    @Override
    void sendSampleEvent(TestSuite testSuite) {
        eventClient.sendEvent("event-tester", testSuite)
    }

    @Override
    void invokeGithubWorkflow(TestSuite testSuite) {
        secureHttpClient.post(createUrl(testSuite.source), gson.toJson(new WorkflowRequest(workflowInputs: ["TEST_TYPE": "cucumber"])))
    }

    @Override
    boolean ensureTestSuiteDataExists(TestSuite testSuite) {
        boolean result = testSuiteRepository.get(testSuite.id)
        log.info("Does test suite ${testSuite.id} exist?: ${result}")
        return result
    }

    @Override
    boolean ensureMinimumEventTopicAndSubscriptionData(TestSuite testSuite) {
        return checkForTopics() && checkForSubscriptions()
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

    private static String createUrl(String projectName) {
        return "https://github.project.trevorism.com/repo/${projectName}/workflow"
    }

    private boolean checkForSubscriptions() {
        def subs = ["store-test-result","update-test-suite","store-deploy","event-test-sub"]
        def found = [false,false,false,false]
        channelClient.listSubscriptions().each { sub ->
            0..3.each { i ->
                if(sub.topic == subs[i]){
                    found[i] = true
                }
            }
        }
        boolean everySubFound = found.every { it }
        log.info("Does every subscription exist?: ${everySubFound}")
        return everySubFound
    }

    private boolean checkForTopics() {
        def topics = ["testResult","deploy","event-tester", "dead-letter-topic"]
        def found = [false,false,false,false]
        channelClient.listTopics().each { topic ->
            0..3.each { i ->
                if(topic == topics[i]){
                    found[i] = true
                }
            }
        }

        boolean everyTopicFound = found.every { it }
        log.info("Does every topic exist?: ${everyTopicFound}")
        return everyTopicFound
    }
}
