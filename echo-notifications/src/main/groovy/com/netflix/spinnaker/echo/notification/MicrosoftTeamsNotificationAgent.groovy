/*
 * Copyright 2020 Cerner Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netflix.spinnaker.echo.notification;

@Slf4j
@ConditionalOnProperty('microsoft-teams.enabled')
@Service
class MicrosoftTeamsNotificationAgent extends AbstractEventNotificationAgent {

  @Autowired
  MicrosoftTeamsService teamsService

  @Override
  public String getNotificationType() {
    return "microsoftteams";
  }

  @Override
  void sendNotifications(Map preference, String application, Event event, Map config, String status) {
    log.info('Building Microsoft Teams notification')

    HashMap<String, Object> metadata = new HashMap<>()
    String executionUrl = event.content?.execution?.trigger?.buildInfo?.url
    String executionDescription = event.content?.execution?.description
    String executionName = event.content?.execution?.name
    String message
    String summary
    String eventName

    if (config.type == 'pipeline' || config.type == 'stage') {
      metadata.put("buildNumber", (Integer)event.content.execution.trigger.buildInfo.number)
    }
    
    if (config.type == 'stage') {
      eventName = event.content.name ?: context.stageDetails.name
      metadata.put("eventName", eventName)
      summary = """Stage $eventName for ${application}'s ${event.content?.execution?.name} pipeline ${buildInfo}"""
    } else if (config.type == 'pipeline') {
      summary = """${application}'s ${event.content?.execution?.name} pipeline ${buildInfo}"""
    } else {
      summary = """${application}'s ${event.content?.execution?.id} task """
    }

    summary += """${status == 'starting' ? 'is' : 'has'} ${status == 'complete' ? 'completed successfully' : status}"""
    summary = preference.customSubject ?: context.customSubject ?: subject

    String customMessage = preference.customMessage ?: event.content?.context?.customMessage
    if (customMessage) {
      message = customMessage
        .replace("{{executionId}}", (String) event.content.execution?.id ?: "")
        .replace("{{link}}", link ?: "")
    }
  
    if (executionDescription != null) {
      metadata.put("executionDescription", executionDescription)
    }

    if (executionName != null) {
      metadata.put("executionName", executionUrl)
    }

    if (executionUrl != null) {
      metadata.put("executionUrl", executionUrl)
    }

    metadata.put("executionName", executionName)
    metadata.put("executionStatus", status)
    metadata.put("executionSummary", summary)

    log.info('Sending Microsoft Teams notification')
    String baseUrl = "https://outlook.office.com/webhook/"
    String completeLink = preference.address
    String partialWebhookURL = completeLink.substring(baseUrl.length())
    Response response = teamsService.sendMessage(partialWebhookURL, new MicrosoftTeamsMessage(message, summary, metadata))

    log.info("Received response from Google Chat: {} {} for execution id {}. {}",
      response?.status, response?.reason, event.content?.execution?.id, response?.body)
  }
}