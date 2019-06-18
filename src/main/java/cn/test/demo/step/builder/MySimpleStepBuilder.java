package cn.test.demo.step.builder;

import cn.test.demo.step.item.MySimpleChunkProcessor;
import org.springframework.batch.core.*;
import org.springframework.batch.core.annotation.*;
import org.springframework.batch.core.listener.StepListenerFactoryBean;
import org.springframework.batch.core.step.builder.AbstractTaskletStepBuilder;
import org.springframework.batch.core.step.builder.FaultTolerantStepBuilder;
import org.springframework.batch.core.step.builder.StepBuilderHelper;
import org.springframework.batch.core.step.item.ChunkOrientedTasklet;
import org.springframework.batch.core.step.item.SimpleChunkProvider;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.function.FunctionItemProcessor;
import org.springframework.batch.repeat.CompletionPolicy;
import org.springframework.batch.repeat.RepeatOperations;
import org.springframework.batch.repeat.policy.SimpleCompletionPolicy;
import org.springframework.batch.repeat.support.RepeatTemplate;
import org.springframework.batch.support.ReflectionUtils;
import org.springframework.util.Assert;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Function;

public class MySimpleStepBuilder<I, O> extends AbstractTaskletStepBuilder<MySimpleStepBuilder<I, O>> {

    private static final int DEFAULT_COMMIT_INTERVAL = 1;

    private ItemReader<? extends I> reader;

    private ItemWriter<? super O> writer;

    private ItemProcessor<? super I, ? extends O> processor;

    private Function<? super I, ? extends O> itemProcessorFunction;

    private int chunkSize = 0;

    private RepeatOperations chunkOperations;

    private CompletionPolicy completionPolicy;

    private Set<StepListener> itemListeners = new LinkedHashSet<>();

    private boolean readerTransactionalQueue = false;

    /**
     * Create a new builder initialized with any properties in the parent. The parent is copied, so it can be re-used.
     *
     * @param parent a parent helper containing common step properties
     */
    public MySimpleStepBuilder(StepBuilderHelper<?> parent) {
        super(parent);
    }

    /**
     * Create a new builder initialized with any properties in the parent. The parent is copied, so it can be re-used.
     *
     * @param parent a parent helper containing common step properties
     */
    protected MySimpleStepBuilder(MySimpleStepBuilder<I, O> parent) {
        super(parent);
        this.chunkSize = parent.chunkSize;
        this.completionPolicy = parent.completionPolicy;
        this.chunkOperations = parent.chunkOperations;
        this.reader = parent.reader;
        this.writer = parent.writer;
        this.processor = parent.processor;
        this.itemListeners = parent.itemListeners;
        this.readerTransactionalQueue = parent.readerTransactionalQueue;
    }

    public FaultTolerantStepBuilder<I, O> faultTolerant() {
        return new FaultTolerantStepBuilder<>(this);
    }

    /**
     * Build a step with the reader, writer, processor as provided.
     *
     * @see org.springframework.batch.core.step.builder.AbstractTaskletStepBuilder#build()
     */
    @Override
    public TaskletStep build() {

        registerStepListenerAsItemListener();
        registerAsStreamsAndListeners(reader, processor, writer);
        return super.build();
    }

    protected void registerStepListenerAsItemListener() {
        for (StepExecutionListener stepExecutionListener: properties.getStepExecutionListeners()){
            checkAndAddItemListener(stepExecutionListener);
        }
        for (ChunkListener chunkListener: this.chunkListeners){
            checkAndAddItemListener(chunkListener);
        }
    }

    @SuppressWarnings("unchecked")
    private void checkAndAddItemListener(StepListener stepListener) {
        if (stepListener instanceof ItemReadListener){
            listener((ItemReadListener<I>)stepListener);
        }
        if (stepListener instanceof ItemProcessListener){
            listener((ItemProcessListener<I,O>)stepListener);
        }
        if (stepListener instanceof ItemWriteListener){
            listener((ItemWriteListener<O>)stepListener);
        }
    }

    @Override
    protected Tasklet createTasklet() {
        Assert.state(reader != null, "ItemReader must be provided");
        Assert.state(writer != null, "ItemWriter must be provided");
        RepeatOperations repeatOperations = createChunkOperations();
        SimpleChunkProvider<I> chunkProvider = new SimpleChunkProvider<>(getReader(), repeatOperations);
        MySimpleChunkProcessor<I, O> chunkProcessor = new MySimpleChunkProcessor<>(getProcessor(), getWriter());
        chunkProvider.setListeners(new ArrayList<>(itemListeners));
        chunkProcessor.setListeners(new ArrayList<>(itemListeners));
        ChunkOrientedTasklet<I> tasklet = new ChunkOrientedTasklet<>(chunkProvider, chunkProcessor);
        tasklet.setBuffering(!readerTransactionalQueue);
        return tasklet;
    }

