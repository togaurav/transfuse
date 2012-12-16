/**
 * Copyright 2012 John Ericksen
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
package org.androidtransfuse.intentFactory;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

/**
 * @author John Ericksen
 */
public class ServiceIntentFactoryStrategy extends AbstractIntentFactoryStrategy {

    protected ServiceIntentFactoryStrategy(Class<? extends Context> targetContext, Bundle bundle) {
        super(targetContext, bundle);
    }

    @Override
    public void start(Context context, Intent intent) {
        context.startService(intent);
    }
}
