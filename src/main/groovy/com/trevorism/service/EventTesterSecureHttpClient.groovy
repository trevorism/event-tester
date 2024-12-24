package com.trevorism.service

import com.trevorism.https.AppClientSecureHttpClient
import com.trevorism.https.SecureHttpClient
import jakarta.inject.Named

@jakarta.inject.Singleton
@Named("eventTesterSecureHttpClient")
class EventTesterSecureHttpClient extends AppClientSecureHttpClient implements SecureHttpClient{
}