    /**
     * Sets the chunk size or commit interval for this step. This is the maximum number of items that will be read
     * before processing starts in a single transaction. Not compatible with {@link #completionPolicy}
     * .
     *
     * @param chunkSize the chunk size (a.k.a commit interval)
     * @return this for fluent chaining
     */
    public MySimpleStepBuilder<I, O> chunk(int chunkSize) {
        Assert.state(completionPolicy == null || chunkSize == 0,
                "You must specify either a chunkCompletionPolicy or a commitInterval but not both.");
        this.chunkSize = chunkSize;
        return this;
    }

    /**
     * Sets a completion policy for the chunk processing. Items are read until this policy determines that a chunk is
     * complete, giving more control than with just the {@link #chunk(int) chunk size} (or commit interval).
     *
     * @param completionPolicy a completion policy for the chunk
     * @return this for fluent chaining
     */
    public MySimpleStepBuilder<I, O> chunk(CompletionPolicy completionPolicy) {
        Assert.state(chunkSize == 0 || completionPolicy == null,
                "You must specify either a chunkCompletionPolicy or a commitInterval but not both.");
        this.completionPolicy = completionPolicy;
        return this;
    }

    /**
     * An item reader that provides a stream of items. Will be automatically registered as a {@link #stream(ItemStream)}
     * or listener if it implements the corresponding interface. By default assumed to be non-transactional.
     *
     * @see #readerTransactionalQueue
     * @param reader an item reader
     * @return this for fluent chaining
     */
    public MySimpleStepBuilder<I, O> reader(ItemReader<? extends I> reader) {
        this.reader = reader;
        return this;
    }

    /**
     * An item writer that writes a chunk of items. Will be automatically registered as a {@link #stream(ItemStream)} or
     * listener if it implements the corresponding interface.
     *
     * @param writer an item writer
     * @return this for fluent chaining
     */
    public MySimpleStepBuilder<I, O> writer(ItemWriter<? super O> writer) {
        this.writer = writer;
        return this;
    }

    /**
     * An item processor that processes or transforms a stream of items. Will be automatically registered as a
     * {@link #stream(ItemStream)} or listener if it implements the corresponding interface.
     *
     * @param processor an item processor
     * @return this for fluent chaining
     */
    public MySimpleStepBuilder<I, O> processor(ItemProcessor<? super I, ? extends O> processor) {
        this.processor = processor;
        return this;
    }

    /**
     * A {@link Function} to be delegated to as an {@link ItemProcessor}.  If this is set,
     * it will take precedence over any {@code ItemProcessor} configured via
     * {@link #processor(ItemProcessor)}.
     *
     * @param function the function to delegate item processing to
     * @return this for fluent chaining
     */
    public MySimpleStepBuilder<I, O> processor(Function<? super I, ? extends O> function) {
        this.itemProcessorFunction = function;
        return this;
    }

    /**
     * Sets a flag to say that the reader is transactional (usually a queue), which is to say that failed items might be
     * rolled back and re-presented in a subsequent transaction. Default is false, meaning that the items are read
     * outside a transaction and possibly cached.
     *
     * @return this for fluent chaining
     */
    public MySimpleStepBuilder<I, O> readerIsTransactionalQueue() {
        this.readerTransactionalQueue = true;
        return this;
    }

    /**
     * Registers objects using the annotation based listener configuration.
     *
     * @param listener the object that has a method configured with listener annotation
     * @return this for fluent chaining
     */
    @SuppressWarnings("unchecked")
    @Override
    public MySimpleStepBuilder<I, O> listener(Object listener) {
        super.listener(listener);

        Set<Method> itemListenerMethods = new HashSet<>();
        itemListenerMethods.addAll(ReflectionUtils.findMethod(listener.getClass(), BeforeRead.class));
        itemListenerMethods.addAll(ReflectionUtils.findMethod(listener.getClass(), AfterRead.class));
        itemListenerMethods.addAll(ReflectionUtils.findMethod(listener.getClass(), BeforeProcess.class));
        itemListenerMethods.addAll(ReflectionUtils.findMethod(listener.getClass(), AfterProcess.class));
        itemListenerMethods.addAll(ReflectionUtils.findMethod(listener.getClass(), BeforeWrite.class));
        itemListenerMethods.addAll(ReflectionUtils.findMethod(listener.getClass(), AfterWrite.class));
        itemListenerMethods.addAll(ReflectionUtils.findMethod(listener.getClass(), OnReadError.class));
        itemListenerMethods.addAll(ReflectionUtils.findMethod(listener.getClass(), OnProcessError.class));
        itemListenerMethods.addAll(ReflectionUtils.findMethod(listener.getClass(), OnWriteError.class));

        if(itemListenerMethods.size() > 0) {
            StepListenerFactoryBean factory = new StepListenerFactoryBean();
            factory.setDelegate(listener);
            itemListeners.add((StepListener) factory.getObject());
        }

        @SuppressWarnings("unchecked")
        MySimpleStepBuilder<I, O> result = this;
        return result;
    }


