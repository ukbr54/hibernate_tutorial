package hibernate.forum.dto;

import org.hibernate.boot.Metadata;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;

import java.util.List;

/**
 * Created by Ujjwal Gupta on Sep,2019
 */
public class ClassImportIntegrator implements Integrator {

    private final List<Class> classImportList;

    public ClassImportIntegrator(List<Class> classImportList) {
        this.classImportList = classImportList;
    }

    @Override
    public void integrate(Metadata metadata,
                          SessionFactoryImplementor sessionFactoryImplementor,
                          SessionFactoryServiceRegistry sessionFactoryServiceRegistry) {
        for(Class classImport : classImportList){
            metadata.getImports().put(classImport.getSimpleName(),classImport.getName());
        }
    }

    @Override
    public void disintegrate(SessionFactoryImplementor sessionFactoryImplementor,
                             SessionFactoryServiceRegistry sessionFactoryServiceRegistry) {

    }
}
