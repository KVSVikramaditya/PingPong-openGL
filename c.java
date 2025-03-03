import org.springframework.context.ApplicationContext;
import javax.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class BeanScannerDebugger {
    private final ApplicationContext applicationContext;

    public BeanScannerDebugger(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @PostConstruct
    public void printAllBeans() {
        System.out.println("Beans in context:");
        for (String beanName : applicationContext.getBeanDefinitionNames()) {
            System.out.println(beanName);
        }
    }
}