    /**
     * Register an item reader listener.
     *
     * @param listener the listener to register
     * @return this for fluent chaining
     */
    public MySimpleStepBuilder<I, O> listener(ItemReadListener<? super I> listener) {
        itemListeners.add(listener);
        return this;
    }

    /**
     * Register an item writer listener.
     *
     * @param listener the listener to register
     * @return this for fluent chaining
     */
    public MySimpleStepBuilder<I, O> listener(ItemWriteListener<? super O> listener) {
        itemListeners.add(listener);
        return this;
    }

    /**
     * Register an item processor listener.
     *
     * @param listener the listener to register
     * @return this for fluent chaining
     */
    public MySimpleStepBuilder<I, O> listener(ItemProcessListener<? super I, ? super O> listener) {
        itemListeners.add(listener);
        return this;
    }

    /**
     * Instead of a {@link #chunk(int) chunk size} or {@link #chunk(CompletionPolicy) completion policy} you can provide
     * a complete repeat operations instance that handles the iteration over the item reader.
     *
     * @param repeatTemplate a complete repeat template for the chunk
     * @return this for fluent chaining
     */
    public MySimpleStepBuilder<I, O> chunkOperations(RepeatOperations repeatTemplate) {
        this.chunkOperations = repeatTemplate;
        return this;
    }

    protected RepeatOperations createChunkOperations() {
        RepeatOperations repeatOperations = chunkOperations;
        if (repeatOperations == null) {
            RepeatTemplate repeatTemplate = new RepeatTemplate();
            repeatTemplate.setCompletionPolicy(getChunkCompletionPolicy());
            repeatOperations = repeatTemplate;
        }
        return repeatOperations;
    }

    protected ItemReader<? extends I> getReader() {
        return reader;
    }

    protected ItemWriter<? super O> getWriter() {
        return writer;
    }

    protected ItemProcessor<? super I, ? extends O> getProcessor() {
        if(this.itemProcessorFunction != null) {
            this.processor = new FunctionItemProcessor<>(this.itemProcessorFunction);
        }

        return processor;
    }

    protected int getChunkSize() {
        return chunkSize;
    }

    protected boolean isReaderTransactionalQueue() {
        return readerTransactionalQueue;
    }

    protected Set<StepListener> getItemListeners() {
        return itemListeners;
    }

    /**
     * @return a {@link CompletionPolicy} consistent with the chunk size and injected policy (if present).
     */
    protected CompletionPolicy getChunkCompletionPolicy() {
        Assert.state(!(completionPolicy != null && chunkSize > 0),
                "You must specify either a chunkCompletionPolicy or a commitInterval but not both.");
        Assert.state(chunkSize >= 0, "The commitInterval must be positive or zero (for default value).");

        if (completionPolicy != null) {
            return completionPolicy;
        }
        if (chunkSize == 0) {
            logger.info("Setting commit interval to default value (" + DEFAULT_COMMIT_INTERVAL + ")");
            chunkSize = DEFAULT_COMMIT_INTERVAL;
        }
        return new SimpleCompletionPolicy(chunkSize);
    }

    protected void registerAsStreamsAndListeners(ItemReader<? extends I> itemReader,
                                                 ItemProcessor<? super I, ? extends O> itemProcessor, ItemWriter<? super O> itemWriter) {
        for (Object itemHandler : new Object[] { itemReader, itemWriter, itemProcessor }) {
            if (itemHandler instanceof ItemStream) {
                stream((ItemStream) itemHandler);
            }
            if (StepListenerFactoryBean.isListener(itemHandler)) {
                StepListener listener = StepListenerFactoryBean.getListener(itemHandler);
                if (listener instanceof StepExecutionListener) {
                    listener((StepExecutionListener) listener);
                }
                if (listener instanceof ChunkListener) {
                    listener((ChunkListener) listener);
                }
                if (listener instanceof ItemReadListener<?> || listener instanceof ItemProcessListener<?, ?>
                        || listener instanceof ItemWriteListener<?>) {
                    itemListeners.add(listener);
                }
            }
        }
    }
}
