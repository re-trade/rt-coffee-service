package org.retrade.main.init;

import lombok.RequiredArgsConstructor;
import org.retrade.main.model.entity.RoleEntity;
import org.retrade.main.repository.jpa.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RoleInitializer implements CommandLineRunner {
    private final RoleRepository roleRepository;
    @Override
    public void run(String... args) {
        List<RoleEntity> defaultRoles = List.of(
                RoleEntity.builder()
                        .name("Role Customer")
                        .code("ROLE_CUSTOMER")
                        .build(),

                RoleEntity.builder()
                        .name("Role Customer")
                        .code("ROLE_SELLER")
                        .build(),

                RoleEntity.builder()
                        .name("Role Admin")
                        .code("ROLE_ADMIN")
                        .build()
        );
        defaultRoles.forEach(orderStatus -> {
            roleRepository.findByCode(orderStatus.getCode())
                    .ifPresentOrElse(
                            existing -> System.out.println("Role already exists: " + existing.getCode()),
                            () -> {
                                roleRepository.save(orderStatus);
                                System.out.println("Added order status: " + orderStatus.getCode());
                            }
                    );
        });
    }
}
