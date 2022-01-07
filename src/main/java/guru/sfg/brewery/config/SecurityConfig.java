package guru.sfg.brewery.config;

import guru.sfg.brewery.security.RestHeaderAuthFilter;
import guru.sfg.brewery.security.RestUrlAuthFilter;
import guru.sfg.brewery.security.SfgPasswordEncoderFactories;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    public RestHeaderAuthFilter restHeaderAuthFilter(AuthenticationManager authenticationManager) {
        RestHeaderAuthFilter filter = new RestHeaderAuthFilter(new AntPathRequestMatcher("/api/**"));
        filter.setAuthenticationManager(authenticationManager);
        return filter;
    }

    public RestUrlAuthFilter restUrlAuthFilter(AuthenticationManager authenticationManager) {
        RestUrlAuthFilter filter = new RestUrlAuthFilter(new AntPathRequestMatcher("/api/**"));
        filter.setAuthenticationManager(authenticationManager);
        return filter;
    }


    // By providing a password encoder as a bean, it's overriding the default implementation of delegating password encoder.
    // it's telling Spring "use this password encoder!"
    @Bean
    PasswordEncoder passwordEncoder() {
        //return NoOpPasswordEncoder.getInstance();
        //return new LdapShaPasswordEncoder();
        //return new StandardPasswordEncoder();
        //return new BCryptPasswordEncoder();
        //return PasswordEncoderFactories.createDelegatingPasswordEncoder();
        return SfgPasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http.addFilterBefore(restHeaderAuthFilter(authenticationManager()), UsernamePasswordAuthenticationFilter.class)
                .csrf().disable();

        http.addFilterBefore(restUrlAuthFilter(authenticationManager()), UsernamePasswordAuthenticationFilter.class);


        // this list will get longer for large project, so use annotation based like @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')") on method level
        http
                .authorizeRequests(authorize -> {
                    authorize
                            .antMatchers("/h2-console/**").permitAll() // do not use in production
                            .antMatchers("/", "/webjars/**", "/login", "/resources/**").permitAll()
                            .antMatchers(HttpMethod.GET, "/api/v1/beer/**").hasAnyRole("ADMIN", "CUSTOMER", "USER")
                            //.mvcMatchers(HttpMethod.DELETE, "/api/v1/beer/**").hasRole("ADMIN")
                            .mvcMatchers(HttpMethod.GET, "/api/v1/beerUpc/{upc}").hasAnyRole("ADMIN", "CUSTOMER", "USER") // spring mvcMatchers works the same way except '**'
                            .mvcMatchers("/brewery/breweries").hasAnyRole("ADMIN", "CUSTOMER") // allow ROLE_ADMIN and ROLE_CUSTOMER
                            .mvcMatchers(HttpMethod.GET, "/brewery/api/v1/breweries").hasAnyRole("ADMIN", "CUSTOMER")
                            .mvcMatchers("/beers/find", "/beers/{bearId}").hasAnyRole("ADMIN", "CUSTOMER", "USER");
                })
                .authorizeRequests()
                .anyRequest().authenticated()
                .and()
                .formLogin()
                .and()
                .httpBasic();

        // h2 console config
        // http://localhost:8080/h2-console and enter jdbc:h2:mem:bddf0570-9280-4b8c-9cbd-24299c855a97 from log. Default 'User Name' is 'sa'. Leave password blank.
        http.headers().frameOptions().sameOrigin();
    }

    // same thing as using userDetailsService() like the method below.
    //
//    @Override
//    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
//        auth.inMemoryAuthentication()
//                .withUser("spring")
//                .password("{bcrypt}$2a$10$wlXPfWnO0PcBxEsR4vPlxeK6mRdF1pTJR3XIa.xsP.hhM/Q3w9I5a") // {noop} is used to bypass password encoder.
//                .roles("ADMIN")
//                .and()
//                .withUser("user")
//                .password("{sha256}e3765dc5c16d794d179bd1c1949b5a275f61ac5d431c3de6a84cb9c7386fb75abd5696bc18a4d89b")
//                .roles("USER");
//
//        auth.inMemoryAuthentication()
//                .withUser("scott")
//                .password("{bcrypt10}$2a$10$m58Jz/ZnSk5tLwAs2MqI4eeRmUX2tD4z.ggcRZ3qo3rrKgndfPZXO")
//                .roles("CUSTOMER");
//    }

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
