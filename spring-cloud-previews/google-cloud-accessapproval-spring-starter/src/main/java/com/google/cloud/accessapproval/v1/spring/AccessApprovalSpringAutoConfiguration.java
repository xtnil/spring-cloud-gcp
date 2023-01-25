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

package com.google.cloud.accessapproval.v1.spring;

import com.google.api.core.BetaApi;
import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.ExecutorProvider;
import com.google.api.gax.retrying.RetrySettings;
import com.google.api.gax.rpc.HeaderProvider;
import com.google.api.gax.rpc.TransportChannelProvider;
import com.google.cloud.accessapproval.v1.AccessApprovalAdminClient;
import com.google.cloud.accessapproval.v1.AccessApprovalAdminSettings;
import com.google.cloud.spring.autoconfigure.core.GcpContextAutoConfiguration;
import com.google.cloud.spring.core.DefaultCredentialsProvider;
import com.google.cloud.spring.core.Retry;
import com.google.cloud.spring.core.util.RetryUtil;
import java.io.IOException;
import java.util.Collections;
import javax.annotation.Generated;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

// AUTO-GENERATED DOCUMENTATION AND CLASS.
/**
 * Auto-configuration for {@link AccessApprovalClient}.
 *
 * <p>Provides auto-configuration for Spring Boot
 *
 * <p>The default instance has everything set to sensible defaults:
 *
 * <ul>
 *   <li>The default transport provider is used.
 *   <li>Credentials are acquired automatically through Application Default Credentials.
 *   <li>Retries are configured for idempotent methods but not for non-idempotent methods.
 * </ul>
 */
@Generated("by google-cloud-spring-generator")
@BetaApi("Autogenerated Spring autoconfiguration is not yet stable")
@AutoConfiguration
@AutoConfigureAfter(GcpContextAutoConfiguration.class)
@ConditionalOnClass(AccessApprovalAdminClient.class)
@ConditionalOnProperty(
    value = "com.google.cloud.accessapproval.v1.access-approval.enabled",
    matchIfMissing = true)
@EnableConfigurationProperties(AccessApprovalSpringProperties.class)
public class AccessApprovalSpringAutoConfiguration {
  private final AccessApprovalSpringProperties clientProperties;
  private final CredentialsProvider credentialsProvider;
  private static final Log LOGGER = LogFactory.getLog(AccessApprovalSpringAutoConfiguration.class);

  protected AccessApprovalSpringAutoConfiguration(
      AccessApprovalSpringProperties clientProperties, CredentialsProvider credentialsProvider)
      throws IOException {
    this.clientProperties = clientProperties;
    if (this.clientProperties.getCredentials().hasKey()) {
      if (LOGGER.isTraceEnabled()) {
        LOGGER.trace("Using credentials from AccessApproval-specific configuration");
      }
      this.credentialsProvider =
          ((CredentialsProvider) new DefaultCredentialsProvider(this.clientProperties));
    } else {
      this.credentialsProvider = credentialsProvider;
    }
  }

  /**
   * Provides a default transport channel provider bean. The default is gRPC and will default to it
   * unless the useRest option is provided to use HTTP transport instead
   *
   * @return a default transport channel provider.
   */
  @Bean
  @ConditionalOnMissingBean(name = "defaultAccessApprovalTransportChannelProvider")
  public TransportChannelProvider defaultAccessApprovalTransportChannelProvider() {
    if (this.clientProperties.getUseRest()) {
      return AccessApprovalAdminSettings.defaultHttpJsonTransportProviderBuilder().build();
    }
    return AccessApprovalAdminSettings.defaultTransportChannelProvider();
  }

