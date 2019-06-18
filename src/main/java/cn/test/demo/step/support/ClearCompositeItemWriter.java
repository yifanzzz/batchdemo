package cn.test.demo.step.support;

import org.springframework.batch.item.*;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import java.util.List;

public class ClearCompositeItemWriter<T> implements ItemStreamWriter<T>, InitializingBean {

        private List<ItemWriter<? super T>> delegates;

        private boolean ignoreItemStream = false;

        private boolean isOrder = false;

        private List<ItemWriter<? super T>> actualList;

        /**
         * Establishes the policy whether to call the open, close, or update methods for the
         * item writer delegates associated with the ClearCompositeItemWriter.
         *
         * @param ignoreItemStream if false the delegates' open, close, or update methods will
         * be called when the corresponding methods on the ClearCompositeItemWriter are called. If
         * true the delegates' open, close, nor update methods will not be called (default is false).
         */
        public void setIgnoreItemStream(boolean ignoreItemStream) {
        this.ignoreItemStream = ignoreItemStream;
    }

        @Override
        public void write(List<? extends T> item) throws Exception {
            if(!isOrder){
                delegates = actualList;
            }
            for (ItemWriter<? super T> writer : delegates) {
                writer.write(item);
            }
    }

        @Override
        public void afterPropertiesSet() throws Exception {
        Assert.notNull(delegates, "The 'delegates' may not be null");
        Assert.notEmpty(delegates, "The 'delegates' may not be empty");
    }

        /**
         * The list of item writers to use as delegates. Items are written to each of the
         * delegates.
         *
         * @param delegates the list of delegates to use.  The delegates list must not be null nor be empty.
         */
        public void setDelegates(List<ItemWriter<? super T>> delegates) {
        this.delegates = delegates;
    }

        @Override
        public void close() throws ItemStreamException {
        for (ItemWriter<? super T> writer : delegates) {
            if (!ignoreItemStream && (writer instanceof ItemStream)) {
                ((ItemStream) writer).close();
            }
        }
    }

        @Override
        public void open(ExecutionContext executionContext) throws ItemStreamException {
        for (ItemWriter<? super T> writer : delegates) {
            if (!ignoreItemStream && (writer instanceof ItemStream)) {
                ((ItemStream) writer).open(executionContext);
            }
        }
    }

        @Override
        public void update(ExecutionContext executionContext) throws ItemStreamException {
        for (ItemWriter<? super T> writer : delegates) {
            if (!ignoreItemStream && (writer instanceof ItemStream)) {
                ((ItemStream) writer).update(executionContext);
            }
        }
    }
    public void setActualList(List<ItemWriter<? super T>> actualList) {
        this.actualList = actualList;
    }

    public void setOrder(boolean order) {
        isOrder = order;
    }
}
