/*
 * Copyright 2022 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.cloud.recommender.v1.spring;

import com.google.api.core.BetaApi;
import com.google.cloud.spring.core.Credentials;
import com.google.cloud.spring.core.CredentialsSupplier;
import com.google.cloud.spring.core.Retry;
import javax.annotation.Generated;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

// AUTO-GENERATED DOCUMENTATION AND CLASS.
/** Provides default property values for Recommender client bean */
@Generated("by google-cloud-spring-generator")
@BetaApi("Autogenerated Spring autoconfiguration is not yet stable")
@ConfigurationProperties("com.google.cloud.recommender.v1.recommender")
public class RecommenderSpringProperties implements CredentialsSupplier {
  /** OAuth2 credentials to authenticate and authorize calls to Google Cloud Client Libraries. */
  @NestedConfigurationProperty
  private final Credentials credentials =
      new Credentials("https://www.googleapis.com/auth/cloud-platform");
  /** Quota project to use for billing. */
  private String quotaProjectId;
  /** Number of threads used for executors. */
  private Integer executorThreadCount;
  /** Allow override of default transport channel provider to use REST instead of gRPC. */
  private boolean useRest = false;
  /** Allow override of retry settings at service level, applying to all of its RPC methods. */
  @NestedConfigurationProperty private Retry retry;
  /**
   * Allow override of retry settings at method-level for listInsights. If defined, this takes
   * precedence over service-level retry configurations for that RPC method.
   */
  @NestedConfigurationProperty private Retry listInsightsRetry;
  /**
   * Allow override of retry settings at method-level for getInsight. If defined, this takes
   * precedence over service-level retry configurations for that RPC method.
   */
  @NestedConfigurationProperty private Retry getInsightRetry;
  /**
   * Allow override of retry settings at method-level for markInsightAccepted. If defined, this
   * takes precedence over service-level retry configurations for that RPC method.
   */
  @NestedConfigurationProperty private Retry markInsightAcceptedRetry;
  /**
   * Allow override of retry settings at method-level for listRecommendations. If defined, this
   * takes precedence over service-level retry configurations for that RPC method.
   */
  @NestedConfigurationProperty private Retry listRecommendationsRetry;
  /**
   * Allow override of retry settings at method-level for getRecommendation. If defined, this takes
   * precedence over service-level retry configurations for that RPC method.
   */
  @NestedConfigurationProperty private Retry getRecommendationRetry;
  /**
   * Allow override of retry settings at method-level for markRecommendationClaimed. If defined,
   * this takes precedence over service-level retry configurations for that RPC method.
   */
  @NestedConfigurationProperty private Retry markRecommendationClaimedRetry;
  /**
   * Allow override of retry settings at method-level for markRecommendationSucceeded. If defined,
   * this takes precedence over service-level retry configurations for that RPC method.
   */
  @NestedConfigurationProperty private Retry markRecommendationSucceededRetry;
  /**
   * Allow override of retry settings at method-level for markRecommendationFailed. If defined, this
   * takes precedence over service-level retry configurations for that RPC method.
   */
  @NestedConfigurationProperty private Retry markRecommendationFailedRetry;
  /**
   * Allow override of retry settings at method-level for getRecommenderConfig. If defined, this
   * takes precedence over service-level retry configurations for that RPC method.
   */
  @NestedConfigurationProperty private Retry getRecommenderConfigRetry;
  /**
   * Allow override of retry settings at method-level for updateRecommenderConfig. If defined, this
   * takes precedence over service-level retry configurations for that RPC method.
   */
  @NestedConfigurationProperty private Retry updateRecommenderConfigRetry;
  /**
   * Allow override of retry settings at method-level for getInsightTypeConfig. If defined, this
   * takes precedence over service-level retry configurations for that RPC method.
   */
  @NestedConfigurationProperty private Retry getInsightTypeConfigRetry;
  /**
   * Allow override of retry settings at method-level for updateInsightTypeConfig. If defined, this
   * takes precedence over service-level retry configurations for that RPC method.
   */
  @NestedConfigurationProperty private Retry updateInsightTypeConfigRetry;

