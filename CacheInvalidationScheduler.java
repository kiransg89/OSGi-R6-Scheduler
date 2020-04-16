package com.example.core.scheduler;

import java.util.Map;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.commons.scheduler.ScheduleOptions;
import org.apache.sling.commons.scheduler.Scheduler;
import org.apache.sling.settings.SlingSettingsService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.adobe.acs.commons.replication.dispatcher.DispatcherFlusher;
import com.day.cq.replication.Agent;
import com.day.cq.replication.ReplicationActionType;
import com.day.cq.replication.ReplicationException;
import com.day.cq.replication.ReplicationResult;
import com.example.core.services.ExampleResourceResolverService;
import com.example.core.services.config.CacheInvalidationSchedulerConfig;

@Component(immediate = true, service = CacheInvalidationScheduler.class)
@Designate(ocd = CacheInvalidationSchedulerConfig.class)
public class CacheInvalidationScheduler implements Runnable {

    private static final Logger LOG =
            LoggerFactory.getLogger(CacheInvalidationScheduler.class);

    /**
     * Id of the scheduler based on its name
     */
    private String schedulerJobName;
    private String[] invalidationPaths;
    final ReplicationActionType replicationActionType =
            ReplicationActionType.valueOf(ReplicationActionType.DELETE.name());
    ResourceResolver flushingResourceResolver = null;

    @Reference
    private Scheduler scheduler;

    @Reference
    private SlingSettingsService slingSettings;

    @Reference
    private ExampleResourceResolverService resourceResolver;

    @Reference
    private DispatcherFlusher dispatcherFlusher;

    @Activate
    @Modified
    protected void activate(
            CacheInvalidationSchedulerConfig cacheInvalidationSchedulerConfig)
            throws Exception {
        if (isAuthor()) {
            /**
             * Creating the scheduler id
             */
            this.schedulerJobName = this.getClass().getSimpleName();
            this.invalidationPaths = cacheInvalidationSchedulerConfig.invalidationPaths();
            addScheduler(cacheInvalidationSchedulerConfig);
        }
    }

    /**
     * @see Runnable#run().
     */
    @Override
    public final void run() {
        LOG.debug("Running example API Invoker scheduler");
        flushingResourceResolver = resourceResolver.getResourceResolver();
        if (ArrayUtils.isNotEmpty(invalidationPaths)) {
            Map<Agent, ReplicationResult> results;
            try {
                results = dispatcherFlusher.flush(flushingResourceResolver, replicationActionType, true,
                        invalidationPaths);
                for (final Map.Entry<Agent, ReplicationResult> entry : results.entrySet()) {
                    final Agent agent = entry.getKey();
                    final ReplicationResult result = entry.getValue();
                    LOG.debug("Replication using agent {} is succuess {}", agent.getId(),
                            result.isSuccess() && result.getCode() == SlingHttpServletResponse.SC_OK);
                }
            } catch (ReplicationException exception) {
                LOG.debug("Replication exception occured in Security question invalidation scheduler {}",
                        exception);
            } finally {
                resourceResolver.closeResourceResolver(flushingResourceResolver);
            }
        }
        LOG.debug("example API Invoker scheduler job finished.");
    }



    @Deactivate
    protected void deactivate() {

        /**
         * Removing the scheduler
         */
        removeScheduler();
    }

    /**
     * This method adds the scheduler
     *
     * @param config
     */
    private void addScheduler(
            CacheInvalidationSchedulerConfig cacheInvalidationSchedulerConfig) {

        /**
         * Check if the scheduler is enabled
         */
        if (cacheInvalidationSchedulerConfig.enabled()) {

            /**
             * Scheduler option takes the cron expression as a parameter and run accordingly
             */
            ScheduleOptions scheduleOptions =
                    scheduler.EXPR(cacheInvalidationSchedulerConfig.scheduler_expression());

            /**
             * Adding some parameters
             */
            scheduleOptions.name(schedulerJobName);
            scheduleOptions.canRunConcurrently(false);

            /**
             * Scheduling the job
             */
            scheduler.schedule(this, scheduleOptions);

            LOG.info("{} Scheduler added", schedulerJobName);
        } else {
            LOG.info("Scheduler is disabled");
            removeScheduler();
        }

    }

    /**
     * This method removes the scheduler
     */
    private void removeScheduler() {

        LOG.info("Removing scheduler: {}", schedulerJobName);

        /**
         * Unscheduling/removing the scheduler
         */
        scheduler.unschedule(String.valueOf(schedulerJobName));
    }

    /**
     * It is use to check whether AEM is running in Publish mode or not.
     *
     * @return Returns true is AEM is in publish mode, false otherwise
     */
    public boolean isAuthor() {
        return this.slingSettings.getRunModes().contains("author");
    }
}
