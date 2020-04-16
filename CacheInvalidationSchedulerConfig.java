package com.example.core.services.config;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(name = "Cache invalidation scheduler", description = "cache invaldation scheduler.")
public @interface CacheInvalidationSchedulerConfig {
    String DEFAULT_SCHEDULER_EXPRESSION = "0 0 23 * * ?";

    @AttributeDefinition(name = "Cron expression defining when this Scheduled Service will run", description = "Default value ('0 0 23 * * ?') will run this job every day at 11PM EST", type = AttributeType.STRING)
    String scheduler_expression() default DEFAULT_SCHEDULER_EXPRESSION;

    @AttributeDefinition(name = "Enabled", description = "True, if scheduler service is enabled", type = AttributeType.BOOLEAN)
    public boolean enabled() default true;

    @AttributeDefinition(name = "Invalidation Paths", description = "invalidation Paths", type = AttributeType.STRING)
    String[] invalidationPaths() default {"/content/example/example.json"};
}
