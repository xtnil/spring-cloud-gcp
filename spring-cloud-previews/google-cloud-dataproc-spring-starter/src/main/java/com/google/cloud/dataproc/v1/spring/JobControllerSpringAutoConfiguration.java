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

package com.google.cloud.dataproc.v1.spring;

import com.google.api.core.BetaApi;
import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.ExecutorProvider;
import com.google.api.gax.retrying.RetrySettings;
import com.google.api.gax.rpc.HeaderProvider;
import com.google.api.gax.rpc.TransportChannelProvider;
import com.google.cloud.dataproc.v1.JobControllerClient;
import com.google.cloud.dataproc.v1.JobControllerSettings;
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
 * Auto-configuration for {@link JobControllerClient}.
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
@ConditionalOnClass(JobControllerClient.class)
@ConditionalOnProperty(
    value = "com.google.cloud.dataproc.v1.job-controller.enabled",
    matchIfMissing = true)
@EnableConfigurationProperties(JobControllerSpringProperties.class)
public class JobControllerSpringAutoConfiguration {
  private final JobControllerSpringProperties clientProperties;
  private final CredentialsProvider credentialsProvider;
  private static final Log LOGGER = LogFactory.getLog(JobControllerSpringAutoConfiguration.class);

  protected JobControllerSpringAutoConfiguration(
      JobControllerSpringProperties clientProperties, CredentialsProvider credentialsProvider)
      throws IOException {
    this.clientProperties = clientProperties;
    if (this.clientProperties.getCredentials().hasKey()) {
      if (LOGGER.isTraceEnabled()) {
        LOGGER.trace("Using credentials from JobController-specific configuration");
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
  @ConditionalOnMissingBean(name = "defaultJobControllerTransportChannelProvider")
  public TransportChannelProvider defaultJobControllerTransportChannelProvider() {
    if (this.clientProperties.getUseRest()) {
      return JobControllerSettings.defaultHttpJsonTransportProviderBuilder().build();
    }
    return JobControllerSettings.defaultTransportChannelProvider();
  }

  /**
   * Provides a JobControllerSettings bean configured to use the default credentials provider
   * (obtained with jobControllerCredentials()) and its default transport channel provider
   * (defaultJobControllerTransportChannelProvider()). It also configures the quota project ID if
   * provided. It will configure an executor provider in case there is more than one thread
   * configured in the client
   *
   * <p>Retry settings are also configured from service-level and method-level properties specified
   * in JobControllerSpringProperties. Method-level properties will take precedence over
   * service-level properties if available, and client library defaults will be used if neither are
   * specified.
   *
   * @param defaultTransportChannelProvider TransportChannelProvider to use in the settings.
   * @return a {@link JobControllerSettings} bean configured with {@link TransportChannelProvider}
   *     bean.
   */
  @Bean
  @ConditionalOnMissingBean
  public JobControllerSettings jobControllerSettings(
      @Qualifier("defaultJobControllerTransportChannelProvider")
          TransportChannelProvider defaultTransportChannelProvider)
      throws IOException {
    JobControllerSettings.Builder clientSettingsBuilder;
    if (this.clientProperties.getUseRest()) {
      clientSettingsBuilder = JobControllerSettings.newHttpJsonBuilder();
      if (LOGGER.isTraceEnabled()) {
        LOGGER.trace("Using REST (HTTP/JSON) transport.");
      }
    } else {
      clientSettingsBuilder = JobControllerSettings.newBuilder();
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
          JobControllerSettings.defaultExecutorProviderBuilder()
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
      RetrySettings submitJobRetrySettings =
          RetryUtil.updateRetrySettings(
              clientSettingsBuilder.submitJobSettings().getRetrySettings(), serviceRetry);
      clientSettingsBuilder.submitJobSettings().setRetrySettings(submitJobRetrySettings);

      RetrySettings getJobRetrySettings =
          RetryUtil.updateRetrySettings(
              clientSettingsBuilder.getJobSettings().getRetrySettings(), serviceRetry);
      clientSettingsBuilder.getJobSettings().setRetrySettings(getJobRetrySettings);

      RetrySettings listJobsRetrySettings =
          RetryUtil.updateRetrySettings(
              clientSettingsBuilder.listJobsSettings().getRetrySettings(), serviceRetry);
      clientSettingsBuilder.listJobsSettings().setRetrySettings(listJobsRetrySettings);

      RetrySettings updateJobRetrySettings =
          RetryUtil.updateRetrySettings(
              clientSettingsBuilder.updateJobSettings().getRetrySettings(), serviceRetry);
      clientSettingsBuilder.updateJobSettings().setRetrySettings(updateJobRetrySettings);

      RetrySettings cancelJobRetrySettings =
          RetryUtil.updateRetrySettings(
              clientSettingsBuilder.cancelJobSettings().getRetrySettings(), serviceRetry);
      clientSettingsBuilder.cancelJobSettings().setRetrySettings(cancelJobRetrySettings);

      RetrySettings deleteJobRetrySettings =
          RetryUtil.updateRetrySettings(
              clientSettingsBuilder.deleteJobSettings().getRetrySettings(), serviceRetry);
      clientSettingsBuilder.deleteJobSettings().setRetrySettings(deleteJobRetrySettings);

      if (LOGGER.isTraceEnabled()) {
        LOGGER.trace("Configured service-level retry settings from properties.");
      }
    }
    Retry submitJobRetry = clientProperties.getSubmitJobRetry();
    if (submitJobRetry != null) {
      RetrySettings submitJobRetrySettings =
          RetryUtil.updateRetrySettings(
              clientSettingsBuilder.submitJobSettings().getRetrySettings(), submitJobRetry);
      clientSettingsBuilder.submitJobSettings().setRetrySettings(submitJobRetrySettings);
      if (LOGGER.isTraceEnabled()) {
        LOGGER.trace("Configured method-level retry settings for submitJob from properties.");
      }
    }
    Retry getJobRetry = clientProperties.getGetJobRetry();
    if (getJobRetry != null) {
      RetrySettings getJobRetrySettings =
          RetryUtil.updateRetrySettings(
              clientSettingsBuilder.getJobSettings().getRetrySettings(), getJobRetry);
      clientSettingsBuilder.getJobSettings().setRetrySettings(getJobRetrySettings);
      if (LOGGER.isTraceEnabled()) {
        LOGGER.trace("Configured method-level retry settings for getJob from properties.");
      }
    }
    Retry listJobsRetry = clientProperties.getListJobsRetry();
    if (listJobsRetry != null) {
      RetrySettings listJobsRetrySettings =
          RetryUtil.updateRetrySettings(
              clientSettingsBuilder.listJobsSettings().getRetrySettings(), listJobsRetry);
      clientSettingsBuilder.listJobsSettings().setRetrySettings(listJobsRetrySettings);
      if (LOGGER.isTraceEnabled()) {
        LOGGER.trace("Configured method-level retry settings for listJobs from properties.");
      }
    }
    Retry updateJobRetry = clientProperties.getUpdateJobRetry();
    if (updateJobRetry != null) {
      RetrySettings updateJobRetrySettings =
          RetryUtil.updateRetrySettings(
              clientSettingsBuilder.updateJobSettings().getRetrySettings(), updateJobRetry);
      clientSettingsBuilder.updateJobSettings().setRetrySettings(updateJobRetrySettings);
      if (LOGGER.isTraceEnabled()) {
        LOGGER.trace("Configured method-level retry settings for updateJob from properties.");
      }
    }
    Retry cancelJobRetry = clientProperties.getCancelJobRetry();
    if (cancelJobRetry != null) {
      RetrySettings cancelJobRetrySettings =
          RetryUtil.updateRetrySettings(
              clientSettingsBuilder.cancelJobSettings().getRetrySettings(), cancelJobRetry);
      clientSettingsBuilder.cancelJobSettings().setRetrySettings(cancelJobRetrySettings);
      if (LOGGER.isTraceEnabled()) {
        LOGGER.trace("Configured method-level retry settings for cancelJob from properties.");
      }
    }
    Retry deleteJobRetry = clientProperties.getDeleteJobRetry();
    if (deleteJobRetry != null) {
      RetrySettings deleteJobRetrySettings =
          RetryUtil.updateRetrySettings(
              clientSettingsBuilder.deleteJobSettings().getRetrySettings(), deleteJobRetry);
      clientSettingsBuilder.deleteJobSettings().setRetrySettings(deleteJobRetrySettings);
      if (LOGGER.isTraceEnabled()) {
        LOGGER.trace("Configured method-level retry settings for deleteJob from properties.");
      }
    }
    return clientSettingsBuilder.build();
  }

  /**
   * Provides a JobControllerClient bean configured with JobControllerSettings.
   *
   * @param jobControllerSettings settings to configure an instance of client bean.
   * @return a {@link JobControllerClient} bean configured with {@link JobControllerSettings}
   */
  @Bean
  @ConditionalOnMissingBean
  public JobControllerClient jobControllerClient(JobControllerSettings jobControllerSettings)
      throws IOException {
    return JobControllerClient.create(jobControllerSettings);
  }

  private HeaderProvider userAgentHeaderProvider() {
    String springLibrary = "spring-autogen-job-controller";
    String version = this.getClass().getPackage().getImplementationVersion();
    return () -> Collections.singletonMap("user-agent", springLibrary + "/" + version);
  }
}
