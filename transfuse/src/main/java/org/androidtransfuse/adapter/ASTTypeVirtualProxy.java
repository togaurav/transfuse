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
package org.androidtransfuse.adapter;

import org.androidtransfuse.analysis.TransfuseAnalysisException;
import org.androidtransfuse.model.PackageClass;
import org.apache.commons.lang.builder.EqualsBuilder;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;

/**
 * @author John Ericksen
 */
public class ASTTypeVirtualProxy implements ASTType {

    private final PackageClass packageClass;
    private ASTType proxy;

    public ASTTypeVirtualProxy(PackageClass packageClass) {
        this.packageClass = packageClass;
    }

    public void load(ASTType proxy) {
        this.proxy = proxy;
    }

    private ASTType getProxy() {
        if (proxy != null) {
            return proxy;
        }
        throw new TransfuseAnalysisException("Proxy not initialized prior to use");
    }

    @Override
    public Collection<ASTMethod> getMethods() {
        return getProxy().getMethods();
    }

    @Override
    public Collection<ASTField> getFields() {
        return getProxy().getFields();
    }

    @Override
    public Collection<ASTConstructor> getConstructors() {
        return getProxy().getConstructors();
    }

    @Override
    public boolean isConcreteClass() {
        return getProxy().isConcreteClass();
    }

    @Override
    public ASTType getSuperClass() {
        return getProxy().getSuperClass();
    }

    @Override
    public Collection<ASTType> getInterfaces() {
        return getProxy().getInterfaces();
    }

    @Override
    public boolean isArray() {
        return getProxy().isArray();
    }

    @Override
    public List<ASTType> getGenericParameters() {
        return getProxy().getGenericParameters();
    }

    @Override
    public boolean inheritsFrom(ASTType type) {
        return getProxy().inheritsFrom(type);
    }

    @Override
    public boolean extendsFrom(ASTType type) {
        return getProxy().extendsFrom(type);
    }

    @Override
    public boolean implementsFrom(ASTType type) {
        return getProxy().implementsFrom(type);
    }

    @Override
    public boolean isAnnotated(Class<? extends Annotation> annotation) {
        return getProxy().isAnnotated(annotation);
    }

    @Override
    public Collection<ASTAnnotation> getAnnotations() {
        return getProxy().getAnnotations();
    }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> annotation) {
        return getProxy().getAnnotation(annotation);
    }

    @Override
    public ASTAnnotation getASTAnnotation(Class annotation) {
        return getProxy().getASTAnnotation(annotation);
    }

    @Override
    public String getName() {
        return packageClass.getCanonicalName();
    }

    @Override
    public PackageClass getPackageClass() {
        return packageClass;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ASTType)) {
            return false;
        }

        ASTType that = (ASTType) o;

        return new EqualsBuilder().append(proxy, that).isEquals();
    }

    @Override
    public int hashCode() {
        return proxy != null ? proxy.hashCode() : 0;
    }
}
