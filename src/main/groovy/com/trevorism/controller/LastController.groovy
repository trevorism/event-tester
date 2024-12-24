package com.trevorism.controller

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.trevorism.https.AppClientSecureHttpClient
import com.trevorism.https.SecureHttpClient
import com.trevorism.secure.Permissions
import com.trevorism.secure.Roles
import com.trevorism.secure.Secure
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.inject.Inject
import jakarta.inject.Named

@Controller("/last")
class LastController {

    @Inject
    @Named("eventTesterSecureHttpClient")
    private SecureHttpClient appClientSecureHttpClient
    private Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").create()

    @Tag(name = "Last Operations")
    @Operation(summary = "Get the last heartbeat **Secure")
    @Get(value = "/heartbeat", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    @Secure(value = Roles.USER, allowInternal = true, permissions = Permissions.READ)
    Map heartbeat() {
        String json =  appClientSecureHttpClient.get("https://memory.data.trevorism.com/object/test-event/heartbeat")
        gson.fromJson(json, Map)
    }

    @Tag(name = "Last Operations")
    @Operation(summary = "Get the last test event **Secure")
    @Get(value = "/event", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    @Secure(value = Roles.USER, allowInternal = true, permissions = Permissions.READ)
    Map event() {
        String json = appClientSecureHttpClient.get("https://memory.data.trevorism.com/object/test-event/event")
        gson.fromJson(json, Map)
    }

    @Tag(name = "Last Operations")
    @Operation(summary = "Get the last daily test run **Secure")
    @Get(value = "/dailyRun", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    @Secure(value = Roles.USER, allowInternal = true, permissions = Permissions.READ)
    Map testRun() {
        String json = appClientSecureHttpClient.get("https://datastore.data.trevorism.com/object/testsuite/5071251278135296")
        gson.fromJson(json, Map)
    }
}
