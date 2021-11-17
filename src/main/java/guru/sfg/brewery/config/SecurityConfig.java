package guru.sfg.brewery.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    // By providing a password encoder as a bean, it's overriding the default implementation of delegating password encoder.
    // it's telling Spring "use this password encoder!"
    @Bean
    PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests(authorize -> {
                    authorize
                            .antMatchers("/", "/webjars/**", "/login", "/resources/**").permitAll()
                            .antMatchers("/beers/find", "/beers*").permitAll()
                            .antMatchers(HttpMethod.GET, "/api/v1/beer/**").permitAll()
                            .mvcMatchers(HttpMethod.GET, "/api/v1/beerUpc/{upc}").permitAll(); // spring mvcMatchers works the same way except '**'
                })
                .authorizeRequests()
                .anyRequest().authenticated()
                .and()
                .formLogin()
                .and()
                .httpBasic();
    }

    // same thing as using userDetailsService() like the method below.
    //
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication()
                .withUser("spring")
                .password("guru") // {noop} is used to bypass password encoder.
                .roles("ADMIN")
                .and()
                .withUser("user")
                .password("password")
                .roles("USER");

        auth.inMemoryAuthentication()
                .withUser("scott")
                .password("tiger")
                .roles("CUSTOMER");
    }

    //    @Override
//    @Bean
//    protected UserDetailsService userDetailsService() {
//        UserDetails admin = User.withDefaultPasswordEncoder()
//                .username("spring")
//                .password("guru")
//                .roles("ADMIN")
//                .build();
//
//        UserDetails user = User.withDefaultPasswordEncoder()
//                .username("user")
//                .password("password")
//                .roles("USER")
//                .build();
//
//        return new InMemoryUserDetailsManager(admin, user);
//    }
}
