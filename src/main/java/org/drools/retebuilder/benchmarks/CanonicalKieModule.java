package org.drools.retebuilder.benchmarks;

import java.io.File;
import java.util.Collection;
import org.drools.compiler.kie.builder.impl.KieProject;
import org.drools.compiler.kie.builder.impl.ResultsImpl;
import org.drools.compiler.kie.builder.impl.ZipKieModule;
import org.drools.compiler.kproject.models.KieBaseModelImpl;
import org.drools.core.impl.InternalKnowledgeBase;
import org.drools.model.Model;
import org.drools.retebuilder.CanonicalKieBase;
import org.kie.api.KieBaseConfiguration;
import org.kie.api.builder.ReleaseId;
import org.kie.api.builder.model.KieModuleModel;

public class CanonicalKieModule extends ZipKieModule {

    private final Collection<String> ruleClassesNames;

    public CanonicalKieModule(final ReleaseId releaseId, final KieModuleModel kieProject, final File file,
            final Collection<String> ruleClassesNames) {
        super(releaseId, kieProject, file);
        this.ruleClassesNames = ruleClassesNames;
    }

    @Override
    public InternalKnowledgeBase createKieBase(final KieBaseModelImpl kBaseModel, final KieProject kieProject,
            final ResultsImpl messages, final KieBaseConfiguration conf) {
        final CanonicalKieBase kieBase = new CanonicalKieBase();
        final KieProjectClassLoader kieProjectCL = new KieProjectClassLoader(kieProject);
        ruleClassesNames.forEach(ruleClassName -> addRuleClassToKieBase(ruleClassName, kieBase, kieProjectCL));
        return kieBase;
    }

    private void addRuleClassToKieBase(final String ruleClassName, final CanonicalKieBase kieBase,
            final KieProjectClassLoader kieProjectClassLoader) {
        final Model model = kieProjectClassLoader.createInstance(ruleClassName);
        model.getRules().forEach(kieBase::addRule);
    }

    class KieProjectClassLoader extends ClassLoader {

        public KieProjectClassLoader(final KieProject kieProject) {
            super(kieProject.getClassLoader());
        }

        public Class<?> loadClass(final String className) throws ClassNotFoundException {
            try {
                return super.loadClass(className);
            } catch (final ClassNotFoundException cnfe) {
            }

            final String fileName = className.replace('.', '/') + ".class";
            final byte[] bytes = getBytes(fileName);

            if (bytes == null) {
                throw new ClassNotFoundException(className);
            }

            return defineClass(className, bytes, 0, bytes.length);
        }

        public <T> T createInstance(final String className) {
            try {
                return (T) loadClass(className).newInstance();
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
