package com.trevorism.controller

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.trevorism.https.AppClientSecureHttpClient
import com.trevorism.https.SecureHttpClient
import com.trevorism.secure.Permissions
import com.trevorism.secure.Roles
import com.trevorism.secure.Secure
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag

@Controller("/last")
class LastController {

    private SecureHttpClient appClientSecureHttpClient = new AppClientSecureHttpClient()
    private Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").create()

    @Tag(name = "Last Operations")
    @Operation(summary = "Get the last heartbeat **Secure")
    @Get(value = "/heartbeat", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    @Secure(value = Roles.USER, allowInternal = true, permissions = Permissions.READ)
    Map heartbeat() {
        String json = appClientSecureHttpClient.get("https://memory.data.trevorism.com/object/test-event/heartbeat")
        gson.fromJson(json, Map)
    }

    @Tag(name = "Last Operations")
    @Operation(summary = "Webhook for a sample event **Secure")
    @Get(value = "/event", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    @Secure(value = Roles.USER, allowInternal = true, permissions = Permissions.READ)
    Map event() {
        String json = appClientSecureHttpClient.get("https://memory.data.trevorism.com/object/test-event/event")
        gson.fromJson(json, Map)
    }
}
