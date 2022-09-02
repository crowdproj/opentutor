### flashcard-kt ::: logs-lib

This module contains a custom logger-lib, which wraps logback. It could be used in a web-application in conjunction with ELK (Elasticsearch + Logstash + Kafka) stack.

Dependencies:
- `ch.qos.logback:logback-classic`
- `net.logstash.logback:logstash-logback-encoder`

Note that log-configuration is in client's responsibility.     
Example of config for ELK:
```xml
<configuration debug="true">
    <property name="LOGS_KAFKA_HOSTS" value="${BOOTSTRAP_SERVERS:-localhost:9094}"/>
    <property name="LOGS_KAFKA_TOPIC" value="${LOGS_KAFKA_TOPIC:-app-logs}"/>
    <property name="SERVICE_NAME" value="${SERVICE_NAME:-application}"/>
    <property name="CLIENT_LOG_LEVEL" value="${CLIENT_LOG_LEVEL:-info}"/>
    <property name="COMMON_LOG_LEVEL" value="${COMMON_LOG_LEVEL:-error}"/>
    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level[%marker] %logger{36} - %msg%n%mdc%n</pattern>
        </encoder>
    </appender>
    <appender name="asyncLogKafka" class="net.logstash.logback.appender.LoggingEventAsyncDisruptorAppender">
        <if condition='!property("LOGS_KAFKA_HOSTS").equals("LOGS_KAFKA_HOSTS_IS_UNDEFINED") &amp;&amp; !property("LOGS_KAFKA_HOSTS").isEmpty()'>
            <then>
                <appender name="kafkaVerboseAppender" class="com.github.danielwegener.logback.kafka.KafkaAppender">
                    <encoder class="net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder">
                        <providers>
                            <timestamp/>
                            <version/>
                            <pattern>
                                <pattern>
                                    {
                                    "component": "${SERVICE_NAME}",
                                    "container-id": "${HOSTNAME}"
                                    }
                                </pattern>
                            </pattern>
                            <message/>
                            <loggerName/>
                            <threadName/>
                            <logLevel/>
                            <logstashMarkers/>
                            <callerData/>
                            <stackTrace/>
                            <context/>
                            <mdc/>
                            <logstashMarkers/>
                            <arguments/>
                            <tags/>
                        </providers>
                    </encoder>
                    <topic>${LOGS_KAFKA_TOPIC}</topic>
                    <deliveryStrategy class="com.github.danielwegener.logback.kafka.delivery.AsynchronousDeliveryStrategy"/>
                    <producerConfig>bootstrap.servers=${LOGS_KAFKA_HOSTS}</producerConfig>
                </appender>
            </then>
        </if>
    </appender>
    <logger name="com.example.client" level="${CLIENT_LOG_LEVEL}" additivity="false">
        <appender-ref ref="asyncLogKafka"/>
        <appender-ref ref="STDOUT"/>
    </logger>
    <root level="${COMMON_LOG_LEVEL}">
        <appender-ref ref="asyncLogKafka"/>
        <appender-ref ref="STDOUT"/>
    </root>
</configuration>
```
