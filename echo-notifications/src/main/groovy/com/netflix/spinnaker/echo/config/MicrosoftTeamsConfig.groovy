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

package com.netflix.spinnaker.echo.config

import com.netflix.spinnaker.echo.microsoftteams.MicrosoftTeamsService
import com.netflix.spinnaker.echo.microsoftteams.MicrosoftTeamsClient
import com.netflix.spinnaker.retrofit.Slf4jRetrofitLogger
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import retrofit.Endpoint
import retrofit.RestAdapter
import retrofit.client.Client
import retrofit.converter.JacksonConverter

import static retrofit.Endpoints.newFixedEndpoint

@Configuration
@ConditionalOnProperty("microsoftteams.enabled")
@Slf4j
@CompileStatic
class MicrosoftTeamsConfig {

  @Bean
  Endpoint teamsEndpoint() {
    newFixedEndpoint("https://outlook.office.com/webhook")
  }

  @Bean
  MicrosoftTeamsService microsoftTeamsService(
    Endpoint teamsEndpoint, Client retrofitClient, RestAdapter.LogLevel retrofitLogLevel) {

    log.info("Microsoft Teams service loaded");

    def microsoftTeamsClient = new RestAdapter.Builder()
            .setConverter(new JacksonConverter())
            .setClient(retrofitClient)
            .setEndpoint(teamsEndpoint)
            .setLogLevel(retrofitLogLevel)
            .setLog(new Slf4jRetrofitLogger(MicrosoftTeamsClient.class))
            .build()
            .create(MicrosoftTeamsClient.class);

    new MicrosoftTeamsService(microsoftTeamsClient);
  }
}