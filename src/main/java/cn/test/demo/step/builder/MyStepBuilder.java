package cn.test.demo.step.builder;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.core.step.builder.*;
import org.springframework.batch.core.step.tasklet.Tasklet;

public class MyStepBuilder  extends StepBuilderHelper<MyStepBuilder> {

    /**
     * Initialize a step builder for a step with the given name.
     *
     * @param name the name of the step
     */
    public MyStepBuilder(String name) {
        super(name);
    }

    /**
     * Build a step with a custom tasklet, not necessarily item processing.
     *
     * @param tasklet a tasklet
     * @return a {@link TaskletStepBuilder}
     */
    public TaskletStepBuilder tasklet(Tasklet tasklet) {
        return new TaskletStepBuilder(this).tasklet(tasklet);
    }

    /**
     * Build a step that processes items in chunks with the size provided. To extend the step to being fault tolerant,
     * call the {@link SimpleStepBuilder#faultTolerant()} method on the builder. In most cases you will want to
     * parameterize your call to this method, to preserve the type safety of your readers and writers, e.g.
     *
     * <pre>
     * new StepBuilder(&quot;step1&quot;).&lt;Order, Ledger&gt; chunk(100).reader(new OrderReader()).writer(new LedgerWriter())
     * // ... etc.
     * </pre>
     *
     * @param chunkSize the chunk size (commit interval)
     * @return a {@link SimpleStepBuilder}
     * @param <I> the type of item to be processed as input
     * @param <O> the type of item to be output
     */
    public <I, O> MySimpleStepBuilder<I, O> chunk(int chunkSize) {
        return new MySimpleStepBuilder<I, O>(this).chunk(chunkSize);
    }

    /**
     * Create a partition step builder for a remote (or local) step.
     *
     * @param stepName the name of the remote or delegate step
     * @param partitioner a partitioner to be used to construct new step executions
     * @return a {@link PartitionStepBuilder}
     */
    public PartitionStepBuilder partitioner(String stepName, Partitioner partitioner) {
        return new PartitionStepBuilder(this).partitioner(stepName, partitioner);
    }

    /**
     * Create a partition step builder for a remote (or local) step.
     *
     * @param step the step to execute in parallel
     * @return a PartitionStepBuilder
     */
    public PartitionStepBuilder partitioner(Step step) {
        return new PartitionStepBuilder(this).step(step);
    }

    /**
     * Create a new step builder that will execute a job.
     *
     * @param job a job to execute
     * @return a {@link JobStepBuilder}
     */
    public JobStepBuilder job(Job job) {
        return new JobStepBuilder(this).job(job);
    }

    /**
     * Create a new step builder that will execute a flow.
     *
     * @param flow a flow to execute
     * @return a {@link FlowStepBuilder}
     */
    public FlowStepBuilder flow(Flow flow) {
        return new FlowStepBuilder(this).flow(flow);
    }


}