  @Override
  public Credentials getCredentials() {
    return this.credentials;
  }

  public String getQuotaProjectId() {
    return this.quotaProjectId;
  }

  public void setQuotaProjectId(String quotaProjectId) {
    this.quotaProjectId = quotaProjectId;
  }

  public boolean getUseRest() {
    return this.useRest;
  }

  public void setUseRest(boolean useRest) {
    this.useRest = useRest;
  }

  public Integer getExecutorThreadCount() {
    return this.executorThreadCount;
  }

  public void setExecutorThreadCount(Integer executorThreadCount) {
    this.executorThreadCount = executorThreadCount;
  }

  public Retry getRetry() {
    return this.retry;
  }

  public void setRetry(Retry retry) {
    this.retry = retry;
  }

  public Retry getListInsightsRetry() {
    return this.listInsightsRetry;
  }

  public void setListInsightsRetry(Retry listInsightsRetry) {
    this.listInsightsRetry = listInsightsRetry;
  }

  public Retry getGetInsightRetry() {
    return this.getInsightRetry;
  }

  public void setGetInsightRetry(Retry getInsightRetry) {
    this.getInsightRetry = getInsightRetry;
  }

  public Retry getMarkInsightAcceptedRetry() {
    return this.markInsightAcceptedRetry;
  }

  public void setMarkInsightAcceptedRetry(Retry markInsightAcceptedRetry) {
    this.markInsightAcceptedRetry = markInsightAcceptedRetry;
  }

  public Retry getListRecommendationsRetry() {
    return this.listRecommendationsRetry;
  }

  public void setListRecommendationsRetry(Retry listRecommendationsRetry) {
    this.listRecommendationsRetry = listRecommendationsRetry;
  }

  public Retry getGetRecommendationRetry() {
    return this.getRecommendationRetry;
  }

  public void setGetRecommendationRetry(Retry getRecommendationRetry) {
    this.getRecommendationRetry = getRecommendationRetry;
  }

  public Retry getMarkRecommendationClaimedRetry() {
    return this.markRecommendationClaimedRetry;
  }

  public void setMarkRecommendationClaimedRetry(Retry markRecommendationClaimedRetry) {
    this.markRecommendationClaimedRetry = markRecommendationClaimedRetry;
  }

  public Retry getMarkRecommendationSucceededRetry() {
    return this.markRecommendationSucceededRetry;
  }

  public void setMarkRecommendationSucceededRetry(Retry markRecommendationSucceededRetry) {
    this.markRecommendationSucceededRetry = markRecommendationSucceededRetry;
  }

  public Retry getMarkRecommendationFailedRetry() {
    return this.markRecommendationFailedRetry;
  }

  public void setMarkRecommendationFailedRetry(Retry markRecommendationFailedRetry) {
    this.markRecommendationFailedRetry = markRecommendationFailedRetry;
  }

  public Retry getGetRecommenderConfigRetry() {
    return this.getRecommenderConfigRetry;
  }

  public void setGetRecommenderConfigRetry(Retry getRecommenderConfigRetry) {
    this.getRecommenderConfigRetry = getRecommenderConfigRetry;
  }

  public Retry getUpdateRecommenderConfigRetry() {
    return this.updateRecommenderConfigRetry;
  }

  public void setUpdateRecommenderConfigRetry(Retry updateRecommenderConfigRetry) {
    this.updateRecommenderConfigRetry = updateRecommenderConfigRetry;
  }

  public Retry getGetInsightTypeConfigRetry() {
    return this.getInsightTypeConfigRetry;
  }

  public void setGetInsightTypeConfigRetry(Retry getInsightTypeConfigRetry) {
    this.getInsightTypeConfigRetry = getInsightTypeConfigRetry;
  }

  public Retry getUpdateInsightTypeConfigRetry() {
    return this.updateInsightTypeConfigRetry;
  }

  public void setUpdateInsightTypeConfigRetry(Retry updateInsightTypeConfigRetry) {
    this.updateInsightTypeConfigRetry = updateInsightTypeConfigRetry;
  }
}
