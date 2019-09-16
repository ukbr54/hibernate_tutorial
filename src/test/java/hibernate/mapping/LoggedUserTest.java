package hibernate.mapping;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.Session;
import org.hibernate.annotations.GenerationTime;
import org.hibernate.annotations.GeneratorType;
import org.hibernate.tuple.ValueGenerator;
import org.junit.Test;
import util.AbstractTest;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Created by Ujjwal Gupta on Aug,2019
 */

public class LoggedUserTest extends AbstractTest {

    @Override
    protected Class<?>[] entities() {
        return new Class<?>[]{
                Sensor.class
        };
    }

    @Override
    protected void afterInit(){
       LoggedUser.logIn("Alice");

       doInJPA(entityManager -> {
           Sensor  ip = new Sensor();
           ip.setName("ip");
           ip.setValue("192.168.0.7");
           entityManager.persist(ip);

           executeSync(() -> {
               LoggedUser.logIn("Bob");

               doInJPA(_entityManager -> {
                   Sensor temperature = new Sensor();
                   temperature.setName("temperature");
                   temperature.setValue("32");

                   _entityManager.persist(temperature);
               });
               LoggedUser.logOut();
           });
       });
       LoggedUser.logOut();
    }

    @Test
    public void test() {
        LoggedUser.logIn("Alice");

        doInJPA(entityManager -> {
            Sensor temperature = entityManager.find(Sensor.class, "temperature");

            temperature.setValue("36");

            executeSync(() -> {
                LoggedUser.logIn("Bob");

                doInJPA(_entityManager -> {
                    Sensor ip = _entityManager.find(Sensor.class, "ip");

                    ip.setValue("192.168.0.102");
                });

                LoggedUser.logOut();
            });
        });

        LoggedUser.logOut();
    }

    /**
     * With the ValueGenerator interface, Hibernate allows us to customize the way a given entity property is going to be generated.
     * Now, we only need to instruct Hibernate to use the LoggedUserGenerator for the createdBy and updatedBy properties of
     * our Sensor entity.
     */
    @Setter
    @Getter
    @Entity(name = "Sensor")
    @Table(name = "sensor")
    public static class Sensor {

        @Id
        @Column(name = "sensor_name")
        private String name;

        @Column(name = "sensor_value")
        private String value;

        @Column(name = "created_by")
        @GeneratorType(
                type = LoggedUserGenerator.class,
                when = GenerationTime.ALWAYS
        )
        private String createdBy;

        @Column(name = "updated_by")
        @GeneratorType(
                type = LoggedUserGenerator.class,
                when = GenerationTime.ALWAYS
        )
        private String updatedBy;
    }

    /**
     *  we want to pass the currently logged user to the createdBy and updatedBy properties of our Sensor entity,
     *  To do so, we will create the following ValueGenerator Hibernate utility
     */
    public static class LoggedUserGenerator implements ValueGenerator<String> {

        public LoggedUserGenerator(){}

        @Override
        public String generateValue(Session session, Object o) {
            return LoggedUser.get();
        }
    }

    /**
     *  ThreadLocal utility which stores the currently logged user
     */
    public static class LoggedUser{

        private static final ThreadLocal<String> userHolder = new ThreadLocal<>();

        public static void logIn(String user){
            userHolder.set(user);
        }

        public static void logOut(){
            userHolder.remove();
        }

        public static String get(){
            return userHolder.get();
        }
    }

    /**
     * In a web application Servlet Filter, the LoggedUser.logIn method can be called using the currently authenticated user,
     * and the LoggedUser.logOut method is called right after returning from the inner FilterChain.doFilter invocation.
     */
    public static class LoggedUserFilter implements Filter{

        @Override
        public void init(FilterConfig filterConfig) throws ServletException { }

        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
                throws IOException, ServletException {
            try{
                HttpServletRequest httpServletRequest = (HttpServletRequest)request;
                LoggedUser.logIn(httpServletRequest.getRemoteUser());

                filterChain.doFilter(request,response);
            }finally {
                LoggedUser.logOut();
            }
        }

        @Override
        public void destroy() { }
    }

}
