package org.androidtransfuse.gen.variableBuilder;

import android.content.Context;
import org.androidtransfuse.analysis.AnalysisContext;
import org.androidtransfuse.analysis.InjectionPointFactory;
import org.androidtransfuse.analysis.adapter.ASTAnnotation;
import org.androidtransfuse.analysis.adapter.ASTClassFactory;
import org.androidtransfuse.analysis.adapter.ASTType;
import org.androidtransfuse.model.InjectionNode;

import javax.inject.Inject;

/**
 * @author John Ericksen
 */
public class ApplicationVariableInjectionNodeBuilder implements InjectionNodeBuilder {

    private ASTClassFactory astClassFactory;
    private InjectionPointFactory injectionPointFactory;
    private VariableInjectionBuilderFactory variableInjectionBuilderFactory;

    @Inject
    public ApplicationVariableInjectionNodeBuilder(ASTClassFactory astClassFactory, InjectionPointFactory injectionPointFactory, VariableInjectionBuilderFactory variableInjectionBuilderFactory) {
        this.astClassFactory = astClassFactory;
        this.injectionPointFactory = injectionPointFactory;
        this.variableInjectionBuilderFactory = variableInjectionBuilderFactory;
    }

    @Override
    public InjectionNode buildInjectionNode(ASTType astType, AnalysisContext context, ASTAnnotation annotation) {
        InjectionNode injectionNode = new InjectionNode(astType);

        ASTType contextType = astClassFactory.buildASTClassType(Context.class);
        InjectionNode contextInjectionNode = injectionPointFactory.buildInjectionNode(contextType, context);

        injectionNode.addAspect(VariableBuilder.class, variableInjectionBuilderFactory.buildApplicationVariableBuilder(contextInjectionNode));

        return injectionNode;
    }
}
