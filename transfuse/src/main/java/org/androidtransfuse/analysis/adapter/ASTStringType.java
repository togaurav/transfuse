package org.androidtransfuse.analysis.adapter;

import org.androidtransfuse.model.PackageClass;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author John Ericksen
 */
public class ASTStringType implements ASTType {

    private final String name;

    public ASTStringType(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public PackageClass getPackageClass() {
        return new PackageClass(null, name);
    }

    @Override
    public Collection<ASTMethod> getMethods() {
        return Collections.emptyList();
    }

    @Override
    public Collection<ASTField> getFields() {
        return Collections.emptyList();
    }

    @Override
    public Collection<ASTConstructor> getConstructors() {
        return Collections.emptyList();
    }

    @Override
    public boolean isConcreteClass() {
        return true;
    }

    @Override
    public ASTType getSuperClass() {
        return null;
    }

    @Override
    public Collection<ASTType> getInterfaces() {
        return Collections.emptyList();
    }

    @Override
    public boolean isArray() {
        return false;
    }

    @Override
    public List<ASTType> getGenericParameters() {
        return Collections.emptyList();
    }

    @Override
    public boolean inheritsFrom(ASTType type) {
        return false;
    }

    @Override
    public boolean extendsFrom(ASTType type) {
        return false;
    }

    @Override
    public boolean implementsFrom(ASTType type) {
        return false;
    }

    @Override
    public boolean isAnnotated(Class<? extends Annotation> annotation) {
        return false;
    }

    @Override
    public Collection<ASTAnnotation> getAnnotations() {
        return Collections.emptyList();
    }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> annotation) {
        return null;
    }

    @Override
    public ASTAnnotation getASTAnnotation(Class annotation) {
        return null;
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

        return new EqualsBuilder().append(getName(), that.getName()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(getName()).hashCode();
    }

    @Override
    public String toString() {
        return getName();
    }
}