  /**
   * Provides a AccessApprovalSettings bean configured to use the default credentials provider
   * (obtained with accessApprovalCredentials()) and its default transport channel provider
   * (defaultAccessApprovalTransportChannelProvider()). It also configures the quota project ID if
   * provided. It will configure an executor provider in case there is more than one thread
   * configured in the client
   *
   * <p>Retry settings are also configured from service-level and method-level properties specified
   * in AccessApprovalSpringProperties. Method-level properties will take precedence over
   * service-level properties if available, and client library defaults will be used if neither are
   * specified.
   *
   * @param defaultTransportChannelProvider TransportChannelProvider to use in the settings.
   * @return a {@link AccessApprovalSettings} bean configured with {@link TransportChannelProvider}
   *     bean.
   */
  @Bean
  @ConditionalOnMissingBean
  public AccessApprovalAdminSettings accessApprovalSettings(
      @Qualifier("defaultAccessApprovalTransportChannelProvider")
          TransportChannelProvider defaultTransportChannelProvider)
      throws IOException {
    AccessApprovalAdminSettings.Builder clientSettingsBuilder;
    if (this.clientProperties.getUseRest()) {
      clientSettingsBuilder = AccessApprovalAdminSettings.newHttpJsonBuilder();
      if (LOGGER.isTraceEnabled()) {
        LOGGER.trace("Using REST (HTTP/JSON) transport.");
      }
    } else {
      clientSettingsBuilder = AccessApprovalAdminSettings.newBuilder();
    }
    clientSettingsBuilder
        .setCredentialsProvider(this.credentialsProvider)
        .setTransportChannelProvider(defaultTransportChannelProvider)
        .setHeaderProvider(this.userAgentHeaderProvider());
    if (this.clientProperties.getQuotaProjectId() != null) {
      clientSettingsBuilder.setQuotaProjectId(this.clientProperties.getQuotaProjectId());
      if (LOGGER.isTraceEnabled()) {
        LOGGER.trace(
            "Quota project id set to "
                + this.clientProperties.getQuotaProjectId()
                + ", this overrides project id from credentials.");
      }
    }
    if (this.clientProperties.getExecutorThreadCount() != null) {
      ExecutorProvider executorProvider =
          AccessApprovalAdminSettings.defaultExecutorProviderBuilder()
              .setExecutorThreadCount(this.clientProperties.getExecutorThreadCount())
              .build();
      clientSettingsBuilder.setBackgroundExecutorProvider(executorProvider);
      if (LOGGER.isTraceEnabled()) {
        LOGGER.trace(
            "Background executor thread count is "
                + this.clientProperties.getExecutorThreadCount());
      }
    }
    Retry serviceRetry = clientProperties.getRetry();
    if (serviceRetry != null) {
      RetrySettings listApprovalRequestsRetrySettings =
          RetryUtil.updateRetrySettings(
              clientSettingsBuilder.listApprovalRequestsSettings().getRetrySettings(),
              serviceRetry);
      clientSettingsBuilder
          .listApprovalRequestsSettings()
          .setRetrySettings(listApprovalRequestsRetrySettings);

      RetrySettings getApprovalRequestRetrySettings =
          RetryUtil.updateRetrySettings(
              clientSettingsBuilder.getApprovalRequestSettings().getRetrySettings(), serviceRetry);
      clientSettingsBuilder
          .getApprovalRequestSettings()
          .setRetrySettings(getApprovalRequestRetrySettings);

      RetrySettings approveApprovalRequestRetrySettings =
          RetryUtil.updateRetrySettings(
              clientSettingsBuilder.approveApprovalRequestSettings().getRetrySettings(),
              serviceRetry);
      clientSettingsBuilder
          .approveApprovalRequestSettings()
          .setRetrySettings(approveApprovalRequestRetrySettings);

      RetrySettings dismissApprovalRequestRetrySettings =
          RetryUtil.updateRetrySettings(
              clientSettingsBuilder.dismissApprovalRequestSettings().getRetrySettings(),
              serviceRetry);
      clientSettingsBuilder
          .dismissApprovalRequestSettings()
          .setRetrySettings(dismissApprovalRequestRetrySettings);

      RetrySettings invalidateApprovalRequestRetrySettings =
          RetryUtil.updateRetrySettings(
              clientSettingsBuilder.invalidateApprovalRequestSettings().getRetrySettings(),
              serviceRetry);
      clientSettingsBuilder
          .invalidateApprovalRequestSettings()
          .setRetrySettings(invalidateApprovalRequestRetrySettings);

      RetrySettings getAccessApprovalSettingsRetrySettings =
          RetryUtil.updateRetrySettings(
              clientSettingsBuilder.getAccessApprovalSettingsSettings().getRetrySettings(),
              serviceRetry);
      clientSettingsBuilder
          .getAccessApprovalSettingsSettings()
          .setRetrySettings(getAccessApprovalSettingsRetrySettings);

      RetrySettings updateAccessApprovalSettingsRetrySettings =
          RetryUtil.updateRetrySettings(
              clientSettingsBuilder.updateAccessApprovalSettingsSettings().getRetrySettings(),
              serviceRetry);
      clientSettingsBuilder
          .updateAccessApprovalSettingsSettings()
          .setRetrySettings(updateAccessApprovalSettingsRetrySettings);

      RetrySettings deleteAccessApprovalSettingsRetrySettings =
          RetryUtil.updateRetrySettings(
              clientSettingsBuilder.deleteAccessApprovalSettingsSettings().getRetrySettings(),
              serviceRetry);
      clientSettingsBuilder
          .deleteAccessApprovalSettingsSettings()
          .setRetrySettings(deleteAccessApprovalSettingsRetrySettings);

      RetrySettings getAccessApprovalServiceAccountRetrySettings =
          RetryUtil.updateRetrySettings(
              clientSettingsBuilder.getAccessApprovalServiceAccountSettings().getRetrySettings(),
              serviceRetry);
      clientSettingsBuilder
          .getAccessApprovalServiceAccountSettings()
          .setRetrySettings(getAccessApprovalServiceAccountRetrySettings);

      if (LOGGER.isTraceEnabled()) {
        LOGGER.trace("Configured service-level retry settings from properties.");
      }
    }
    Retry listApprovalRequestsRetry = clientProperties.getListApprovalRequestsRetry();
    if (listApprovalRequestsRetry != null) {
      RetrySettings listApprovalRequestsRetrySettings =
          RetryUtil.updateRetrySettings(
              clientSettingsBuilder.listApprovalRequestsSettings().getRetrySettings(),
              listApprovalRequestsRetry);
      clientSettingsBuilder
          .listApprovalRequestsSettings()
          .setRetrySettings(listApprovalRequestsRetrySettings);
      if (LOGGER.isTraceEnabled()) {
        LOGGER.trace(
            "Configured method-level retry settings for listApprovalRequests from properties.");
      }
    }
    Retry getApprovalRequestRetry = clientProperties.getGetApprovalRequestRetry();
    if (getApprovalRequestRetry != null) {
      RetrySettings getApprovalRequestRetrySettings =
          RetryUtil.updateRetrySettings(
              clientSettingsBuilder.getApprovalRequestSettings().getRetrySettings(),
              getApprovalRequestRetry);
      clientSettingsBuilder
          .getApprovalRequestSettings()
          .setRetrySettings(getApprovalRequestRetrySettings);
      if (LOGGER.isTraceEnabled()) {
        LOGGER.trace(
            "Configured method-level retry settings for getApprovalRequest from properties.");
      }
    }
    Retry approveApprovalRequestRetry = clientProperties.getApproveApprovalRequestRetry();
    if (approveApprovalRequestRetry != null) {
      RetrySettings approveApprovalRequestRetrySettings =
          RetryUtil.updateRetrySettings(
              clientSettingsBuilder.approveApprovalRequestSettings().getRetrySettings(),
              approveApprovalRequestRetry);
      clientSettingsBuilder
          .approveApprovalRequestSettings()
          .setRetrySettings(approveApprovalRequestRetrySettings);
      if (LOGGER.isTraceEnabled()) {
        LOGGER.trace(
            "Configured method-level retry settings for approveApprovalRequest from properties.");
      }
    }
    Retry dismissApprovalRequestRetry = clientProperties.getDismissApprovalRequestRetry();
    if (dismissApprovalRequestRetry != null) {
      RetrySettings dismissApprovalRequestRetrySettings =
          RetryUtil.updateRetrySettings(
              clientSettingsBuilder.dismissApprovalRequestSettings().getRetrySettings(),
              dismissApprovalRequestRetry);
      clientSettingsBuilder
          .dismissApprovalRequestSettings()
          .setRetrySettings(dismissApprovalRequestRetrySettings);
      if (LOGGER.isTraceEnabled()) {
        LOGGER.trace(
            "Configured method-level retry settings for dismissApprovalRequest from properties.");
      }
    }
    Retry invalidateApprovalRequestRetry = clientProperties.getInvalidateApprovalRequestRetry();
    if (invalidateApprovalRequestRetry != null) {
      RetrySettings invalidateApprovalRequestRetrySettings =
          RetryUtil.updateRetrySettings(
              clientSettingsBuilder.invalidateApprovalRequestSettings().getRetrySettings(),
              invalidateApprovalRequestRetry);
      clientSettingsBuilder
          .invalidateApprovalRequestSettings()
          .setRetrySettings(invalidateApprovalRequestRetrySettings);
      if (LOGGER.isTraceEnabled()) {
        LOGGER.trace(
            "Configured method-level retry settings for invalidateApprovalRequest from properties.");
      }
    }
    Retry getAccessApprovalSettingsRetry = clientProperties.getGetAccessApprovalSettingsRetry();
    if (getAccessApprovalSettingsRetry != null) {
      RetrySettings getAccessApprovalSettingsRetrySettings =
          RetryUtil.updateRetrySettings(
              clientSettingsBuilder.getAccessApprovalSettingsSettings().getRetrySettings(),
              getAccessApprovalSettingsRetry);
      clientSettingsBuilder
          .getAccessApprovalSettingsSettings()
          .setRetrySettings(getAccessApprovalSettingsRetrySettings);
      if (LOGGER.isTraceEnabled()) {
        LOGGER.trace(
            "Configured method-level retry settings for getAccessApprovalSettings from properties.");
      }
    }
    Retry updateAccessApprovalSettingsRetry =
        clientProperties.getUpdateAccessApprovalSettingsRetry();
    if (updateAccessApprovalSettingsRetry != null) {
      RetrySettings updateAccessApprovalSettingsRetrySettings =
          RetryUtil.updateRetrySettings(
              clientSettingsBuilder.updateAccessApprovalSettingsSettings().getRetrySettings(),
              updateAccessApprovalSettingsRetry);
      clientSettingsBuilder
          .updateAccessApprovalSettingsSettings()
          .setRetrySettings(updateAccessApprovalSettingsRetrySettings);
      if (LOGGER.isTraceEnabled()) {
        LOGGER.trace(
            "Configured method-level retry settings for updateAccessApprovalSettings from properties.");
      }
    }
    Retry deleteAccessApprovalSettingsRetry =
        clientProperties.getDeleteAccessApprovalSettingsRetry();
    if (deleteAccessApprovalSettingsRetry != null) {
      RetrySettings deleteAccessApprovalSettingsRetrySettings =
          RetryUtil.updateRetrySettings(
              clientSettingsBuilder.deleteAccessApprovalSettingsSettings().getRetrySettings(),
              deleteAccessApprovalSettingsRetry);
      clientSettingsBuilder
          .deleteAccessApprovalSettingsSettings()
          .setRetrySettings(deleteAccessApprovalSettingsRetrySettings);
      if (LOGGER.isTraceEnabled()) {
        LOGGER.trace(
            "Configured method-level retry settings for deleteAccessApprovalSettings from properties.");
      }
    }
    Retry getAccessApprovalServiceAccountRetry =
        clientProperties.getGetAccessApprovalServiceAccountRetry();
    if (getAccessApprovalServiceAccountRetry != null) {
      RetrySettings getAccessApprovalServiceAccountRetrySettings =
          RetryUtil.updateRetrySettings(
              clientSettingsBuilder.getAccessApprovalServiceAccountSettings().getRetrySettings(),
              getAccessApprovalServiceAccountRetry);
      clientSettingsBuilder
          .getAccessApprovalServiceAccountSettings()
          .setRetrySettings(getAccessApprovalServiceAccountRetrySettings);
      if (LOGGER.isTraceEnabled()) {
        LOGGER.trace(
            "Configured method-level retry settings for getAccessApprovalServiceAccount from properties.");
      }
    }
    return clientSettingsBuilder.build();
  }

  /**
   * Provides a AccessApprovalClient bean configured with AccessApprovalSettings.
   *
   * @param accessApprovalSettings settings to configure an instance of client bean.
   * @return a {@link AccessApprovalClient} bean configured with {@link AccessApprovalSettings}
   */
  @Bean
  @ConditionalOnMissingBean
  public AccessApprovalAdminClient accessApprovalClient(
      AccessApprovalAdminSettings accessApprovalSettings) throws IOException {
    return AccessApprovalAdminClient.create(accessApprovalSettings);
  }

  private HeaderProvider userAgentHeaderProvider() {
    String springLibrary = "spring-autogen-access-approval";
    String version = this.getClass().getPackage().getImplementationVersion();
    return () -> Collections.singletonMap("user-agent", springLibrary + "/" + version);
  }
}
