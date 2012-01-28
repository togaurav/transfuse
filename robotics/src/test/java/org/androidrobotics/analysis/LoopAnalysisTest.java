package org.androidrobotics.analysis;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import org.androidrobotics.analysis.adapter.ASTClassFactory;
import org.androidrobotics.analysis.adapter.ASTType;
import org.androidrobotics.analysis.astAnalyzer.ASTInjectionAspect;
import org.androidrobotics.analysis.astAnalyzer.VirtualProxyAspect;
import org.androidrobotics.config.RoboticsGenerationGuiceModule;
import org.androidrobotics.gen.variableBuilder.ProviderVariableBuilder;
import org.androidrobotics.gen.variableBuilder.VariableBuilder;
import org.androidrobotics.gen.variableBuilder.VariableInjectionBuilderFactory;
import org.androidrobotics.model.ConstructorInjectionPoint;
import org.androidrobotics.model.InjectionNode;
import org.androidrobotics.util.JavaUtilLogger;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;
import javax.inject.Provider;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

/**
 * @author John Ericksen
 */
public class LoopAnalysisTest {

    private Analyzer analyzer;
    private ASTClassFactory astClassFactory;
    private AnalysisContext analysisContext;

    //A -> B (BImpl) -> C -> A
    public static class A {
        private B b;

        @Inject
        public A(B b) {
            this.b = b;
        }
    }

    public static interface B {
    }

    public static class BImpl implements B {
        private C c;

        @Inject
        public BImpl(C c) {
            this.c = c;
        }
    }

    public static class C {
        private A a;

        @Inject
        public C(A a) {
            this.a = a;
        }
    }

    //D -> E (EImpl) -> F (FProvider.get()) -> D

    public static class D {
        private E e;

        @Inject
        public D(E e) {
            this.e = e;
        }
    }

    public static interface E {
    }

    public static class EImpl implements E {
        private F f;

        @Inject
        public EImpl(F f) {
            this.f = f;
        }
    }

    public static class F {
        private D d;

        public F(D d) {
            this.d = d;
        }
    }

    public static class FProvider implements Provider<F> {

        private D d;

        @Inject
        public FProvider(D d) {
            this.d = d;
        }

        @Override
        public F get() {
            return new F(d);
        }
    }

    @Before
    public void setup() {
        Injector injector = Guice.createInjector(Stage.DEVELOPMENT, new RoboticsGenerationGuiceModule(new JavaUtilLogger(this)));

        VariableInjectionBuilderFactory variableInjectionBuilderFactory = injector.getInstance(VariableInjectionBuilderFactory.class);

        analyzer = injector.getInstance(Analyzer.class);
        astClassFactory = injector.getInstance(ASTClassFactory.class);

        analysisContext = injector.getInstance(SimpleAnalysisContextFactory.class).buildContext();

        analysisContext.getInjectionNodeBuilders().put(B.class.getName(),
                variableInjectionBuilderFactory.buildVariableInjectionNodeBuilder(astClassFactory.buildASTClassType(BImpl.class)));

        analysisContext.getInjectionNodeBuilders().put(F.class.getName(),
                variableInjectionBuilderFactory.buildProviderInjectionNodeBuilder(astClassFactory.buildASTClassType(FProvider.class)));

        analysisContext.getInjectionNodeBuilders().put(E.class.getName(),
                variableInjectionBuilderFactory.buildVariableInjectionNodeBuilder(astClassFactory.buildASTClassType(EImpl.class)));
    }

    @Test
    public void testProviderLoop() {
        ASTType astType = astClassFactory.buildASTClassType(D.class);

        InjectionNode injectionNode = analyzer.analyze(astType, astType, analysisContext);

        //D -> E
        ConstructorInjectionPoint deConstructorInjectionPoint = injectionNode.getAspect(ASTInjectionAspect.class).getConstructorInjectionPoint();
        assertEquals(1, deConstructorInjectionPoint.getInjectionNodes().size());
        InjectionNode eInjectionNode = deConstructorInjectionPoint.getInjectionNodes().get(0);
        assertTrue(isProxyRequired(eInjectionNode));
        assertEquals(EImpl.class.getName(), eInjectionNode.getClassName());

        //E -> F
        ConstructorInjectionPoint efConstructorInjectionPoint = eInjectionNode.getAspect(ASTInjectionAspect.class).getConstructorInjectionPoint();
        assertEquals(1, efConstructorInjectionPoint.getInjectionNodes().size());
        InjectionNode fInjectionNode = efConstructorInjectionPoint.getInjectionNodes().get(0);
        assertFalse(isProxyRequired(fInjectionNode));
        assertEquals(F.class.getName(), fInjectionNode.getClassName());

        //F -> D
        InjectionNode fProviderInjectionNode = ((ProviderVariableBuilder) fInjectionNode.getAspect(VariableBuilder.class)).getProviderInjectionNode();
        assertFalse(isProxyRequired(fProviderInjectionNode));
        assertEquals(FProvider.class.getName(), fProviderInjectionNode.getClassName());
    }

    @Test
    public void testBackLinkAnalysis() {
        ASTType astType = astClassFactory.buildASTClassType(A.class);

        InjectionNode injectionNode = analyzer.analyze(astType, astType, analysisContext);

        //A -> B
        ConstructorInjectionPoint abConstructorInjectionPoint = injectionNode.getAspect(ASTInjectionAspect.class).getConstructorInjectionPoint();
        assertEquals(1, abConstructorInjectionPoint.getInjectionNodes().size());
        InjectionNode bInjectionNode = abConstructorInjectionPoint.getInjectionNodes().get(0);
        assertTrue(isProxyRequired(bInjectionNode));
        assertEquals(BImpl.class.getName(), bInjectionNode.getClassName());

        //B -> C
        ConstructorInjectionPoint bcConstructorInjectionPoint = bInjectionNode.getAspect(ASTInjectionAspect.class).getConstructorInjectionPoint();
        assertEquals(1, bcConstructorInjectionPoint.getInjectionNodes().size());
        InjectionNode cInjectionNode = bcConstructorInjectionPoint.getInjectionNodes().get(0);
        assertFalse(isProxyRequired(cInjectionNode));
        assertEquals(C.class.getName(), cInjectionNode.getClassName());

        //C -> A
        ConstructorInjectionPoint caConstructorInjectionPoint = cInjectionNode.getAspect(ASTInjectionAspect.class).getConstructorInjectionPoint();
        assertEquals(1, caConstructorInjectionPoint.getInjectionNodes().size());
        InjectionNode aInjectionNode = caConstructorInjectionPoint.getInjectionNodes().get(0);
        assertFalse(isProxyRequired(aInjectionNode));
        assertEquals(A.class.getName(), aInjectionNode.getClassName());
    }

    private boolean isProxyRequired(InjectionNode injectionNode) {
        VirtualProxyAspect proxyAspect = injectionNode.getAspect(VirtualProxyAspect.class);

        return proxyAspect != null && proxyAspect.isProxyRequired();
    }
}