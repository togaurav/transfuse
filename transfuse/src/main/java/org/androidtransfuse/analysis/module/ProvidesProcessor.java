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
package org.androidtransfuse.analysis.module;

import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSet;
import org.androidtransfuse.adapter.ASTAnnotation;
import org.androidtransfuse.adapter.ASTMethod;
import org.androidtransfuse.adapter.ASTType;
import org.androidtransfuse.adapter.classes.ASTClassFactory;
import org.androidtransfuse.analysis.TransfuseAnalysisException;
import org.androidtransfuse.analysis.repository.InjectionNodeBuilderRepositoryFactory;
import org.androidtransfuse.annotations.Provides;
import org.androidtransfuse.gen.variableBuilder.VariableInjectionBuilderFactory;
import org.androidtransfuse.model.InjectionSignature;
import org.androidtransfuse.util.QualifierPredicate;
import org.androidtransfuse.util.matcher.Matcher;
import org.androidtransfuse.util.matcher.Matchers;

import javax.inject.Inject;
import java.util.Collection;

/**
 * Sets up the @Provides configuration.
 *
 * @author John Ericksen
 */
public class ProvidesProcessor implements MethodProcessor {

    private final InjectionNodeBuilderRepositoryFactory injectionNodeBuilders;
    private final VariableInjectionBuilderFactory variableInjectionBuilderFactory;
    private final QualifierPredicate qualifierPredicate;
    private final ASTClassFactory astClassFactory;

    @Inject
    public ProvidesProcessor(InjectionNodeBuilderRepositoryFactory injectionNodeBuilders,
                             VariableInjectionBuilderFactory variableInjectionBuilderFactory,
                             QualifierPredicate qualifierPredicate,
                             ASTClassFactory astClassFactory) {
        this.injectionNodeBuilders = injectionNodeBuilders;
        this.variableInjectionBuilderFactory = variableInjectionBuilderFactory;
        this.qualifierPredicate = qualifierPredicate;
        this.astClassFactory = astClassFactory;
    }

    public ModuleConfiguration process(ASTType moduleType, ASTMethod astMethod, ASTAnnotation astAnnotation) {

        ImmutableSet<ASTAnnotation> qualifierAnnotations =
                FluentIterable.from(astMethod.getAnnotations())
                        .filter(qualifierPredicate).toImmutableSet();

        validate(astMethod.getAnnotations());

        return new ProvidesModuleConfiguration(moduleType, qualifierAnnotations, astMethod);
    }

    private void validate(Collection<ASTAnnotation> annotations) {
        ImmutableSet<ASTAnnotation> nonQualifierAnnotations =
                FluentIterable.from(annotations)
                        .filter(Predicates.not(qualifierPredicate)).toImmutableSet();

        ASTType providesType = astClassFactory.getType(Provides.class);

        ImmutableSet.Builder<ASTType> erroredAnnotations = ImmutableSet.builder();
        for (ASTAnnotation annotation : nonQualifierAnnotations) {
            if(!annotation.getASTType().equals(providesType)){
                //error
                erroredAnnotations.add(annotation.getASTType());
            }
        }

        ImmutableSet<ASTType> errored = erroredAnnotations.build();
        if(errored.size() > 0){
            throw new TransfuseAnalysisException("Found non-Provides, non-Qualifier annotation");
        }
    }

    private final class ProvidesModuleConfiguration implements ModuleConfiguration {

        private final ASTType moduleType;
        private final ASTMethod astMethod;
        private final ImmutableSet<ASTAnnotation> qualifiers;

        private ProvidesModuleConfiguration(ASTType moduleType, ImmutableSet<ASTAnnotation> qualifiers, ASTMethod astMethod) {
            this.moduleType = moduleType;
            this.astMethod = astMethod;
            this.qualifiers = qualifiers;
        }

        @Override
        public void setConfiguration() {

            Matcher<InjectionSignature> matcher = Matchers.type(astMethod.getReturnType()).annotated().byAnnotation(qualifiers).build();

            injectionNodeBuilders.putInjectionSignatureConfig(matcher,
                    variableInjectionBuilderFactory.buildProvidesInjectionNodeBuilder(moduleType, astMethod));
        }
    }
}
