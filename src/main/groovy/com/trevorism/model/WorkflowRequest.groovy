package com.trevorism.model

class WorkflowRequest {
    String branchName = "master"
    String yamlName = "test.yml"
    Map<String, String> workflowInputs = [:]
}
