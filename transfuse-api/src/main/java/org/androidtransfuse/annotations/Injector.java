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
package org.androidtransfuse.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * Defines an interface as a Transfuse Injector.  This annotation instructs Transfuse to generate an implementation
 * that builds the types specified as return values of the methods defined.</p>
 *
 * <p>
 * Example:
 * <pre>
 *     {@code @Injector}
 *     public interface InjectorExample{
 *         BuildMe build();
 *     }
 * </pre>
 * </p>
 * <p>Transfuse will build the injection graph generation code within an implementation of {@code InjectorExample}.  In
 * order to get an instance of the generated Injector, one must simply inject the Injector by the interface type.
 * Transfuse automatically binds the generated implementation to the interface:
 * <pre>
 *     {@code @Inject}
 *     InjectorExample injector;
 * </pre>
 * </p>
 * <p>
 * If Dependency Injection is unavailable, one may get an instance of the generated Injector through the
 * {@code Injectors} utility class:
 * <pre>
 *     InjectorExample injector = Injectors.get(InjectorExample.class);
 * </pre>
 * </p>
 *
 * @see org.androidtransfuse.Injectors
 *
 * @author John Ericksen
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Injector {}
