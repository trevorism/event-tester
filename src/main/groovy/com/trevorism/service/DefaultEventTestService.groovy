package com.trevorism.service

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.trevorism.TestErrorClient
import com.trevorism.data.FastDatastoreRepository
import com.trevorism.data.Repository
import com.trevorism.event.ChannelClient
import com.trevorism.event.DefaultChannelClient
import com.trevorism.event.DefaultEventClient
import com.trevorism.event.EventClient
import com.trevorism.https.AppClientSecureHttpClient
import com.trevorism.https.SecureHttpClient
import com.trevorism.model.TestError
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
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@RequestScope
class DefaultEventTestService implements EventTestService{

    private static final Logger log = LoggerFactory.getLogger(DefaultEventTestService.class.name)

    private Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").create()
    private SecureHttpClient secureHttpClient
    private EventClient<TestSuite> eventClient
    private ChannelClient channelClient
    private Repository<TestSuite> testSuiteRepository
    private ScheduleService scheduleService
    private SecureHttpClient appClientSecureHttpClient = new AppClientSecureHttpClient()

    DefaultEventTestService(SecureHttpClient secureHttpClient){
        this.secureHttpClient = secureHttpClient
        this.eventClient = new DefaultEventClient(secureHttpClient)
        this.channelClient = new DefaultChannelClient(secureHttpClient)
        this.testSuiteRepository = new FastDatastoreRepository<>(TestSuite, secureHttpClient)
        this.scheduleService = new DefaultScheduleService(secureHttpClient)
    }

    @Override
    String sendSampleEvent(TestSuite testSuite) {
        eventClient.sendEvent("event-tester", testSuite)
    }

    @Override
    String invokeGithubWorkflow(TestSuite testSuite) {
        appClientSecureHttpClient.post(createGithubUrl(testSuite.source), gson.toJson(new WorkflowRequest(workflowInputs: ["TEST_TYPE": "cucumber"])))
    }

    @Override
    boolean ensureTestSuiteDataExists(TestSuite testSuite) {
        boolean result = testSuiteRepository.get(testSuite.id)
        log.info("Does test suite ${testSuite.id} exist?: ${result}")
        return result
    }

    @Override
    boolean ensureMinimumEventTopicAndSubscriptionData() {
        return checkForTopics() && checkForSubscriptions()
    }

    @Override
    boolean ensureScheduleData() {
        ScheduledTask task = scheduleService.get("6317601198178304")
        ScheduledTask task2 = scheduleService.get("5721406113316864")
        boolean taskExists = task != null && task2 != null
        log.info("Does scheduled task for daily error check and monitoring this app exist?: ${taskExists}")
        return taskExists
    }

    @Override
    boolean ensureSampleEventReceipt() {
        def executor = Executors.newSingleThreadScheduledExecutor()
        def future = executor.schedule({
            String response = secureHttpClient.get("https://memory.data.trevorism.com/object/test-event/event")
            String timestamp = gson.fromJson(response, Map)["timestamp"]
            boolean event = checkIfTimeOccurredBetweenNowAndOneHourAgo(timestamp)
            log.info("Did event happen within an hour: ${event}")
            return event
        } as Callable<Boolean>, 10, TimeUnit.SECONDS)

        try {
            return future.get()
        } catch (Exception e) {
            log.warn("Error during delay", e)
            return false
        } finally {
            executor.shutdown()
        }
    }

    private static boolean checkIfTimeOccurredBetweenNowAndOneHourAgo(String timestamp) {
        Instant instant = Instant.parse(timestamp)
        return instant != null && instant.isBefore(Instant.now()) && instant.isAfter(Instant.now().minus(1, ChronoUnit.HOURS))
    }

    @Override
    boolean ensureGithubInvocationSuccess(TestSuite testSuite) {
        String baseUrl = createGithubUrl(testSuite.source)
        baseUrl += "/test.yml"
        String response = appClientSecureHttpClient.get(baseUrl)
        WorkflowStatus status = gson.fromJson(response, WorkflowStatus.class)
        log.info("Workflow status: ${status}")
        return checkIfTimeOccurredBetweenNowAndOneHourAgo(status.updatedAt)
    }

    @Override
    boolean ensureHeartbeat() {
        String response = secureHttpClient.get("https://memory.data.trevorism.com/object/test-event/heartbeat")
        String timestamp = gson.fromJson(response, Map)["timestamp"]
        boolean heartbeat = checkIfTimeOccurredBetweenNowAndOneHourAgo(timestamp)
        log.info("Did heartbeat happen within an hour: ${heartbeat}")
        return heartbeat
    }

    @Override
    boolean ensureDailyTestRunAndSendError() {
        TestSuite testSuite = testSuiteRepository.get("5071251278135296")
        log.info("Test suite last run date: ${testSuite.lastRunDate}")
        Instant instant = testSuite.lastRunDate.toInstant()
        boolean result = instant?.isAfter(Instant.now().minus(1, ChronoUnit.DAYS))

        if(!result){
            TestErrorClient testErrorClient = new TestErrorClient(appClientSecureHttpClient)
            testErrorClient.addTestError(
                    new TestError(source: "event-tester", message: "Test suite did not run today", details: ["lastRun": instant.toString()]))
        }

        return result
    }

    @Override
    void storeHeartbeat(Map map) {
        map.put("id", "heartbeat")
        map.put("timestamp", Instant.now().toString())
        try {
            appClientSecureHttpClient.delete("https://memory.data.trevorism.com/object/test-event/heartbeat")
        } catch (Exception e) {
            log.warn("Failed to delete heartbeat", e)
        }
        appClientSecureHttpClient.post("https://memory.data.trevorism.com/object/test-event", gson.toJson(map))
    }

    @Override
    void storeEvent(Map map) {
        map.put("id", "event")
        map.put("timestamp", Instant.now().toString())
        try{
            appClientSecureHttpClient.delete("https://memory.data.trevorism.com/object/test-event/event")
        } catch (Exception e) {
            log.warn("Failed to delete event", e)
        }
        appClientSecureHttpClient.post("https://memory.data.trevorism.com/object/test-event", gson.toJson(map))
    }

    private static String createGithubUrl(String projectName) {
        return "https://github.project.trevorism.com/repo/${projectName}/workflow"
    }

    private boolean checkForSubscriptions() {
        def subs = ["store-test-result","update-test-suite","store-deploy","event-test-sub"]
        def found = [false,false,false,false]
        channelClient.listSubscriptions().each { sub ->
            for(int i = 0; i < 4; i++) {
                if (sub.name == subs[i]) {
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
            for(int i = 0; i < 4; i++) {
                if (topic == topics[i]) {
                    found[i] = true
                }
            }
        }

        boolean everyTopicFound = found.every { it }
        log.info("Does every topic exist?: ${everyTopicFound}")
        return everyTopicFound
    }
}
