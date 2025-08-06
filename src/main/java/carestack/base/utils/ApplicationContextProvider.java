package carestack.base.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Utility class to provide access to Spring ApplicationContext from non-Spring managed classes.
 * This allows retrieving beans programmatically when dependency injection is not available.
 */
@Component
public class ApplicationContextProvider implements ApplicationContextAware {

    private static ApplicationContext context;

    /**
     * Returns the Spring ApplicationContext.
     *
     * @return ApplicationContext instance
     */
    public static ApplicationContext getApplicationContext() {
        return context;
    }

    /**
     * Retrieves a bean by class type.
     *
     * @param beanClass the class type of the bean
     * @param <T> the type parameter
     * @return the bean instance
     */
    public static <T> T getBean(Class<T> beanClass) {
        return context.getBean(beanClass);
    }

    /**
     * Retrieves a bean by name.
     *
     * @param beanName the name of the bean
     * @return the bean instance
     */
    public static Object getBean(String beanName) {
        return context.getBean(beanName);
    }

    /**
     * Sets the ApplicationContext. Called by Spring during initialization.
     *
     * @param ctx the ApplicationContext
     * @throws BeansException if bean access fails
     */
    @Override
    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
        context = ctx;
    }
}