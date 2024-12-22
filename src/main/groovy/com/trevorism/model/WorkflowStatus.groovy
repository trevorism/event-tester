package com.trevorism.model

import groovy.transform.ToString

@ToString
class WorkflowStatus {
    Long workflowId
    Long runId

    String state
    String event
    String result

    String createdAt
    String updatedAt

}
