/**
 * Copyright 2013 John Ericksen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.androidtransfuse.processor;

import com.google.common.collect.ImmutableSet;

import java.util.HashMap;
import java.util.Map;

/**
 * @author John Ericksen
 */
public class TransactionProcessorChain implements TransactionProcessor<Void, Void> {

    private final TransactionProcessor beforeProcessor;
    private final TransactionProcessor afterProcessor;

    public TransactionProcessorChain(TransactionProcessor beforeProcessor, TransactionProcessor afterProcessor) {
        this.beforeProcessor = beforeProcessor;
        this.afterProcessor = afterProcessor;
    }

    @Override
    public void execute() {
        beforeProcessor.execute();

        if (beforeProcessor.isComplete() && !afterProcessor.isComplete()) {
            afterProcessor.execute();
        }
    }

    @Override
    public boolean isComplete() {
        return beforeProcessor.isComplete() && afterProcessor.isComplete();
    }

    @Override
    public ImmutableSet<Exception> getErrors() {
        ImmutableSet.Builder<Exception> exceptions = ImmutableSet.builder();
        exceptions.addAll(beforeProcessor.getErrors());
        exceptions.addAll(afterProcessor.getErrors());

        return exceptions.build();
    }

    @Override
    public Map<Void, Void> getResults() {
        return new HashMap<Void, Void>();
    }
}
