/*
 * Copyright 2020 Cerner Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netflix.spinnaker.echo.microsoftteams;

import com.netflix.spinnaker.echo.api.Notification;
import com.netflix.spinnaker.echo.controller.EchoResponse;
import com.netflix.spinnaker.echo.notification.NotificationService;
import com.netflix.spinnaker.echo.notification.NotificationTemplateEngine;
import java.util.Collection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty('microsoft-teams.enabled')
class MicrosoftTeamsNotificationService implements NotificationService {

  @Autowired MicrosoftTeamsService teamsService;
  @Autowired NotificationTemplateEngine notificationTemplateEngine;

  @Override
  public boolean supportsType(String type) {
    return "MICROSOFT_TEAMS".equals(type.toUpperCase());
  }

  @Override
  public EchoResponse.Void handle(Notification notification) {
    String message = notificationTemplateEngine.build(notification, NotificationTemplateEngine.Type.BODY);
    String summary = notificationTemplateEngine.build(notification, NotificationTemplateEngine.Type.SUBJECT);
    
    for (String webhookUrl : notification.getTo()) {
      log.info("Sending Microsoft Teams message to webhook URL " + webhookUrl);

      String baseUrl = "https://outlook.office.com/webhook/";
      String completeLink = webhookUrl;
      String partialWebhookURL = completeLink.substring(baseUrl.length());

      teamsService.sendMessage(partialWebhookURL, new MicrosoftTeamsMessage(message, summary, null))
    }

    return new EchoResponse.Void();
  }
}
