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
package org.androidtransfuse;

import com.google.inject.ImplementedBy;
import com.google.inject.Key;
import com.google.inject.name.Names;
import org.androidtransfuse.adapter.ASTType;
import org.androidtransfuse.adapter.element.ASTElementConverterFactory;
import org.androidtransfuse.analysis.TransfuseAnalysisException;
import org.androidtransfuse.annotations.*;
import org.androidtransfuse.config.EnterableScope;
import org.androidtransfuse.config.TransfuseAndroidModule;
import org.androidtransfuse.config.TransfuseGenerateGuiceModule;
import org.androidtransfuse.model.manifest.Manifest;
import org.androidtransfuse.model.r.RBuilder;
import org.androidtransfuse.model.r.RResource;
import org.androidtransfuse.model.r.RResourceComposite;
import org.androidtransfuse.processor.ReloadableASTElementFactory;
import org.androidtransfuse.processor.TransfuseProcessor;
import org.androidtransfuse.util.Logger;
import org.androidtransfuse.util.ManifestLocator;
import org.androidtransfuse.util.ManifestSerializer;
import org.androidtransfuse.util.SupportedAnnotations;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import java.io.File;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import static com.google.common.collect.Collections2.transform;
import static org.androidtransfuse.config.TransfuseInjector.buildInjector;

/**
 * Transfuse Annotation processor.  Kicks off the process of analyzing and generating code based on the compiled
 * codebase.
 * <p/>
 * To use this class, you simply have to annotate your classes with the proper root components (Activity,
 * Application, etc) and have this annotation processor on the classpath during a full compilation.
 * <p/>
 * This approach is compatible with Java 6 and above.
 * <p/>
 * See http://androidtransfuse.org for more details
 *
 * @author John Ericksen
 */
@SupportedAnnotations({
        Activity.class,
        Application.class,
        BroadcastReceiver.class,
        Service.class,
        Fragment.class,
        TransfuseModule.class,
        Injector.class,
        ImplementedBy.class})
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class TransfuseAnnotationProcessor extends AnnotationProcessorBase {

    @Inject
    private ASTElementConverterFactory astElementConverterFactory;
    @Inject
    private ManifestSerializer manifestParser;
    @Inject
    private RBuilder rBuilder;
    @Inject
    private ReloadableASTElementFactory reloadableASTElementFactory;
    @Inject
    private ManifestLocator manifestLocator;
    @Inject
    private Logger logger;
    @Inject
    @Named(TransfuseGenerateGuiceModule.CONFIGURATION_SCOPE)
    private EnterableScope configurationScope;
    @Inject
    private Provider<TransfuseProcessor> processorProvider;
    @Inject
    private Elements elements;
    private boolean baseModuleConfiguration = false;

    @Override
    public void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        buildInjector(processingEnv).injectMembers(this);
    }

    @Override
    public boolean process(Set<? extends TypeElement> typeElements, RoundEnvironment roundEnvironment) {

        long start = System.currentTimeMillis();

        //setup transfuse processor with manifest and R classes
        File manifestFile = manifestLocator.findManifest();
        Manifest manifest = manifestParser.readManifest(manifestFile);

        RResourceComposite r = new RResourceComposite(
                buildR(rBuilder, manifest.getApplicationPackage() + ".R"),
                buildR(rBuilder, "android.R"));

        configurationScope.enter();

        configurationScope.seed(Key.get(File.class, Names.named(TransfuseGenerateGuiceModule.MANIFEST_FILE)), manifestFile);
        configurationScope.seed(RResource.class, r);
        configurationScope.seed(Key.get(Manifest.class, Names.named(TransfuseGenerateGuiceModule.ORIGINAL_MANIFEST)), manifest);

        TransfuseProcessor transfuseProcessor = processorProvider.get();

        if (!baseModuleConfiguration) {
            transfuseProcessor.submit(TransfuseModule.class, reloadableASTElementFactory.buildProviders(
                    Collections.singleton(elements.getTypeElement(TransfuseAndroidModule.class.getName())
                    )));
            baseModuleConfiguration = true;
        }

        Set<? extends Element> applicationTypes = roundEnvironment.getElementsAnnotatedWith(Application.class);

        if (applicationTypes.size() > 1) {
            throw new TransfuseAnalysisException("Unable to process with more than one application defined");
        }

        //process components
        if (applicationTypes.isEmpty()) {
            transfuseProcessor.generateEmptyApplication();
        } else {
            transfuseProcessor.submit(Application.class, reloadableASTElementFactory.buildProviders((applicationTypes)));
        }

        transfuseProcessor.submit(TransfuseModule.class, buildASTCollection(roundEnvironment, TransfuseModule.class));
        transfuseProcessor.submit(ImplementedBy.class, buildASTCollection(roundEnvironment, ImplementedBy.class));
        transfuseProcessor.submit(Injector.class, buildASTCollection(roundEnvironment, Injector.class));
        transfuseProcessor.submit(Activity.class, buildASTCollection(roundEnvironment, Activity.class));
        transfuseProcessor.submit(BroadcastReceiver.class, buildASTCollection(roundEnvironment, BroadcastReceiver.class));
        transfuseProcessor.submit(Service.class, buildASTCollection(roundEnvironment, Service.class));
        transfuseProcessor.submit(Fragment.class, buildASTCollection(roundEnvironment, Fragment.class));

        transfuseProcessor.execute();

        if (roundEnvironment.processingOver()) {
            transfuseProcessor.checkForErrors();
        }

        logger.info("Transfuse took " + (System.currentTimeMillis() - start) + "ms to process");

        configurationScope.exit();

        return true;
    }

    private RResource buildR(RBuilder rBuilder, String className) {
        TypeElement rTypeElement = elements.getTypeElement(className);
        if (rTypeElement != null) {
            Collection<? extends ASTType> rInnerTypes = wrapASTCollection(ElementFilter.typesIn(rTypeElement.getEnclosedElements()));
            return rBuilder.buildR(rInnerTypes);
        }
        return null;
    }

    private Collection<Provider<ASTType>> buildASTCollection(RoundEnvironment round, Class<? extends Annotation> annotation) {
        return reloadableASTElementFactory.buildProviders(round.getElementsAnnotatedWith(annotation));
    }

    private Collection<? extends ASTType> wrapASTCollection(Collection<? extends Element> elementCollection) {
        return transform(elementCollection,
                astElementConverterFactory.buildASTElementConverter(ASTType.class)
        );
    }
}
