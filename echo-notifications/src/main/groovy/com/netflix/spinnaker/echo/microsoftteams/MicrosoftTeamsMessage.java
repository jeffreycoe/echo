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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class MicrosoftTeamsMessage {
  // API References:
  // https://docs.microsoft.com/en-us/outlook/actionable-messages/message-card-reference
  // https://docs.microsoft.com/en-us/microsoftteams/platform/webhooks-and-connectors/how-to/connectors-using
  // https://docs.microsoft.com/en-us/microsoftteams/platform/task-modules-and-cards/cards/cards-reference#office-365-connector-card
  
  private static String ACTIVITY_TITLE = "Spinnaker Notifications";
  private static String FACTS_TITLE = "Execution Status";

  @JsonProperty("@context")
  public String context = "http://schema.org/extensions";

  @JsonProperty("@type")
  public String type = "MessageCard";

  public String correlationId;
  public String summary;
  public String themeColor;

  private transient HashMap<String, Object> metadata;

  public MicrosoftTeamsMessage(String message, String summary, HashMap<String, Object> metadata) {
    this.correlationId = this.createRandomUUID();
    this.summary = summary;
    themeColor = this.getThemeColor((String)metadata.get("executionStatus"));

    if (metadata != null) {
      this.metadata = metadata;
    }
  }

  public List<HashMap> getSections() {
    List<HashMap> sections = new ArrayList<>();
    HashMap<String, Object> activityTitle = this.getSection("activityTitle", ACTIVITY_TITLE);

    sections.add(activityTitle);
    sections.add((HashMap) this.getFacts());

    return sections;
  }

  public HashMap<String, Object> getSection(String name, Object obj) {
    HashMap<String, Object> section = new HashMap<>();

    section.put(name, obj);

    return section;
  }

  private HashMap<String, Object> getFacts() {
    HashMap<String, Object> facts = new HashMap<>();
    List<HashMap> factsList = new ArrayList<>();

    factsList.add((HashMap) this.getFact("Build Number", (String) metadata.get("buildNumber")));
    factsList.add((HashMap) this.getFact("Description", (String) metadata.get("description")));
    factsList.add((HashMap) this.getFact("Execution Name", (String) metadata.get("executionName")));

    if (message != "") {
      factsList.add((HashMap) this.getFact("Message", message));
    }

    factsList.add((HashMap) this.getFact("Pipeline / Stage Name", (String) metadata.get("eventName")));
    factsList.add((HashMap) this.getFact("Status", (String) metadata.get("executionStatus")));
    factsList.add((HashMap) this.getFact("Summary", (String) metadata.get("executionSummary")));

    facts.put("title", FACTS_TITLE);
    facts.put("facts", factsList);

    return facts;
  }

  public HashMap<String, String> getFact(String name, String value) {
    HashMap<String, String> fact = new HashMap<>();

    fact.put("name", name);
    fact.put("value", value);

    return fact;
  }

  public List<HashMap> getPotentialAction() {
    List<HashMap> potentialAction = new ArrayList<>();
    HashMap<String, Object> action = new HashMap<>();
    ArrayList<String> targets = new ArrayList<>();

    targets.add((String) metadata.get("executionUrl"));

    action.put("@context", "http://schema.org");
    action.put("@type", "ViewAction");
    action.put("name", "View Execution");
    action.put("target", targets);

    potentialAction.add(action);

    return potentialAction;
  }

  private static String getThemeColor(String status) {
    String color = "";
    status = (status == null) ? "" : status;

    if (status.contains("failed")) {
      color = "EB1A1A";
    } else if (status.contains("complete")) {
      color = "73DB69";
    } else {
      color = "0076D7";
    }
    
    return color;
  }

  private static String createRandomUUID() {
    return UUID.randomUUID().toString();
  }
}
