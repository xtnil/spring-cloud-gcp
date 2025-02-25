## Secret Manager

[Google Cloud Secret Manager](https://cloud.google.com/secret-manager)
is a secure and convenient method for storing API keys, passwords,
certificates, and other sensitive data. A detailed summary of its
features can be found in the [Secret Manager
documentation](https://cloud.google.com/blog/products/identity-security/introducing-google-clouds-secret-manager).

Spring Framework on Google Cloud provides:

  - A config data resource which allows you to specify and load the secrets of your Google Cloud project into your application context using [Spring Boot's Config Data API](https://spring.io/blog/2020/08/14/config-file-processing-in-spring-boot-2-4).

  - A `SecretManagerTemplate` which allows you to read, write, and
    update secrets in Secret Manager.

### Dependency Setup

To begin using this library, add the
`spring-cloud-gcp-starter-secretmanager` artifact to your project.

Maven coordinates,
using [Spring Framework on Google Cloud BOM](getting-started.xml#bill-of-materials):

``` xml
<dependency>
  <groupId>com.google.cloud</groupId>
  <artifactId>spring-cloud-gcp-starter-secretmanager</artifactId>
</dependency>
```

Gradle coordinates:

    dependencies {
      implementation("com.google.cloud:spring-cloud-gcp-starter-secretmanager")
    }

#### Configuration

By default, Spring Framework on Google Cloud Secret Manager will authenticate using
Application Default Credentials. This can be overridden using the
authentication properties.

|                                                                                                                 |                                                                                                                                                                                       |          |                                                                                                                                 |
|-----------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------| -------- |---------------------------------------------------------------------------------------------------------------------------------|
| Name                                                                                                            | Description                                                                                                                                                                           | Required | Default value                                                                                                                   |
| `spring.cloud.gcp.secretmanager.enabled`                                                           | Enables the Secret Manager integration.                                                                                                         | No       | `true`                                                                                                                          |
| `spring.cloud.gcp.secretmanager.credentials.location`                                                           | OAuth2 credentials for authenticating to the Google Cloud Secret Manager API.                                                                                                         | No       | By default, infers credentials from [Application Default Credentials](https://cloud.google.com/docs/authentication/production). |
| `spring.cloud.gcp.secretmanager.credentials.encoded-key`                                                        | Base64-encoded contents of OAuth2 account private key for authenticating to the Google Cloud Secret Manager API.                                                                      | No       | By default, infers credentials from [Application Default Credentials](https://cloud.google.com/docs/authentication/production). |
| `spring.cloud.gcp.secretmanager.project-id`                                                                     | The default Google Cloud project used to access Secret Manager API for the template and property source.                                                                                       | No       | By default, infers the project from [Application Default Credentials](https://cloud.google.com/docs/authentication/production). |
| `spring.cloud.gcp.secretmanager.allow-default-secret`                                                           | Define the behavior when accessing a non-existent secret string/bytes. If set to `true`, `null` will be returned when accessing a non-existent secret; otherwise throwing an exception. | No | `false`                                                                                                                         |

### Secret Manager Config Data Resource

The Spring Framework on Google Cloud integration for Google Cloud Secret Manager enables you to use Secret Manager as an external config data resource.
This allows you to specify and load secrets from Google Cloud Secret Manager as properties into the application context using [Spring Boot's Config Data API](https://spring.io/blog/2020/08/14/config-file-processing-in-spring-boot-2-4).

The Secret Manager config data resource uses the following syntax to specify secrets:

    # 1. Long form - specify the project ID, secret ID, and version
    sm://projects/<project-id>/secrets/<secret-id>/versions/<version-id>}
    
    # 2.  Long form - specify project ID, secret ID, and use latest version
    sm://projects/<project-id>/secrets/<secret-id>
    
    # 3. Short form - specify project ID, secret ID, and version
    sm://<project-id>/<secret-id>/<version-id>
    
    # 4. Short form - default project; specify secret + version
    #
    # The project is inferred from the spring.cloud.gcp.secretmanager.project-id setting
    # in your application.properties (see Configuration) or from application-default credentials if
    # this is not set.
    sm://<secret-id>/<version>
    
    # 5. Shortest form - specify secret ID, use default project and latest version.
    sm://<secret-id>

You can use this syntax in the following places:

1. In your `application.properties` file:
    
        # Example of the project-secret long-form syntax.
        spring.config.import=sm://
        spring.datasource.password=${sm://projects/my-gcp-project/secrets/my-secret}
   The former is used to enable [Spring Boot's Config Data API](https://spring.io/blog/2020/08/14/config-file-processing-in-spring-boot-2-4).

3. Access the value using the `@Value` annotation.
    
        // Example of using shortest form syntax.
        @Value("${sm://my-secret}")

### Secret Manager Template

The `SecretManagerTemplate` class simplifies operations of creating,
updating, and reading secrets.

To begin using this class, you may inject an instance of the class using
`@Autowired` after adding the starter dependency to your project.

``` java
@Autowired
private SecretManagerTemplate secretManagerTemplate;
```

Please consult
[`SecretManagerOperations`](https://github.com/GoogleCloudPlatform/spring-cloud-gcp/blob/main/spring-cloud-gcp-secretmanager/src/main/java/com/google/cloud/spring/secretmanager/SecretManagerOperations.java)
for information on what operations are available for the Secret Manager
template.

### Refresh secrets without restarting the application

1. Before running your application, change the project's configuration files as follows:

- import the actuator starter dependency to your project,

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>

- add the following property to your project's `application.properties`.

        management.endpoints.web.exposure.include=refresh

- finally, add the following property to your project's `bootstrap.properties` to disable
  Secret Manager bootstrap phrase.
 
        spring.cloud.gcp.secretmanager.legacy=false


2. After running the application, update your secret stored in the Secret Manager.

3. To refresh the secret, send the following command to your application sever:

         curl -X POST http://[host]:[port]/actuator/refresh

    Note that only `@ConfigurationProperties` annotated with `@RefreshScope` support updating secrets without restarting the application.

### Allow default secret

By default, when accessing a non-existed secret, the Secret Manager will throw an exception.

However, if your want to use a default value in such a scenario, you can add the following property to project's properties:

        spring.cloud.gcp.secretmanager.allow-default-secret=true

Therefore, a variable annotated with `@Value("${${sm://application-fake}:DEFAULT}")` will be resolved as `DEFAULT` when there is no `application-fake` in Secret Manager and `application-fake` is NOT a valid application property.

### Sample

A [Secret Manager Sample
Application](https://github.com/GoogleCloudPlatform/spring-cloud-gcp/tree/main/spring-cloud-gcp-samples/spring-cloud-gcp-secretmanager-sample)
is provided which demonstrates basic property source loading and usage
of the template class.
