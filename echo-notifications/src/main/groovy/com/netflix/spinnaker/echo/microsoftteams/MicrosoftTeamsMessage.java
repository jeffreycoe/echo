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

  static transient String ACTIVITY_TITLE = "Spinnaker Notification";
  static transient String ACTIVITY_SUBTITLE = "Status";
  static transient String CONTEXT = "http://schema.org/extensions";
  static transient boolean MARKDOWN_ENABLED = true;
  static transient String MESSAGE_TYPE = "MessageCard";
  static transient String VIEW_ACTION_CONTEXT = "http://schema.org";
  static transient String VIEW_ACTION_TYPE = "ViewAction";

  @SerializedName("@context")
  public String context = CONTEXT;

  @SerializedName("@type")
  public String type = MESSAGE_TYPE;

  public String correlationId;
  public String themeColor;
  public Section sections;
  public String summary;
  public PotentialAction potentialAction;

  transient String executionUrl;
  transient HashMap<String, Object> metadata;

  public MicrosoftTeamsMessage(String message, String summary, HashMap<String, Object> metadata) {
    this.correlationId = this.createRandomUUID();
    this.summary = summary;
    themeColor = this.getThemeColor((String)metadata.get("executionStatus"));

    sections = new Section();
    potentialAction = new PotentialAction();

    if (metadata != null) {
      this.metadata = metadata;
    }
  }

  class Fact {
    public HashMap<String, String> fact = new HashMap<>();

    public Fact(String name, String value) {
      fact.put(name, value);
    }
  }

  class Facts {
    public List<Fact> facts = new ArrayList<>();

    public Facts {
      if (metadata.containsKey("buildNumber")) {
        facts.add(new Fact("Build Number", (Integer)metadata.get("buildNumber")));
      }

      facts.add(new Fact("Description", (String)metadata.get("executionDescription")));
      facts.add(new Fact("Execution Name", (String)metadata.get("executionName")));

      if (metadata.containsKey("eventName")) {
        facts.add(new Fact("Event Name", (String)metadata.get("eventName")));
      }

      facts.add(new Fact("Status", (String)metadata.get("executionStatus")));
      facts.add(new Fact("Summary", (String)metadata.get("executionSummary")));
    }
  }

  class PotentialAction {
    public List<ViewAction> potentialAction = new ArrayList<>();

    public PotentialAction() {
      if (executionUrl != null) {
        potentialAction.add(new ViewAction());
      }      
    }
  }

  class Section {
    public HashMap<String, Object> title = new HashMap<>();
    public HashMap<String, Object> facts = new HashMap<>();

    public Section() {
      title.put("activityTitle", ACTIVITY_TITLE);
      title.put("markdown", MARKDOWN_ENABLED);

      facts.put("title", "Execution Status");
      facts.put("facts", new Facts());
    }
  }

  class ViewAction {
    @SerializedName("@context")
    public String potentialActionContext = VIEW_ACTION_CONTEXT;

    @SerializedName("@type")
    public String potentialActionType = VIEW_ACTION_TYPE;

    public String name = "View Execution";
    public List<String> target = new ArrayList<>();

    public ViewAction() {
      if (metadata.containsKey("executionUrl")) {
        target.add((String)metadata.get("executionUrl"));
      }
    }
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
    UUID uuid = UUID.randomUUID();
    String uuidString = uuid.toString();

    return uuidString;
  }
}
