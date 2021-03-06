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
package org.androidtransfuse.gen.variableDecorator;

import com.google.inject.assistedinject.Assisted;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpression;
import org.androidtransfuse.analysis.astAnalyzer.VirtualProxyAspect;
import org.androidtransfuse.gen.InjectionBuilderContext;
import org.androidtransfuse.gen.proxy.VirtualProxyGenerator;
import org.androidtransfuse.gen.variableBuilder.ProxyVariableBuilder;
import org.androidtransfuse.gen.variableBuilder.TypedExpressionFactory;
import org.androidtransfuse.model.InjectionNode;
import org.androidtransfuse.model.TypedExpression;

import javax.inject.Inject;
import java.util.Map;

/**
 * @author John Ericksen
 */
public class VirtualProxyExpressionDecorator extends VariableExpressionBuilderDecorator {

    private final VirtualProxyGenerator virtualProxyGenerator;
    private final ProxyVariableBuilder proxyVariableBuilder;
    private final TypedExpressionFactory typedExpressionFactory;

    @Inject
    public VirtualProxyExpressionDecorator(@Assisted VariableExpressionBuilder decorated,
                                           ProxyVariableBuilder proxyVariableBuilder,
                                           VirtualProxyGenerator virtualProxyGenerator,
                                           TypedExpressionFactory typedExpressionFactory) {
        super(decorated);
        this.proxyVariableBuilder = proxyVariableBuilder;
        this.virtualProxyGenerator = virtualProxyGenerator;
        this.typedExpressionFactory = typedExpressionFactory;
    }

    @Override
    public TypedExpression buildVariableExpression(InjectionBuilderContext injectionBuilderContext, InjectionNode injectionNode) {
        VirtualProxyAspect proxyAspect = injectionNode.getAspect(VirtualProxyAspect.class);
        Map<InjectionNode, TypedExpression> variableMap = injectionBuilderContext.getVariableMap();
        TypedExpression variable;

        if (proxyAspect != null && proxyAspect.isProxyRequired() && !proxyAspect.isProxyDefined()) {
            //proxy
            JDefinedClass proxyDescriptor = virtualProxyGenerator.generateProxy(injectionNode);
            JExpression proxyExpression = proxyVariableBuilder.buildProxyInstance(injectionBuilderContext, injectionNode, proxyDescriptor);
            variable = typedExpressionFactory.build(injectionNode.getASTType(), proxyExpression);
            variableMap.put(injectionNode, variable);
            proxyAspect.setProxyExpression(variable);
            injectionBuilderContext.getProxyLoad().add(injectionNode);
        } else {
            variable = getDecorated().buildVariableExpression(injectionBuilderContext, injectionNode);
        }

        return variable;
    }
}
