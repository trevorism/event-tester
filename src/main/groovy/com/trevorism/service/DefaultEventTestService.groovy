package com.trevorism.service

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.trevorism.data.FastDatastoreRepository
import com.trevorism.data.Repository
import com.trevorism.event.ChannelClient
import com.trevorism.event.DefaultChannelClient
import com.trevorism.event.DefaultEventClient
import com.trevorism.event.EventClient
import com.trevorism.https.AppClientSecureHttpClient
import com.trevorism.https.SecureHttpClient
import com.trevorism.model.TestSuite
import com.trevorism.model.WorkflowRequest
import com.trevorism.model.WorkflowStatus
import com.trevorism.schedule.DefaultScheduleService
import com.trevorism.schedule.ScheduleService
import com.trevorism.schedule.model.ScheduledTask
import io.micronaut.runtime.http.scope.RequestScope
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.time.Instant
import java.time.temporal.ChronoUnit

@RequestScope
class DefaultEventTestService implements EventTestService{

    private static final Logger log = LoggerFactory.getLogger(DefaultEventTestService.class.name)

    private Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").create()
    private SecureHttpClient secureHttpClient
    private EventClient<TestSuite> eventClient
    private ChannelClient channelClient
    private Repository<TestSuite> testSuiteRepository
    private ScheduleService scheduleService

    DefaultEventTestService(SecureHttpClient secureHttpClient){
        this.secureHttpClient = secureHttpClient
        this.eventClient = new DefaultEventClient(secureHttpClient)
        this.channelClient = new DefaultChannelClient(secureHttpClient)
        this.testSuiteRepository = new FastDatastoreRepository<>(TestSuite, secureHttpClient)
        this.scheduleService = new DefaultScheduleService(secureHttpClient)
    }

    @Override
    void sendSampleEvent(TestSuite testSuite) {
        eventClient.sendEvent("event-tester", testSuite)
    }

    @Override
    void invokeGithubWorkflow(TestSuite testSuite) {
        secureHttpClient.post(createGithubUrl(testSuite.source), gson.toJson(new WorkflowRequest(workflowInputs: ["TEST_TYPE": "cucumber"])))
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
        ScheduledTask task = scheduleService.get("6317601198178304")
        boolean taskExists = task != null
        log.info("Does scheduled task exist?: ${taskExists}")
        return taskExists
    }

    @Override
    boolean ensureSampleEventReceipt(TestSuite testSuite) {
        String response = secureHttpClient.get("https://memory.data.trevorism.com/object/test-event/event")
        Date date = response["timestamp"]
        log.info("Sample event timestamp: ${date}")
        return date != null && date.before(new Date()) && date.after(Date.from(Instant.now().minus(1, ChronoUnit.HOURS)))
    }

    @Override
    boolean ensureGithubInvocationSuccess(TestSuite testSuite) {
        String baseUrl = createGithubUrl(testSuite.source)
        baseUrl += "/test.yml"
        String response = new AppClientSecureHttpClient().get(baseUrl)
        WorkflowStatus status = gson.fromJson(response, WorkflowStatus.class)
        log.info("Workflow status: ${status}")
        return status != null && status.state == "success"
    }

    @Override
    boolean ensureHeartbeat() {
        String response = secureHttpClient.get("https://memory.data.trevorism.com/object/test-event/heartbeat")
        Date date = response["heartbeat"]
        log.info("Heartbeat: ${date}")
        return date != null && date.before(new Date()) && date.after(Date.from(Instant.now().minus(1, ChronoUnit.HOURS)))
    }

    @Override
    void storeHeartbeat(Map map) {
        map.put("id", "heartbeat")
        map.put("timestamp", new Date())
        secureHttpClient.post("https://memory.data.trevorism.com/object/test-event", gson.toJson(map))
    }

    @Override
    void storeEvent(Map map) {
        map.put("id", "event")
        map.put("timestamp", new Date())
        secureHttpClient.post("https://memory.data.trevorism.com/object/test-event", gson.toJson(map))
    }

    private static String createGithubUrl(String projectName) {
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
